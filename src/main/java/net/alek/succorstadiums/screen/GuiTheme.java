package net.alek.succorstadiums.screen;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

public class GuiTheme {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("SuccorStadiums/GuiTheme");

    private static final File PREFS_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "succorstadiums/gui_prefs.json"
    );

    private Theme theme;

    public GuiTheme() {
        theme = loadTheme();
    }

    public Theme getTheme() {
        return theme;
    }

    public String getThemeName() {
        return theme.name();
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        saveTheme(theme);
    }

    public void nextTheme() {
        theme = theme.next();
        saveTheme(theme);
    }

    public void previousTheme() {
        theme = theme.previous();
        saveTheme(theme);
    }

    public int bg() {
        return theme.bg;
    }

    public int sidebar() {
        return theme.sidebar;
    }

    public int panel() {
        return theme.panel;
    }

    public int border() {
        return theme.border;
    }

    public int header() {
        return theme.header;
    }

    public int text() {
        return theme.text;
    }

    public int subtext() {
        return theme.subtext;
    }

    private static void saveTheme(Theme theme) {
        try {
            PREFS_FILE.getParentFile().mkdirs();

            String json = """
                    {
                      "theme": "%s"
                    }
                    """.formatted(theme.name());

            Files.writeString(PREFS_FILE.toPath(), json);

        } catch (Exception e) {
            LOGGER.error("Failed to save GUI theme.", e);
        }
    }

    private static Theme loadTheme() {

        try {

            if (!PREFS_FILE.exists()) {
                return Theme.DARK;
            }

            String json = Files.readString(PREFS_FILE.toPath());

            for (Theme theme : Theme.values()) {
                if (json.contains("\"theme\": \"" + theme.name() + "\"")) {
                    return theme;
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load GUI theme.", e);
        }

        return Theme.DARK;
    }
}