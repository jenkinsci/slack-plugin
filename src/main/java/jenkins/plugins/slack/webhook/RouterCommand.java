package jenkins.plugins.slack.webhook;




public interface RouterCommand<T> {
    T execute(String... args);
}
