package jenkins.plugins.slack.decisions;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import javax.annotation.Nullable;
import jenkins.plugins.slack.logging.BuildKey;

public class Context {
    private final AbstractBuild<?, ?> current;
    private final AbstractBuild<?, ?> previous;

    public Context(AbstractBuild<?, ?> current, AbstractBuild<?, ?> previous) {
        this.current = current;
        this.previous = previous;
    }

    public String currentKey() {
        return BuildKey.format(current);
    }

    public Result previousResultOrSuccess() {
        if (previous == null || previous.getResult() == null) {
            return Result.SUCCESS;
        }
        return previous.getResult();
    }

    @Nullable
    public Result currentResult() {
        if (current == null) {
            return null;
        }
        return current.getResult();
    }
}
