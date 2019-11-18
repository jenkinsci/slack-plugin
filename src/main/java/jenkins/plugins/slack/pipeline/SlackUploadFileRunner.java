package jenkins.plugins.slack.pipeline;

import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.TaskListener;
import hudson.util.DirScanner;
import hudson.util.FileVisitor;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.plugins.slack.HttpClient;
import jenkins.security.MasterToSlaveCallable;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

class SlackUploadFileRunner extends MasterToSlaveCallable<Boolean, Throwable> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String API_URL = "https://slack.com/api/files.upload";
    private static final Logger logger = Logger.getLogger(SlackUploadFileRunner.class.getName());

    private final FilePath filePath;
    private String fileToUploadPath;

    private final String channels;

    private final String token;

    private final TaskListener listener;
    private final String initialComment;
    private final ProxyConfiguration proxy;

    SlackUploadFileRunner(TaskListener listener, ProxyConfiguration proxy, SlackFileRequest slackFileRequest) {
        this.listener = listener;
        this.filePath = slackFileRequest.getFilePath();
            this.fileToUploadPath = slackFileRequest.getFileToUploadPath();
        this.channels = slackFileRequest.getChannels();
        this.initialComment = slackFileRequest.getInitialComment();
        this.token = slackFileRequest.getToken();
        this.proxy = proxy;
    }

    @Override
    public Boolean call() throws Throwable {
        listener.getLogger().println(String.format("Using dirname=%s and includeMask=%s", filePath.getRemote(), fileToUploadPath));

        final List<File> files = new ArrayList<>();
        new DirScanner.Glob(fileToUploadPath, null).scan(new File(filePath.getRemote()), new FileVisitor() {
            @Override
            public void visit(File file, String relativePath) {
                if (file.isFile()) {
                    listener.getLogger().println("Adding file (only first one will be uploaded) " + file.getAbsolutePath());

                    files.add(file);
                }
            }
        });

        if (files.isEmpty()) {
            listener.getLogger().println("No files found for mask=" + this.filePath);
            return false;
        }

        return doIt(files.get(0));
    }

    private boolean doIt(File file) {
        try (CloseableHttpClient client = HttpClient.getCloseableHttpClient(proxy)) {
            String threadTs = null;
            String theChannels = channels;

            //thread_ts is passed once with roomId: Ex: roomId:threadTs
            String[] splitThread = channels.split("[:]+");
            if (splitThread.length > 1) {
                theChannels = splitThread[0];
                threadTs = splitThread[1];
            }

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
                    .addTextBody("token", token, ContentType.DEFAULT_TEXT)
                    .addTextBody("channels", theChannels, ContentType.DEFAULT_TEXT);

            if (initialComment != null) {
                multipartEntityBuilder = multipartEntityBuilder
                        .addTextBody("initial_comment", initialComment, ContentType.DEFAULT_TEXT);
            }

            if (threadTs != null) {
                multipartEntityBuilder = multipartEntityBuilder
                        .addTextBody("thread_ts", threadTs, ContentType.DEFAULT_TEXT);
            }

            HttpUriRequest request = RequestBuilder
                    .post(API_URL)
                    .setEntity(multipartEntityBuilder.build())
                    .build();

            ResponseHandler<JSONObject> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? new org.json.JSONObject(EntityUtils.toString(entity)) : null;
                } else {
                    logger.log(Level.WARNING, "Slack upload may have failed. Response: " + status);
                    return null;
                }
            };

            org.json.JSONObject responseBody = client.execute(request, responseHandler);

            if (responseBody != null) {
                return responseBody.getBoolean("ok");
            } else {
                return false;
            }
        } catch (Exception e) {
            String msg = "Exception uploading file '" + file + "' to Slack ";
            logger.log(Level.WARNING, msg, e);
            listener.getLogger().println(msg + e.getMessage());
        }
        return false;
    }

}
