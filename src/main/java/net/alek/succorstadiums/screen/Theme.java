package net.alek.succorstadiums.screen;

public enum Theme {

    LIGHT(
            0xFFF0F0F0,
            0xFFDDDDDD,
            0xFFFFFFFF,
            0xFFAAAAAA,
            0xFF333333,
            0xFF222222,
            0xFF666666
    ),

    DARK(
            0xFF1E1E2E,
            0xFF181825,
            0xFF242436,
            0xFF44445A,
            0xFFCDD6F4,
            0xFFCDD6F4,
            0xFF9399B2
    ),

    CATPPUCCIN(
            0xFF1E1E2E,
            0xFF181825,
            0xFF313244,
            0xFF585B70,
            0xFFF5E0DC,
            0xFFCDD6F4,
            0xFFBAC2DE
    ),

    OCEAN(
            0xFF0B1220,
            0xFF132238,
            0xFF1E3A5F,
            0xFF4F83CC,
            0xFFE3F2FD,
            0xFFF8FBFF,
            0xFFAFC9E8
    ),

    FOREST(
            0xFF102418,
            0xFF183423,
            0xFF22553A,
            0xFF3F8F62,
            0xFFDFF8E6,
            0xFFF2FFF5,
            0xFFAED9BB
    ),

    SUNSET(
            0xFF331514,
            0xFF4B1E1B,
            0xFF70312B,
            0xFFF28C28,
            0xFFFFE0B2,
            0xFFFFF7F0,
            0xFFFFC58A
    ),

    CRIMSON(
            0xFF2B0E12,
            0xFF42131A,
            0xFF5C1C26,
            0xFFC0392B,
            0xFFFFD8D8,
            0xFFFFF2F2,
            0xFFE6B5B5
    ),

    PURPLE(
            0xFF20152F,
            0xFF302044,
            0xFF473066,
            0xFF8E63D2,
            0xFFF1E5FF,
            0xFFFFF7FF,
            0xFFD6B7F5
    ),

    EMERALD(
            0xFF0D2118,
            0xFF133125,
            0xFF1D4D38,
            0xFF3CB878,
            0xFFD7FFE8,
            0xFFF3FFF8,
            0xFFA5DDBD
    ),

    MIDNIGHT(
            0xFF090B12,
            0xFF101521,
            0xFF1A2233,
            0xFF3A4B67,
            0xFFE6ECF8,
            0xFFF8FAFF,
            0xFFA6B4CC
    );

    public final int bg;
    public final int sidebar;
    public final int panel;
    public final int border;
    public final int header;
    public final int text;
    public final int subtext;

    Theme(int bg, int sidebar, int panel, int border,
          int header, int text, int subtext) {
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