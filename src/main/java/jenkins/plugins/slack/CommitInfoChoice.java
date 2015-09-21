package jenkins.plugins.slack;

public enum CommitInfoChoice {
    NONE("nothing about commits") {
        @Override
        public boolean showAuthor() {
            return false;
        }
        @Override
        public boolean showTitle() {
            return false;
        }
    },

    AUTHORS("commit list with authors only") {
        @Override
        public boolean showAuthor() {
            return true;
        }
        @Override
        public boolean showTitle() {
            return false;
        }
    },

    AUTHORS_AND_TITLES("commit list with authors and titles") {
        @Override
        public boolean showAuthor() {
            return true;
        }
        @Override
        public boolean showTitle() {
            return true;
        }
    };

    private final String displayName;

    private CommitInfoChoice(String displayName) {
        this.displayName = displayName;
    }

    public abstract boolean showAuthor();
    public abstract boolean showTitle();
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
