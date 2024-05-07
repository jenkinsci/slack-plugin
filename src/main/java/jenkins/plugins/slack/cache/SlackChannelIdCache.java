package jenkins.plugins.slack.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import hudson.AbortException;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.HttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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

    private static Map<String, String> populateCache(String token) {
        try (CloseableHttpClient client = HttpClient.getCloseableHttpClient(Jenkins.get().getProxy())) {
            return convertChannelNameToId(client, token, new HashMap<>(), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getChannelId(String botUserToken, String channelName) throws ExecutionException, InterruptedException, AbortException {
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

    private static Map<String, String> convertChannelNameToId(CloseableHttpClient client, String token, Map<String, String> channels, String cursor) throws IOException {
        RequestBuilder requestBuilder = RequestBuilder.get("https://slack.com/api/conversations.list")
                .addHeader("Authorization", "Bearer " + token)
                .addParameter("exclude_archived", "true")
                .addParameter("types", "public_channel,private_channel");

        if (cursor != null) {
            requestBuilder.addParameter("cursor", cursor);
        }
        ResponseHandler<JSONObject> standardResponseHandler = getStandardResponseHandler();
        JSONObject result = client.execute(requestBuilder.build(), standardResponseHandler);

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
            return convertChannelNameToId(client, token, channels, cursor);
        }

        return channels;
    }

    private static ResponseHandler<JSONObject> getStandardResponseHandler() {
        return response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? new JSONObject(EntityUtils.toString(entity)) : null;
            } else {
                String errorMessage = UPLOAD_FAILED_TEMPLATE + status + " " + EntityUtils.toString(response.getEntity());
                throw new HttpStatusCodeException(response.getStatusLine().getStatusCode(), errorMessage);
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
