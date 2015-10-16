package jenkins.plugins.slack;

public enum CommitInfoChoice {
    NONE("nothing about commits",                             false, false),
    AUTHORS("commit list with authors only",                  true,  false),
    AUTHORS_AND_TITLES("commit list with authors and titles", true,  true);

    private final String displayName;
    private boolean showAuthor;
    private boolean showTitle;

    private CommitInfoChoice(String displayName, boolean showAuthor, boolean showTitle) {
        this.displayName = displayName;
        this.showAuthor = showAuthor;
        this.showTitle = showTitle;
    }

    public boolean showAuthor() {
        return this.showAuthor;
    }
    public boolean showTitle() {
        return this.showTitle;
    }
    public boolean showAnything() {
        return showAuthor() || showTitle();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static CommitInfoChoice forDisplayName(String displayName) {
        for (CommitInfoChoice commitInfoChoice : values()) {
            if (commitInfoChoice.getDisplayName().equals(displayName)) {
                return commitInfoChoice;
            }
        }
        return null;
    }
}
