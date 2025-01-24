package jenkins.plugins.slack.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import hudson.AbortException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.HttpClient;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.json.JSONArray;
import org.json.JSONObject;

public class SlackChannelIdCache {

    private static final String UPLOAD_FAILED_TEMPLATE = "Failed to retrieve channel names. Response: ";
    private static final Logger logger = Logger.getLogger(SlackChannelIdCache.class.getName());

    // cache that includes all channel names and IDs for each workspace used
    private static final LoadingCache<String, Map<String, String>> CHANNEL_METADATA_CACHE = Caffeine.newBuilder()
            .maximumSize(100)
            .refreshAfterWrite(Duration.ofHours(24))
            .build(SlackChannelIdCache::populateCache);
    private static final int MAX_RETRIES = 10;

    private static Map<String, String> populateCache(String token) {
        HttpClientBuilder closeableHttpClientBuilder = HttpClient.getCloseableHttpClientBuilder(Jenkins.get().getProxy())
                .setRetryStrategy(new HttpRequestRetryStrategy() {

                    private long retryInterval;

                    @Override
                    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
                        boolean shouldRetry = executionCount <= MAX_RETRIES &&
                                response.getCode() == HttpStatus.SC_TOO_MANY_REQUESTS;
                        if (shouldRetry) {
                            Header firstHeader = response.getFirstHeader("Retry-After");
                            if (firstHeader != null) {
                                retryInterval = Long.parseLong(firstHeader.getValue()) * 1000L;
                                logger.info(String.format("Rate limited by Slack, retrying in %dms", retryInterval));
                            }
                        }
                        return shouldRetry;
                    }

                    @Override
                    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
                        return false;
                    }

                    @Override
                    public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
                        return TimeValue.ofSeconds(retryInterval);
                    }
                });
        try (CloseableHttpClient client = closeableHttpClientBuilder.build()) {
            return convertChannelNameToId(client, token, new HashMap<>(), null);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getChannelId(String botUserToken, String channel) throws ExecutionException, InterruptedException, AbortException {
        String channelName = cleanChannelName(channel);
        if (channelName.matches("^(C[A-Z0-9]{8}|C[A-Z0-9]{10}|G[A-Z0-9]{10}|D[A-Z0-9]{8})$")) {
            return channelName;
        }

        Map<String, String> channelNameToIdMap = CHANNEL_METADATA_CACHE.get(botUserToken);
        String channelId = channelNameToIdMap.get(channelName);

        // most likely is that a new channel has been created since the last cache refresh
        // or a typo in the channel name, a bit risky in larger workspaces but shouldn't happen too often
        if (channelId == null) {
            try {
                CompletableFuture<Map<String, String>> newResult = CHANNEL_METADATA_CACHE.refresh(botUserToken);
                channelNameToIdMap = newResult.get();
            } catch (CompletionException e) {
                throw new AbortException("Failed uploading file to slack, channel not found: " + channelName + ", error: " + e.getMessage());
            }

            channelId = channelNameToIdMap.get(channelName);
        }

        return channelId;
    }

    private static String cleanChannelName(String channelName) {
        String[] splitForThread = channelName.split(":", 2);
        String channel = channelName;
        if (splitForThread.length == 2) {
            channel = splitForThread[0];
        }
        if (channel.startsWith("#")) {
            return channel.substring(1);
        }
        return channel;
    }


    private static Map<String, String> convertChannelNameToId(CloseableHttpClient client, String token, Map<String, String> channels, String cursor) throws IOException, URISyntaxException {
        convertPublicChannelNameToId(client, token, channels, cursor);
        convertPrivateChannelNameToId(client, token, channels, cursor);
        return channels;
    }

    private static Map<String, String> convertPublicChannelNameToId(CloseableHttpClient client, String token, Map<String, String> channels, String cursor) throws IOException, URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder("https://slack.com/api/conversations.list")
                .addParameter("exclude_archived", "true")
                .addParameter("types", "public_channel")
                .addParameter("limit", "999");
        if (cursor != null) {
            uriBuilder.addParameter("cursor", cursor);
        }
        Request requestBuilder = Request.get(uriBuilder.build())
                .addHeader("Authorization", "Bearer " + token);
        JSONObject result = requestBuilder.execute(client).handleResponse(getStandardResponseHandler());

        if (!result.getBoolean("ok")) {
            logger.warning("Couldn't convert channel name to ID in Slack: " + result);
            return channels;
        }

        JSONArray channelsArray = result.getJSONArray("channels");
        for (int i = 0; i < channelsArray.length(); i++) {
            JSONObject channel = channelsArray.getJSONObject(i);

            String channelName = channel.getString("name");
            String channelId = channel.getString("id");

            channels.put(channelName, channelId);
        }

        cursor = result.getJSONObject("response_metadata").getString("next_cursor");
        if (cursor != null && !cursor.isEmpty()) {
            return convertPublicChannelNameToId(client, token, channels, cursor);
        }

        return channels;
    }

    private static Map<String, String> convertPrivateChannelNameToId(CloseableHttpClient client, String token, Map<String, String> channels, String cursor) throws IOException, URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder("https://slack.com/api/conversations.list")
                .addParameter("exclude_archived", "true")
                .addParameter("types", "private_channel")
                .addParameter("limit", "999");
        if (cursor != null) {
            uriBuilder.addParameter("cursor", cursor);
        }
        Request requestBuilder = Request.get(uriBuilder.build())
                .addHeader("Authorization", "Bearer " + token);
        JSONObject result = requestBuilder.execute(client).handleResponse(getStandardResponseHandler());

        if (!result.getBoolean("ok")) {
            logger.warning("Couldn't convert channel name to ID in Slack: " + result);
            return channels;
        }

        JSONArray channelsArray = result.getJSONArray("channels");
        for (int i = 0; i < channelsArray.length(); i++) {
            JSONObject channel = channelsArray.getJSONObject(i);

            String channelName = channel.getString("name");
            String channelId = channel.getString("id");

            channels.put(channelName, channelId);
        }

        cursor = result.getJSONObject("response_metadata").getString("next_cursor");
        if (cursor != null && !cursor.isEmpty()) {
            return convertPrivateChannelNameToId(client, token, channels, cursor);
        }

        return channels;
    }

    private static HttpClientResponseHandler<JSONObject> getStandardResponseHandler() {
        return response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? new JSONObject(EntityUtils.toString(entity)) : null;
            } else {
                String errorMessage = UPLOAD_FAILED_TEMPLATE + status + " " + EntityUtils.toString(response.getEntity());
                throw new HttpStatusCodeException(response.getCode(), errorMessage);
            }
        };
    }

    public static class HttpStatusCodeException extends RuntimeException {
        private final int statusCode;

        public HttpStatusCodeException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public static void clearCache() {
        CHANNEL_METADATA_CACHE.invalidateAll();
    }
}
