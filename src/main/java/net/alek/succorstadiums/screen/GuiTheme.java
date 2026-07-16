package net.alek.succorstadiums.screen;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

public class GuiTheme {

    private static final Logger LOGGER = LoggerFactory.getLogger("SuccorStadiums/GuiTheme");

    private static final int LIGHT_BG      = 0xFFF0F0F0;
    private static final int LIGHT_SIDEBAR = 0xFFDDDDDD;
    private static final int LIGHT_PANEL   = 0xFFFFFFFF;
    private static final int LIGHT_BORDER  = 0xFFAAAAAA;
    private static final int LIGHT_HEADER  = 0xFF333333;
    private static final int LIGHT_TEXT    = 0xFF222222;
    private static final int LIGHT_SUBTEXT = 0xFF666666;

    private static final int DARK_BG      = 0xFF1E1E2E;
    private static final int DARK_SIDEBAR = 0xFF181825;
    private static final int DARK_PANEL   = 0xFF242436;
    private static final int DARK_BORDER  = 0xFF44445A;
    private static final int DARK_HEADER  = 0xFFCDD6F4;
    private static final int DARK_TEXT    = 0xFFCDD6F4;
    private static final int DARK_SUBTEXT = 0xFF9399B2;

    private static final File PREFS_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "succorstadiums/gui_prefs.json"
    );

    private boolean darkMode;

    public GuiTheme() {
        this.darkMode = loadDarkMode();
    }

    public boolean isDarkMode() { return darkMode; }

    public void toggle() {
        darkMode = !darkMode;
        saveDarkMode(darkMode);
    }

    public int bg()      { return darkMode ? DARK_BG      : LIGHT_BG; }
    public int sidebar()  { return darkMode ? DARK_SIDEBAR : LIGHT_SIDEBAR; }
    public int panel()   { return darkMode ? DARK_PANEL   : LIGHT_PANEL; }
    public int border()  { return darkMode ? DARK_BORDER  : LIGHT_BORDER; }
    public int header()  { return darkMode ? DARK_HEADER  : LIGHT_HEADER; }
    public int text()    { return darkMode ? DARK_TEXT     : LIGHT_TEXT; }
    public int subtext() { return darkMode ? DARK_SUBTEXT  : LIGHT_SUBTEXT; }

    private static void saveDarkMode(boolean darkMode) {
        try {
            PREFS_FILE.getParentFile().mkdirs();
            Files.writeString(PREFS_FILE.toPath(), "{\"darkMode\":" + darkMode + "}");
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private static boolean loadDarkMode() {
        try {
            if (!PREFS_FILE.exists()) return false;
            String json = new String(Files.readAllBytes(PREFS_FILE.toPath()));
            return json.contains("\"darkMode\":true");
        } catch (Exception e) {
            return false;
        }
    }
}