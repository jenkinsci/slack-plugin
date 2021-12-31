package jenkins.plugins.slack.decisions;

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.tasks.junit.TestResultAction;
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

    public Result currentResultOrSuccess() {
        if (current == null || current.getResult() == null) {
            return Result.SUCCESS;
        }
        return current.getResult();
    }

    @Nullable
    private TestResultAction getTestResult(AbstractBuild<?, ?> build) {
        if (build == null) { return null; }
        return build.getAction(TestResultAction.class);
    }

    @Nullable
    public TestResultAction getPreviousTestResult() {
        return getTestResult(previous);
    }

    @Nullable
    public TestResultAction getCurrentTestResult() {
        return getTestResult(current);
    }
}
