package jenkins.plugins.hipchat;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;

import java.util.Map;
import java.util.logging.Logger;

@Extension
@SuppressWarnings("rawtypes")
public class HipChatListener extends RunListener<AbstractBuild> {

    private static final Logger logger = Logger.getLogger(HipChatListener.class.getName());

    public HipChatListener() {
        super(AbstractBuild.class);
    }

    @Override
    public void onCompleted(AbstractBuild r, TaskListener listener) {
        getNotifier(r.getProject()).completed(r);
        super.onCompleted(r, listener);
    }

    @Override
    public void onStarted(AbstractBuild r, TaskListener listener) {
        // getNotifier(r.getProject()).started(r);
        // super.onStarted(r, listener);
    }

    @Override
    public void onDeleted(AbstractBuild r) {
        // getNotifier(r.getProject()).deleted(r);
        // super.onDeleted(r);
    }

    @Override
    public void onFinalized(AbstractBuild r) {
        // getNotifier(r.getProject()).finalized(r);
        // super.onFinalized(r);
    }

    @SuppressWarnings("unchecked")
    FineGrainedNotifier getNotifier(AbstractProject project) {
        Map<Descriptor<Publisher>, Publisher> map = project.getPublishersList().toMap();
        for (Publisher publisher : map.values()) {
            if (publisher instanceof HipChatNotifier) {
                return new ActiveNotifier((HipChatNotifier) publisher);
            }
        }
        return new DisabledNotifier();
    }

}
