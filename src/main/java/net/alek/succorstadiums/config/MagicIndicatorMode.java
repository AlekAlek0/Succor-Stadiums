package net.alek.succorstadiums.config;

public enum MagicIndicatorMode {
    CROSSHAIR("Crosshair"),
    HOTBAR("Hotbar"),
    OFF("OFF");

    private final String displayName;

    MagicIndicatorMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}