package jenkins.plugins.slack.pipeline;

import hudson.FilePath;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
public class SlackFileRequest {
    private final String fileToUploadPath;
    private final String token;
    private final String channels;

    private final String initialComment;
    private final FilePath filePath;

    public SlackFileRequest(FilePath filePath, String token, String channels, String initialComment, String fileToUploadPath) {
        this.token = token;
        this.channels = channels;
        this.initialComment = initialComment;
        this.filePath = filePath;
        this.fileToUploadPath = fileToUploadPath;
    }

    public String getToken() {
        return token;
    }

    public String getChannels() {
        return channels;
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
}
