package jenkins.plugins.slack.pipeline;

import hudson.FilePath;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
public class SlackFileRequest {
    private final String fileToUploadPath;
    private final String token;
    private final String channelId;
    private final String threadTs;

    private final String initialComment;
    private final FilePath filePath;

    public SlackFileRequest(FilePath filePath, String token, String channelId, String initialComment, String fileToUploadPath, String threadTs) {
        this.token = token;
        this.channelId = channelId;
        this.initialComment = initialComment;
        this.filePath = filePath;
        this.fileToUploadPath = fileToUploadPath;
        this.threadTs = threadTs;
    }

    public String getToken() {
        return token;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getInitialComment() {
        return initialComment;
    }

    public FilePath getFilePath() {
        return filePath;
    }

    public String getFileToUploadPath() {
        return fileToUploadPath;
    }

    public String getThreadTs() {
        return threadTs;
    }
}
