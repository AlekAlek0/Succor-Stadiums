package net.alek.succorstadiums.screen.mobarenagui;

import java.awt.Color;

public enum Theme {

    LIGHT(
            new Color(0xFFF0F0F0, true), // Background
            new Color(0xFFDDDDDD, true), // Sidebar
            new Color(0xFFFFFFFF, true), // Panel
            new Color(0xFFAAAAAA, true), // Border
            new Color(0xFF333333, true), // Header
            new Color(0xFF222222, true), // Text
            new Color(0xFF666666, true)  // Subtext
    ),

    DARK(
            new Color(0xFF1E1E2E, true), // Background
            new Color(0xFF181825, true), // Sidebar
            new Color(0xFF242436, true), // Panel
            new Color(0xFF44445A, true), // Border
            new Color(0xFFCDD6F4, true), // Header
            new Color(0xFFCDD6F4, true), // Text
            new Color(0xFF9399B2, true)  // Subtext
    ),

    CATPPUCCIN(
            new Color(0xFF1E1E2E, true), // Background
            new Color(0xFF181825, true), // Sidebar
            new Color(0xFF313244, true), // Panel
            new Color(0xFF585B70, true), // Border
            new Color(0xFFF5E0DC, true), // Header
            new Color(0xFFCDD6F4, true), // Text
            new Color(0xFFBAC2DE, true)  // Subtext
    ),

    OCEAN(
            new Color(0xFF0B1220, true), // Background
            new Color(0xFF132238, true), // Sidebar
            new Color(0xFF1E3A5F, true), // Panel
            new Color(0xFF4F83CC, true), // Border
            new Color(0xFFE3F2FD, true), // Header
            new Color(0xFFF8FBFF, true), // Text
            new Color(0xFFAFC9E8, true)  // Subtext
    ),

    FOREST(
            new Color(0xFF102418, true), // Background
            new Color(0xFF183423, true), // Sidebar
            new Color(0xFF22553A, true), // Panel
            new Color(0xFF3F8F62, true), // Border
            new Color(0xFFDFF8E6, true), // Header
            new Color(0xFFF2FFF5, true), // Text
            new Color(0xFFAED9BB, true)  // Subtext
    ),

    SUNSET(
            new Color(0xFF331514, true), // Background
            new Color(0xFF4B1E1B, true), // Sidebar
            new Color(0xFF70312B, true), // Panel
            new Color(0xFFF28C28, true), // Border
            new Color(0xFFFFE0B2, true), // Header
            new Color(0xFFFFF7F0, true), // Text
            new Color(0xFFFFC58A, true)  // Subtext
    ),

    CRIMSON(
            new Color(0xFF2B0E12, true), // Background
            new Color(0xFF42131A, true), // Sidebar
            new Color(0xFF5C1C26, true), // Panel
            new Color(0xFFC0392B, true), // Border
            new Color(0xFFFFD8D8, true), // Header
            new Color(0xFFFFF2F2, true), // Text
            new Color(0xFFE6B5B5, true)  // Subtext
    ),

    PURPLE(
            new Color(0xFF20152F, true), // Background
            new Color(0xFF302044, true), // Sidebar
            new Color(0xFF473066, true), // Panel
            new Color(0xFF8E63D2, true), // Border
            new Color(0xFFF1E5FF, true), // Header
            new Color(0xFFFFF7FF, true), // Text
            new Color(0xFFD6B7F5, true)  // Subtext
    ),

    EMERALD(
            new Color(0xFF0D2118, true), // Background
            new Color(0xFF133125, true), // Sidebar
            new Color(0xFF1D4D38, true), // Panel
            new Color(0xFF3CB878, true), // Border
            new Color(0xFFD7FFE8, true), // Header
            new Color(0xFFF3FFF8, true), // Text
            new Color(0xFFA5DDBD, true)  // Subtext
    ),

    MIDNIGHT(
            new Color(0xFF090B12, true), // Background
            new Color(0xFF101521, true), // Sidebar
            new Color(0xFF1A2233, true), // Panel
            new Color(0xFF3A4B67, true), // Border
            new Color(0xFFE6ECF8, true), // Header
            new Color(0xFFF8FAFF, true), // Text
            new Color(0xFFA6B4CC, true)  // Subtext
    );

    public final Color bg;
    public final Color sidebar;
    public final Color panel;
    public final Color border;
    public final Color header;
    public final Color text;
    public final Color subtext;

    Theme(Color bg, Color sidebar, Color panel, Color border, Color header, Color text, Color subtext) {
        this.bg = bg;
        this.sidebar = sidebar;
        this.panel = panel;
        this.border = border;
        this.header = header;
        this.text = text;
        this.subtext = subtext;
    }

    public Theme next() {
        Theme[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public Theme previous() {
        Theme[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }
}