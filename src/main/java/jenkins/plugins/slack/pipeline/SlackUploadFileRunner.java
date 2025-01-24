package jenkins.plugins.slack.pipeline;

import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.TaskListener;
import hudson.util.DirScanner;
import hudson.util.FileVisitor;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.plugins.slack.HttpClient;
import jenkins.security.MasterToSlaveCallable;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

public class SlackUploadFileRunner extends MasterToSlaveCallable<Boolean, Throwable> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String GET_UPLOAD_URL_API = "https://slack.com/api/files.getUploadURLExternal";
    private static final Logger logger = Logger.getLogger(SlackUploadFileRunner.class.getName());
    private static final String UPLOAD_FAILED_TEMPLATE = "Slack upload may have failed. Response: ";

    private final FilePath filePath;
    private final String fileToUploadPath;

    private final String channelId;

    private final String token;

    private final TaskListener listener;
    private final String initialComment;
    private final ProxyConfiguration proxy;
    private final String threadTs;

    public SlackUploadFileRunner(TaskListener listener, ProxyConfiguration proxy, SlackFileRequest slackFileRequest) {
        this.listener = listener;
        this.filePath = slackFileRequest.getFilePath();
        this.fileToUploadPath = slackFileRequest.getFileToUploadPath();
        this.channelId = slackFileRequest.getChannelId();
        this.threadTs = slackFileRequest.getThreadTs();
        this.initialComment = slackFileRequest.getInitialComment();
        this.token = slackFileRequest.getToken();
        this.proxy = proxy;
    }

    @Override
    public Boolean call() throws Throwable {
        logger.fine(filePath + "");
        logger.fine(fileToUploadPath);
        listener.getLogger().printf("Using dirname=%s and includeMask=%s%n", filePath.getRemote(), fileToUploadPath);

        final List<File> files = new ArrayList<>();
        new DirScanner.Glob(fileToUploadPath, null).scan(new File(filePath.getRemote()), new FileVisitor() {
            @Override
            public void visit(File file, String relativePath) {
                if (file.isFile()) {
                    listener.getLogger().println("Adding file " + file.getAbsolutePath());
                    files.add(file);
                }
            }
        });

        if (files.isEmpty()) {
            listener.getLogger().println("No files found for mask=" + this.filePath);
            return false;
        }

        return doIt(files);
    }

    private boolean doIt(List<File> files) {
        List<String> fileIds = new ArrayList<>();
        try (CloseableHttpClient client = HttpClient.getCloseableHttpClient(proxy)) {
            for (File file : files) {
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                        .setMode(HttpMultipartMode.LEGACY)
                        .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
                JSONObject getUploadUrlResult = getUploadUrlExternal(file, client);
                if (getUploadUrlResult == null) {
                    return false;
                }

                String uploadUrl = getUploadUrlResult.getString("upload_url");

                if (!uploadFile(uploadUrl, multipartEntityBuilder, client)) {
                    listener.getLogger().println("Failed to upload file to Slack");
                    return false;
                }
                String fileId = getUploadUrlResult.getString("file_id");
                fileIds.add(fileId);
            }
            if (!completeUploadExternal(channelId, threadTs , fileIds, client)) {
                listener.getLogger().println("Failed to complete uploading file to Slack");
                return false;
            }

        } catch (IOException | ParseException | URISyntaxException e) {
            String msg = "Exception uploading to Slack ";
            logger.log(Level.WARNING, msg, e);
            listener.getLogger().println(msg + e.getMessage());
        }
        return true;
    }

    private boolean completeUploadExternal(String channelId, String threadTs, List<String> fileIds, CloseableHttpClient client) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channel_id", channelId);
        if (initialComment != null) {
            jsonObject.put("initial_comment", initialComment);
        }
        if (threadTs != null) {
            jsonObject.put("thread_ts", threadTs);
        }

        jsonObject.put("files", convertListToJsonArray(fileIds));
        Request completeRequest = Request
                .post("https://slack.com/api/files.completeUploadExternal")
                .body(new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON))
                .addHeader("Authorization", "Bearer " + token);

        JSONObject completeRequestResponse = completeRequest.execute(client).handleResponse(getStandardResponseHandler());

        if (completeRequestResponse != null && !completeRequestResponse.getBoolean("ok")) {
            listener.getLogger().println(UPLOAD_FAILED_TEMPLATE + completeRequestResponse);
            return false;
        }

        return true;
    }

    private static JSONArray convertListToJsonArray(List<String> fileIds) {
        JSONArray jsonArray = new JSONArray();
        fileIds.stream()
                .map(fileId -> new JSONObject().put("id", fileId))
                .forEach(jsonArray::put);
        return jsonArray;
    }

    private HttpClientResponseHandler<JSONObject> getStandardResponseHandler() {
        return response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? new JSONObject(EntityUtils.toString(entity)) : null;
            } else {
                String errorMessage = UPLOAD_FAILED_TEMPLATE + status + " " + EntityUtils.toString(response.getEntity());
                listener.getLogger().println(errorMessage);
                logger.log(Level.WARNING, errorMessage);
                return null;
            }
        };
    }

    private boolean uploadFile(String uploadUrl, MultipartEntityBuilder multipartEntityBuilder, CloseableHttpClient client) throws IOException, ParseException {
        Request request = Request.post(uploadUrl)
                .body(multipartEntityBuilder.build())
                .addHeader("Authorization", "Bearer " + token);

        Response responseBody = request.execute(client);
        if (responseBody.returnResponse().getCode() != HttpStatus.SC_OK) {
            listener.getLogger().println(UPLOAD_FAILED_TEMPLATE + responseBody.returnContent().asString());
            return false;
        }
        return true;
    }

    private JSONObject getUploadUrlExternal(File file, CloseableHttpClient client) throws IOException, URISyntaxException {
        URI uri = new URIBuilder(GET_UPLOAD_URL_API)
                .addParameter("filename", file.getName())
                .addParameter("length", String.valueOf(file.length()))
                .build();
        Request getUploadApiRequest = Request.get(uri)
                .addHeader("Authorization", "Bearer " + token);

        JSONObject getUploadRequestResponse = getUploadApiRequest.execute(client).handleResponse(getStandardResponseHandler());
        if (getUploadRequestResponse != null && !getUploadRequestResponse.getBoolean("ok")) {
            listener.getLogger().println(UPLOAD_FAILED_TEMPLATE + getUploadRequestResponse);
            return null;
        } else if (getUploadRequestResponse == null) {
            listener.getLogger().println(UPLOAD_FAILED_TEMPLATE);
            return null;
        }
        return getUploadRequestResponse;
    }
}
