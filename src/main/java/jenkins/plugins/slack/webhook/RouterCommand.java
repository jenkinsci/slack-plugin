package jenkins.plugins.slack.webhook;




public interface RouterCommand<T> {
    public T execute(String... args);
}
