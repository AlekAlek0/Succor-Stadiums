package net.alek.succorstadiums.config;

public enum PouchScrollDirection {
    DEFAULT("DEFAULT"),
    INVERT("INVERTED");

    private final String displayName;

    PouchScrollDirection(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}