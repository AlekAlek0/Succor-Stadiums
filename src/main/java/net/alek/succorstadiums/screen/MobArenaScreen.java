package net.alek.succorstadiums.screen;

import net.alek.succorstadiums.network.ArenaActionPayload;
import net.alek.succorstadiums.network.ArenaDataPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * MobArenaScreen is the main client-side GUI for managing Mob Arenas.
 *
 * It is split into two panels:
 *   - Left sidebar: lists all known arenas, plus a "New Arena" button
 *   - Right detail panel: shows info/controls for the selected arena,
 *     or a form for creating/editing arenas and adding mobs
 *
 * All arena data lives on the server. This screen only displays what
 * the server sends back, and sends action packets for every change.
 */
public class MobArenaScreen extends Screen {

    // ── Layout constants ─────────────────────────────────────────────────────

    private static final int SIDEBAR_W = 110; // pixel width of the left sidebar
    private static final int PANEL_PAD = 8;   // inner padding used throughout
    private static final int BTN_H     = 16;  // standard button height
    private static final int ROW_H     = 18;  // row height for arena/wave list rows

    // Theme Toggle

    private boolean darkMode = loadDarkMode();

    // Light theme
    private static final int LIGHT_BG      = 0xFFF0F0F0;
    private static final int LIGHT_SIDEBAR = 0xFFDDDDDD;
    private static final int LIGHT_PANEL   = 0xFFFFFFFF;
    private static final int LIGHT_BORDER  = 0xFFAAAAAA;
    private static final int LIGHT_HEADER  = 0xFF333333;
    private static final int LIGHT_TEXT    = 0xFF222222;
    private static final int LIGHT_SUBTEXT = 0xFF666666;

    // Dark theme
    private static final int DARK_BG      = 0xFF1E1E2E;
    private static final int DARK_SIDEBAR = 0xFF181825;
    private static final int DARK_PANEL   = 0xFF242436;
    private static final int DARK_BORDER  = 0xFF44445A;
    private static final int DARK_HEADER  = 0xFFCDD6F4;
    private static final int DARK_TEXT    = 0xFFCDD6F4;
    private static final int DARK_SUBTEXT = 0xFF9399B2;

    // Color getters

    private int colBg()      { return darkMode ? DARK_BG      : LIGHT_BG; }
    private int colSidebar() { return darkMode ? DARK_SIDEBAR : LIGHT_SIDEBAR; }
    private int colPanel()   { return darkMode ? DARK_PANEL   : LIGHT_PANEL; }
    private int colBorder()  { return darkMode ? DARK_BORDER  : LIGHT_BORDER; }
    private int colHeader()  { return darkMode ? DARK_HEADER  : LIGHT_HEADER; }
    private int colText()    { return darkMode ? DARK_TEXT     : LIGHT_TEXT; }
    private int colSubtext() { return darkMode ? DARK_SUBTEXT  : LIGHT_SUBTEXT; }


    // ── UI state ─────────────────────────────────────────────────────────────

    /** The list of arenas received from the server. Refreshed every 2 seconds. */
    private List<ArenaDataPayload.ArenaEntry> arenas = new ArrayList<>();

    /** Index into `arenas` for whichever arena the user clicked. -1 = none selected. */
    private int selectedArena = -1;

    /** The wave number the user is currently adding mobs to. -1 = none. */
    private int selectedWave  = -1;

    /**
     * Controls which "page" the right-hand detail panel shows.
     * Switching views calls rebuildWidgets() to swap out all buttons/fields.
     */
    private enum DetailView { OVERVIEW, ADD_ARENA, VIEW_MOB, ADD_MOB, DEL_MOB, EDIT_ARENA }
    private DetailView detailView = DetailView.OVERVIEW;

    // ── Input fields ─────────────────────────────────────────────────────────

    // ADD_ARENA / EDIT_ARENA fields
    private EditBox nameField, xField, yField, zField, radiusField, delayField;

    // ADD_MOB and DEL_MOB fields
    private EditBox mobTypeField, mobCountField;

    // ── Mob type autocomplete ─────────────────────────────────────────────────

    /** Current filtered list of entity ID suggestions matching what the user typed. */
    private List<String> mobSuggestions = new ArrayList<>();

    /** Index of the highlighted suggestion (navigated with Tab). */
    private int selectedSuggestion = 0;

    /** How many rows the dropdown has been scrolled down. */
    private int suggestionScrollOffset = 0;

    /** Maximum number of suggestion rows visible in the dropdown at once. */
    private static final int MAX_VISIBLE_SUGGESTIONS = 8;

    // ── Player picker ─────────────────────────────────────────────────────────

    /** Players the user has checked to include when starting an arena. */
    private final List<String> selectedPlayers = new ArrayList<>();

    /** When true, the detail panel shows the player-picker overlay instead of the normal view. */
    private boolean showPlayerPicker = false;

    // ── Misc ──────────────────────────────────────────────────────────────────

    /** Always 0 — scroll offset for the wave list (not yet wired to user input). */
    private int waveScroll  = 0;
    private int arenaScroll = 0;

    /** Counts ticks so data is re-requested from the server every 40 ticks (~2 seconds). */
    private int tickCounter = 0;

    // ── Constructor ───────────────────────────────────────────────────────────

    public MobArenaScreen(Component title) {
        super(title);
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    /**
     * Called once when the screen first opens.
     * Requests fresh arena data from the server, then builds the initial widgets.
     */

    @Override
    protected void init() {
        ClientPlayNetworking.send(ArenaActionPayload.requestData());
        rebuildWidgets();
    }

    /**
     * Called every game tick (~20 times per second).
     * While on the OVERVIEW screen, polls the server for updated data every 2 seconds
     * so the running/stopped status stays current without the user doing anything.
     */
    @Override
    public void tick() {
        super.tick();
        if (detailView != DetailView.OVERVIEW
                && detailView != DetailView.DEL_MOB
                && detailView != DetailView.VIEW_MOB) return;
        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            ClientPlayNetworking.send(ArenaActionPayload.requestData());
        }
    }

    /**
     * Called by the networking layer when the server sends back arena data.
     * Updates our local arena list and refreshes the UI if we're on the overview.
     *
     * @param payload the data packet from the server containing all arena entries
     */
    public void receiveData(ArenaDataPayload payload) {
        this.arenas = new ArrayList<>(payload.arenas());
        if (selectedArena >= arenas.size()) selectedArena = arenas.size() - 1;
        if (detailView != DetailView.OVERVIEW
                && detailView != DetailView.DEL_MOB
                && detailView != DetailView.VIEW_MOB) return;
        rebuildWidgets();
    }

    // ── Layout helpers ────────────────────────────────────────────────────────

    // These methods compute the GUI's position and size every time they're called,
    // so the window always stays centered at any screen resolution.

    private int guiLeft()   { return (width - guiWidth()) / 2; }
    private int guiTop()    { return (height - guiHeight()) / 2; }
    private int guiWidth()  { return Math.min(width - 40, 480); }   // max 480px wide
    private int guiHeight() { return Math.min(height - 40, 300); }  // max 300px tall
    private int sidebarX()  { return guiLeft(); }
    private int detailX()   { return guiLeft() + SIDEBAR_W + 1; }   // +1 for the divider line
    private int detailW()   { return guiWidth() - SIDEBAR_W - 1; }

    // Y positions for the ADD_MOB view.
    // These are methods (not constants) because they depend on guiTop(), which is dynamic.

    /** Y coordinate of the mob type text field */
    private int mobFieldY()   { return guiTop() + 30; }

    /** Y coordinate where the autocomplete dropdown starts (just below the text field) */
    private int dropdownY()   { return mobFieldY() + 14; }

    /** Y coordinate of the "Count" label (below the full dropdown area) */
    private int countLabelY() { return dropdownY() + MAX_VISIBLE_SUGGESTIONS * 12 + 6; }

    /** Y coordinate of the count text field */
    private int countFieldY() { return countLabelY() + 10; }

    // ── Widget construction ───────────────────────────────────────────────────

    /**
     * Tears down all existing buttons/fields and rebuilds them from scratch.
     * This is the core UI pattern: whenever state changes, we just rebuild everything.
     * Called on init, on every view switch, and on every data refresh.
     */
    @Override
    protected void rebuildWidgets() {
        clearWidgets(); // remove all previously registered buttons/fields

        // Theme toggle
        addRenderableWidget(Button.builder(
                Component.literal(darkMode ? "☀ Light" : "☾ Dark"),
                btn -> {
                    darkMode = !darkMode;
                    saveDarkMode(darkMode);
                    rebuildWidgets();
                }
        ).bounds(sidebarX(), guiTop() - 14, SIDEBAR_W, 12).build());

        // Reset autocomplete state whenever we leave the ADD_MOB view
        if (detailView != DetailView.ADD_MOB) {
            mobSuggestions = new ArrayList<>();
            selectedSuggestion = 0;
            suggestionScrollOffset = 0;
        }

        // The sidebar is always visible regardless of which detail view is active
        buildSidebarButtons();

        // Build the appropriate right-panel content based on current state
        if (showPlayerPicker) {
            buildPlayerPickerButtons();
        } else if (detailView == DetailView.ADD_ARENA) {
            buildAddArenaWidgets();
        } else if (detailView == DetailView.VIEW_MOB) {
            buildViewMobWidgets();
        } else if (detailView == DetailView.ADD_MOB) {
            buildAddMobWidgets();
        } else if (detailView == DetailView.DEL_MOB) {
            buildDelMobWidgets();
        } else if (detailView == DetailView.EDIT_ARENA) {
            buildEditArenaWidgets();
        } else {
            buildDetailButtons(); // OVERVIEW: show arena details + wave list
        }
    }

    /**
     * Builds the left sidebar: one button per arena, plus a "New Arena" button at the bottom.
     * Clicking an arena button selects it and switches the detail panel to OVERVIEW.
     */
    private void buildSidebarButtons() {
        int x = sidebarX() + PANEL_PAD;
        int y = guiTop() + 24;
        int maxVisible = (guiHeight() - 40) / ROW_H;

        for (int i = arenaScroll; i < Math.min(arenas.size(), arenaScroll + maxVisible); i++) {
            final int idx = i;
            String label = truncate(arenas.get(i).name());
            addRenderableWidget(Button.builder(Component.literal(label),
                    btn -> {
                        selectedArena = idx;
                        selectedWave = -1;
                        waveScroll = 0;
                        detailView = DetailView.OVERVIEW;
                        rebuildWidgets();
                    }
            ).bounds(x, y + (i - arenaScroll) * ROW_H, SIDEBAR_W - PANEL_PAD * 2, BTN_H).build());
        }

        addRenderableWidget(Button.builder(Component.literal("+ New Arena"),
                btn -> {
                    detailView = DetailView.ADD_ARENA;
                    selectedArena = -1;
                    rebuildWidgets();
                }
        ).bounds(x, guiTop() + guiHeight() - BTN_H - PANEL_PAD, SIDEBAR_W - PANEL_PAD * 2, BTN_H).build());
    }

    /**
     * Builds the OVERVIEW detail panel for a selected arena.
     * Shows Start/Stop, Edit, Add Wave, Delete Arena buttons,
     * and per-wave "Add Mobs" / "Delete Wave" buttons.
     */
    private void buildDetailButtons() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);

        int bx = detailX() + PANEL_PAD;
        int by = guiTop() + guiHeight() - BTN_H - PANEL_PAD; // bottom action row

        // Start/Stop toggles based on whether the arena is currently running
        if (!arena.running()) {
            addRenderableWidget(Button.builder(Component.literal("▶ Start Arena"),
                    btn -> { showPlayerPicker = true; rebuildWidgets(); }
            ).bounds(bx, by, 80, BTN_H).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("■ Stop Arena"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.stopArena(arena.name()))
            ).bounds(bx, by, 80, BTN_H).build());
        }

        // Edit button in the top-right corner of the detail panel
        addRenderableWidget(Button.builder(Component.literal("✎ Edit Arena"),
                btn -> { detailView = DetailView.EDIT_ARENA; rebuildWidgets(); }
        ).bounds(detailX() + detailW() - PANEL_PAD - 70, guiTop() + 18, 70, 14).build());

        // Add a new (empty) wave to this arena
        addRenderableWidget(Button.builder(Component.literal("+ Wave"),
                btn -> ClientPlayNetworking.send(ArenaActionPayload.addWave(arena.name()))
        ).bounds(bx + 84, by, 50, BTN_H).build());

        // Delete this entire arena and return to the "no selection" state
        addRenderableWidget(Button.builder(Component.literal("✕ Delete Arena"),
                btn -> {
                    ClientPlayNetworking.send(ArenaActionPayload.removeArena(arena.name()));
                    selectedArena = -1;
                    rebuildWidgets();
                }
        ).bounds(detailX() + detailW() - PANEL_PAD - 80, by, 80, BTN_H).build());

        // Per-wave action buttons — one pair of buttons per visible wave row
        int waveAreaY = guiTop() + 56;
        int maxWaves = (guiHeight() - 76) / ROW_H;

        for (int i = waveScroll; i < Math.min(arena.waves().size(), waveScroll + maxWaves); i++) {
            ArenaDataPayload.WaveEntry wave = arena.waves().get(i);
            int wy = waveAreaY + (i - waveScroll) * ROW_H;
            final int waveNum = wave.waveNumber();

            int totalBtnWidth = 68 + 4 + 68 + 4 + 68 + 4 + 80; // View + Add + Del + Delete = 296
            int btnStart = detailX() + detailW() - PANEL_PAD - totalBtnWidth;

            addRenderableWidget(Button.builder(Component.literal("View Mobs"),
                    btn -> { selectedWave = waveNum; detailView = DetailView.VIEW_MOB; rebuildWidgets(); }
            ).bounds(btnStart, wy + 1, 68, 16).build());

            addRenderableWidget(Button.builder(Component.literal("+ Add Mobs"),
                    btn -> { selectedWave = waveNum; detailView = DetailView.ADD_MOB; rebuildWidgets(); }
            ).bounds(btnStart + 72, wy + 1, 68, 16).build());

            addRenderableWidget(Button.builder(Component.literal("- Del Mobs"),
                    btn -> { selectedWave = waveNum; detailView = DetailView.DEL_MOB; rebuildWidgets(); }
            ).bounds(btnStart + 144, wy + 1, 68, 16).build());

            addRenderableWidget(Button.builder(Component.literal("✕ Delete Wave"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeWave(arena.name(), waveNum))
            ).bounds(btnStart + 216, wy + 1, 80, 16).build());
        }
    }

    /**
     * Builds the ADD_ARENA form: name, X/Y/Z position, radius, delay, and Create/Cancel buttons.
     */
    private void buildAddArenaWidgets() {
        int cx = detailX() + PANEL_PAD;
        int cy = guiTop() + 20;
        int fw = detailW() - PANEL_PAD * 2;
        int by = guiTop() + guiHeight() - BTN_H - PANEL_PAD;
        int posW = (fw - 54) / 3 - 2;

        nameField = makeField(cx,cy + 10, fw, 14, "Arena name");
        xField = makeField(cx,cy + 50, posW, 14, "X");
        yField = makeField(cx + posW + 1,cy + 50, posW, 14, "Y");
        zField = makeField(cx + (posW + 1) * 2,cy + 50, posW, 14, "Z");
        radiusField = makeField(cx,cy + 90, fw / 2 - 2, 14, "Radius");
        delayField = makeField(cx + fw / 2 + 2,cy + 90, fw / 2 - 2, 14, "Delay (s)");

        addRenderableWidget(Button.builder(Component.literal("My Pos"),
                btn -> {
                    var player = Minecraft.getInstance().player;
                    if (player != null) {
                        xField.setValue(String.valueOf((int) player.getX()));
                        yField.setValue(String.valueOf((int) player.getY()));
                        zField.setValue(String.valueOf((int) player.getZ()));
                    }
                }
        ).bounds(detailX() + detailW() - PANEL_PAD - 50, cy + 50, 50, BTN_H - 2).build());

        addRenderableWidget(Button.builder(Component.literal("✔ Create"), btn -> submitCreateArena())
                .bounds(cx, by, 60, BTN_H).build());
        addRenderableWidget(Button.builder(Component.literal("✕ Cancel"),
                        btn -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); })
                .bounds(cx + 64, by, 50, BTN_H).build());
    }

    /**
     * Builds the EDIT_ARENA form: same fields as ADD_ARENA and pre-populated with the current arena's values.
     */
    private void buildEditArenaWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);

        int cx = detailX() + PANEL_PAD;
        int cy = guiTop() + 20;
        int fw = detailW() - PANEL_PAD * 2;
        int by = guiTop() + guiHeight() - BTN_H - PANEL_PAD;
        int posW = (fw - 54) / 3 - 2;

        nameField = makeField(cx,cy + 10, fw, 14, "Arena name");
        xField = makeField(cx,cy + 50, posW, 14, "X");
        yField = makeField(cx + posW + 1,cy + 50, posW, 14, "Y");
        zField = makeField(cx + (posW + 1) * 2,cy + 50, posW, 14, "Z");
        radiusField = makeField(cx,cy + 90, fw / 2 - 2, 14, "Radius");
        delayField = makeField(cx + fw / 2 + 2,cy + 90, fw / 2 - 2, 14, "Delay (s)");

        addRenderableWidget(Button.builder(Component.literal("My Pos"),
                btn -> {
                    var player = Minecraft.getInstance().player;
                    if (player != null) {
                        xField.setValue(String.valueOf((int) player.getX()));
                        yField.setValue(String.valueOf((int) player.getY()));
                        zField.setValue(String.valueOf((int) player.getZ()));
                    }
                }
        ).bounds(detailX() + detailW() - PANEL_PAD - 50, cy + 50, 50, BTN_H - 2).build());

        // Pre-fill all fields with the arena's current values
        nameField.setValue(arena.name());
        xField.setValue(String.valueOf((int) arena.x()));
        yField.setValue(String.valueOf((int) arena.y()));
        zField.setValue(String.valueOf((int) arena.z()));
        radiusField.setValue(String.valueOf(arena.radius()));
        delayField.setValue(String.valueOf(arena.delaySeconds()));

        addRenderableWidget(Button.builder(Component.literal("✔ Save"), btn -> submitEditArena())
                .bounds(cx, by, 50, BTN_H).build());
        addRenderableWidget(Button.builder(Component.literal("✕ Cancel"),
                        btn -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); })
                .bounds(cx + 54, by, 50, BTN_H).build());
    }

    private void buildViewMobWidgets() {

        int cx = detailX() + PANEL_PAD;
        int by = guiTop() + guiHeight() - BTN_H - PANEL_PAD;

        addRenderableWidget(Button.builder(Component.literal("< Back"),
                        btn -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); })
                .bounds(cx, by, 50, BTN_H).build());
    }

    /**
     * Builds the ADD_MOB form: a mob type field with live autocomplete, a count field,
     * and Add/Cancel buttons.
     *
     * The autocomplete dropdown is NOT a registered widget — it's drawn manually in
     * renderDropdown() so it can appear on top of the count field below it.
     */
    private void buildAddMobWidgets() {

        int cx = detailX() + PANEL_PAD;
        int fw = detailW() - PANEL_PAD * 2;
        int by = guiTop() + guiHeight() - BTN_H - PANEL_PAD;

        mobTypeField = makeField(cx, mobFieldY(), fw, 14, "Mob type (e.g. minecraft:zombie)");

        // setResponder fires on every keystroke — used to filter the suggestion list
        mobTypeField.setResponder(text -> {
            selectedSuggestion = 0;
            suggestionScrollOffset = 0;
            if (text.isEmpty()) {
                mobSuggestions = new ArrayList<>();
                mobTypeField.setSuggestion(null); // clear the greyed-out suffix hint
            } else {
                // Filter the full entity registry down to IDs that contain the typed text
                mobSuggestions = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                        .keySet().stream()
                        .map(Object::toString)
                        .filter(s -> s.contains(text.toLowerCase()))
                        .sorted()
                        .toList();

                // Show a greyed-out completion suffix in the text field for the top match
                if (!mobSuggestions.isEmpty()) {
                    String first = mobSuggestions.get(0);
                    // Only append the suffix if the suggestion actually starts with what was typed
                    mobTypeField.setSuggestion(first.startsWith(text) ? first.substring(text.length()) : null);
                } else {
                    mobTypeField.setSuggestion(null);
                }
            }
        });

        // Count field sits below the dropdown area.
        // The dropdown visually overlays it, drawn last in extractRenderState().
        mobCountField = makeField(cx, countFieldY(), fw / 3, 14, "Count");

        addRenderableWidget(Button.builder(Component.literal("✔ Add"), btn -> submitAddMob())
                .bounds(cx, by, 50, BTN_H).build());
        addRenderableWidget(Button.builder(Component.literal("✕ Cancel"),
                        btn -> {
                            detailView = DetailView.OVERVIEW;
                            mobSuggestions = new ArrayList<>();
                            if (mobTypeField != null) mobTypeField.setSuggestion(null);
                            rebuildWidgets();
                        })
                .bounds(cx + 54, by, 50, BTN_H).build());
    }

    private void buildDelMobWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
        ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
        if (wave == null) return;

        int cx = detailX() + PANEL_PAD;
        int cy = guiTop() + 36;
        int by = guiTop() + guiHeight() - BTN_H - PANEL_PAD;

        for (int i = 0; i < wave.mobs().size(); i++) {
            ArenaDataPayload.MobEntry mob = wave.mobs().get(i);
            int ry = cy + i * ROW_H;

            addRenderableWidget(Button.builder(Component.literal("-1"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(
                            arena.name(), selectedWave, mob.mobType(), 1))
            ).bounds(cx, ry, 24, BTN_H).build());

            addRenderableWidget(Button.builder(Component.literal("-5"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(
                            arena.name(), selectedWave, mob.mobType(), 5))
            ).bounds(cx + 28, ry, 24, BTN_H).build());

            addRenderableWidget(Button.builder(Component.literal("-10"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(
                            arena.name(), selectedWave, mob.mobType(), 10))
            ).bounds(cx + 56, ry, 28, BTN_H).build());

            addRenderableWidget(Button.builder(Component.literal("-20"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(
                            arena.name(), selectedWave, mob.mobType(), 20))
            ).bounds(cx + 88, ry, 28, BTN_H).build());

            addRenderableWidget(Button.builder(Component.literal("✕ All"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(
                            arena.name(), selectedWave, mob.mobType(), mob.count()))
            ).bounds(cx + 120, ry, 36, BTN_H).build());
        }

        addRenderableWidget(Button.builder(Component.literal("< Back"),
                        btn -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); })
                .bounds(cx, by, 50, BTN_H).build());
    }

    /**
     * Builds the player picker overlay shown before starting an arena.
     * Lists all currently online players as checkboxes (simulated with ☑/☐ labels).
     * Confirm sends the start packet; Cancel returns to the overview.
     */
    private void buildPlayerPickerButtons() {
        // Fetch the current online player list from the client's connection
        List<String> online = new ArrayList<>();
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            conn.getOnlinePlayers().forEach(p -> online.add(p.getProfile().name()));
        }

        int cx = detailX() + PANEL_PAD;
        int cy = guiTop() + 36;
        int fw = detailW() - PANEL_PAD * 2;

        // Show up to 8 players; each button toggles that player in/out of selectedPlayers
        for (int i = 0; i < Math.min(online.size(), 8); i++) {
            final String pName = online.get(i);
            boolean sel = selectedPlayers.contains(pName);
            addRenderableWidget(Button.builder(
                    Component.literal((sel ? "☑ " : "☐ ") + pName),
                    btn -> {
                        // Toggle selection and rebuild so the checkbox label updates
                        if (selectedPlayers.contains(pName)) selectedPlayers.remove(pName);
                        else selectedPlayers.add(pName);
                        rebuildWidgets();
                    }
            ).bounds(cx, cy + i * ROW_H, fw - 74, BTN_H).build());
        }

        int by = guiTop() + guiHeight() - BTN_H - PANEL_PAD;

        boolean allSelected = !online.isEmpty() && new HashSet<>(selectedPlayers).containsAll(online);
        addRenderableWidget(Button.builder(
                Component.literal(allSelected ? "☐ Deselect All" : "☑ Select All"),
                btn -> {
                    if (allSelected) selectedPlayers.clear();
                    else { selectedPlayers.clear(); selectedPlayers.addAll(online); }
                    rebuildWidgets();
                }
        ).bounds(detailX() + PANEL_PAD, by, 80, BTN_H).build());

        addRenderableWidget(Button.builder(Component.literal("✕ Cancel"),
                btn -> { showPlayerPicker = false; selectedPlayers.clear(); rebuildWidgets(); }
        ).bounds(detailX() + detailW() - PANEL_PAD - 134, by, 50, BTN_H).build());

        addRenderableWidget(Button.builder(Component.literal("▶ Confirm"),
                btn -> {
                    // Only start if at least one player is selected and an arena is chosen
                    if (!selectedPlayers.isEmpty() && selectedArena >= 0) {
                        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
                        ClientPlayNetworking.send(ArenaActionPayload.startArena(arena.name(), selectedPlayers));
                    }
                    showPlayerPicker = false;
                    rebuildWidgets();
                }
        ).bounds(detailX() + detailW() - PANEL_PAD - 80, by, 80, BTN_H).build());
    }

    // ── Form submission ───────────────────────────────────────────────────────

    /**
     * Reads the ADD_ARENA form fields, validates them, and sends a createArena packet.
     * Silently ignores the submit if any numeric field can't be parsed.
     */
    private void submitCreateArena() {
        try {
            if (nameField == null || xField == null || yField == null ||
                    zField == null || radiusField == null || delayField == null) return;
            String name = nameField.getValue().trim();
            if (name.isEmpty()) return;
            double x = Double.parseDouble(xField.getValue().trim());
            double y = Double.parseDouble(yField.getValue().trim());
            double z = Double.parseDouble(zField.getValue().trim());
            int radius = Integer.parseInt(radiusField.getValue().trim());
            int delay  = Integer.parseInt(delayField.getValue().trim());
            ClientPlayNetworking.send(ArenaActionPayload.createArena(name, x, y, z, radius, delay));
            detailView = DetailView.OVERVIEW;
            rebuildWidgets();
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Reads the EDIT_ARENA form fields and sends an editArena packet to update the server.
     * The arena name cannot be changed here (it's used as the identifier).
     */
    private void submitEditArena() {
        try {
            if (nameField == null || xField == null || yField == null || zField == null ||
                    radiusField == null || delayField == null) return;
            if (selectedArena < 0 || selectedArena >= arenas.size()) return;
            String oldName = arenas.get(selectedArena).name();
            String newName = nameField.getValue().trim();
            if (newName.isEmpty()) return;
            double x = Double.parseDouble(xField.getValue().trim());
            double y = Double.parseDouble(yField.getValue().trim());
            double z = Double.parseDouble(zField.getValue().trim());
            int radius = Integer.parseInt(radiusField.getValue().trim());
            int delay  = Integer.parseInt(delayField.getValue().trim());
            ClientPlayNetworking.send(ArenaActionPayload.editArena(oldName, newName, x, y, z, radius, delay));
            detailView = DetailView.OVERVIEW;
            rebuildWidgets();
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Reads the ADD_MOB form fields and sends an addMob packet for the selected wave.
     * Requires a non-empty mob type, a count of at least 1, and a selected arena+wave.
     */
    private void submitAddMob() {
        try {
            if (mobTypeField == null || mobCountField == null) return;
            String mob = mobTypeField.getValue().trim();
            int count = Integer.parseInt(mobCountField.getValue().trim());
            if (mob.isEmpty() || count < 1 || selectedArena < 0 || selectedArena >= arenas.size()) return;
            String arenaName = arenas.get(selectedArena).name();
            ClientPlayNetworking.send(ArenaActionPayload.addMob(arenaName, selectedWave, mob, count));
            detailView = DetailView.OVERVIEW;
            rebuildWidgets();
        } catch (NumberFormatException ignored) {}
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    /**
     * Main render method called every frame.
     *
     * Draw order matters here:
     *   1. Static backgrounds and text (renderDetailPanelBase)
     *   2. Registered widgets (buttons, text fields) via super.extractRenderState()
     *   3. The autocomplete dropdown (renderDropdown) — drawn LAST so it overlays widgets
     */
    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int gl = guiLeft(), gt = guiTop(), gw = guiWidth(), gh = guiHeight();

        // ── Draw the chrome / shell ──
        g.fill(gl, gt, gl + gw, gt + gh, colBg());                                     // overall background
        g.fill(sidebarX(), gt, sidebarX() + SIDEBAR_W, gt + gh, colSidebar());          // sidebar tint
        g.fill(sidebarX() + SIDEBAR_W, gt, sidebarX() + SIDEBAR_W + 1, gt + gh, colBorder()); // 1px divider
        g.fill(detailX(), gt, detailX() + detailW(), gt + gh, colPanel());              // detail panel
        g.outline(gl, gt, gw, gh, colBorder());                                        // outer border
        g.fill(sidebarX(), gt, sidebarX() + SIDEBAR_W, gt + 16, 0xFF888888);           // sidebar header bar
        g.text(font, "Arenas", sidebarX() + PANEL_PAD, gt + 4, 0xFFFFFFFF, false);   // "Arenas" label

        // ── Draw the detail panel content (text, wave rows, labels) ──
        renderDetailPanelBase(g);

        // ── Draw registered widgets on top ──
        super.extractRenderState(g, mx, my, delta);

        // ── Draw the autocomplete dropdown on top of everything ──
        renderDropdown(g);
    }

    /**
     * Draws all static text, labels, and wave rows in the detail panel.
     * Does NOT draw the autocomplete dropdown (that comes after widgets).
     */
    private void renderDetailPanelBase(GuiGraphicsExtractor g) {
        int dx = detailX();
        int dt = guiTop();
        int dw = detailW();

        // ── Player picker overlay ──
        if (showPlayerPicker) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "Select Players to Start Arena", dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);
            return;
        }

        // ── ADD_ARENA form labels ──
        if (detailView == DetailView.ADD_ARENA) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "New Arena", dx + PANEL_PAD, dt + 4,   0xFFFFFFFF, false);
            g.text(font, "Name", dx + PANEL_PAD, dt + 20, colSidebar(), false);
            g.text(font, "Position", dx + PANEL_PAD, dt + 60, colSidebar(), false);
            g.text(font, "Radius / Delay (s)", dx + PANEL_PAD, dt + 100, colSidebar(), false);
            return;
        }

        // ── EDIT_ARENA form labels ──
        if (detailView == DetailView.EDIT_ARENA) {
            if (selectedArena < 0 || selectedArena >= arenas.size()) return;
            ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "Edit Arena: " + arena.name(), dx + PANEL_PAD, dt + 4,  0xFFFFFFFF, false);
            g.text(font, "Name", dx + PANEL_PAD, dt + 20, colSidebar(), false);
            g.text(font, "Position", dx + PANEL_PAD, dt + 60, colSidebar(), false);
            g.text(font, "Radius / Delay (s)", dx + PANEL_PAD, dt + 100, colSidebar(), false);
            return;
        }

        if (detailView == DetailView.VIEW_MOB) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "Mobs in Wave " + selectedWave, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

            if (selectedArena >= 0 && selectedArena < arenas.size()) {
                ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
                ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                        .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
                if (wave != null && !wave.mobs().isEmpty()) {
                    int cy = guiTop() + 24;
                    for (int i = 0; i < wave.mobs().size(); i++) {
                        ArenaDataPayload.MobEntry mob = wave.mobs().get(i);
                        String mobName = mob.mobType().replace("minecraft:", "");
                        String display = Character.toUpperCase(mobName.charAt(0)) + mobName.substring(1).replace("_", " ");
                        if (i % 2 == 0) g.fill(dx, cy + i * ROW_H, dx + dw, cy + i * ROW_H + ROW_H, 0x11000000);
                        g.text(font, mob.count() + "x  " + display,
                                dx + PANEL_PAD, cy + i * ROW_H + 4, colText(), false);
                    }
                } else {
                    g.text(font, "No mobs in this wave.", dx + PANEL_PAD, guiTop() + 28, colSubtext(), false);
                }
            }
            return;
        }

        // ── ADD_MOB form labels ──
        if (detailView == DetailView.ADD_MOB) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "Add Mob(s) to Wave " + selectedWave, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);
            g.text(font, "Mob Type", dx + PANEL_PAD, mobFieldY() - 10, colSubtext(), false);
            g.text(font, "Count", dx + PANEL_PAD, countLabelY(), colSubtext(), false);
            return;
        }

        if (detailView == DetailView.DEL_MOB) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFFAA3333);
            g.text(font, "Remove Mobs from Wave " + selectedWave, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

            if (selectedArena >= 0 && selectedArena < arenas.size()) {
                ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
                ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                        .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
                if (wave != null) {
                    int cy = guiTop() + 36;
                    for (int i = 0; i < wave.mobs().size(); i++) {
                        ArenaDataPayload.MobEntry mob = wave.mobs().get(i);
                        int ry = cy + i * ROW_H;
                        // Capitalize mob name and format nicely
                        String mobName = mob.mobType().replace("minecraft:", "");
                        String display = Character.toUpperCase(mobName.charAt(0)) + mobName.substring(1).replace("_", " ");
                        g.text(font, mob.count() + "x  " + display,
                                dx + PANEL_PAD + 162, ry + 4, colText(), false);
                    }
                    if (wave.mobs().isEmpty()) {
                        g.text(font, "No mobs in this wave.", dx + PANEL_PAD, guiTop() + 36, colSubtext(), false);
                    }
                }
            }
            return;
        }

        // ── OVERVIEW: no arena selected ──
        if (selectedArena < 0 || selectedArena >= arenas.size()) {
            g.text(font, "Select an arena or create a new one.", dx + PANEL_PAD, dt + 24, colSubtext(), false);
            return;
        }

        // ── OVERVIEW: arena selected ──
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);

        // Header bar: green if running, red if stopped
        int headerColor = arena.running() ? 0xFF43A047 : 0xFFCC2222;
        g.fill(dx, dt, dx + dw, dt + 16, headerColor);
        g.text(font, arena.name(), dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);
        String status = arena.running() ? "● RUNNING" : "● STOPPED";
        // Right-align the status string inside the header
        g.text(font, status, dx + dw - font.width(status) - PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

        // Arena position and config summary
        g.text(font, "X:" + (int)arena.x() + " Y:" + (int)arena.y() + " Z:" + (int)arena.z(),
                dx + PANEL_PAD, dt + 20, colSubtext(), false);
        g.text(font, "Radius: " + arena.radius() + "  Delay: " + arena.delaySeconds() + "s",
                dx + PANEL_PAD, dt + 30, colSubtext(), false);

        // "Waves (N)" section header
        g.fill(dx, dt + 42, dx + dw - 3, dt + 54, darkMode ? 0xFF313244 : 0xFFEEEEEE);
        g.text(font, "Waves (" + arena.waves().size() + ")", dx + PANEL_PAD, dt + 45, colHeader(), false);

        // Wave list rows
        int waveAreaY = guiTop() + 56;
        int maxWaves = (guiHeight() - 76) / ROW_H;
        for (int i = waveScroll; i < Math.min(arena.waves().size(), waveScroll + maxWaves); i++) {
            ArenaDataPayload.WaveEntry wave = arena.waves().get(i);
            int ry = waveAreaY + (i - waveScroll) * ROW_H;

            // Alternate row shading for readability
            if (i % 2 == 0) g.fill(dx, ry, dx + dw - 3, ry + ROW_H, darkMode ? 0x15FFFFFF : 0x11000000);
            g.text(font, "Wave " + wave.waveNumber(), dx + PANEL_PAD, ry + 4, colText(), false);

        }
    }

    /**
     * Draws the mob type autocomplete dropdown.
     * Called last in extractRenderState() so it visually sits on top of all widgets,
     * including the count field that lives below the dropdown area.
     *
     * Each row highlights the portion of the suggestion that matches the typed text.
     */
    private void renderDropdown(GuiGraphicsExtractor g) {
        if (detailView != DetailView.ADD_MOB) return;
        if (mobSuggestions.isEmpty() || mobTypeField == null) return;

        int sx = detailX() + PANEL_PAD;
        int sy = dropdownY();
        int sw = detailW() - PANEL_PAD * 2;
        int visible = Math.min(MAX_VISIBLE_SUGGESTIONS, mobSuggestions.size());

        // Dark background for the whole dropdown area
        g.fill(sx, sy, sx + sw, sy + visible * 12, 0xFF333333);

        for (int i = 0; i < visible; i++) {
            int idx = i + suggestionScrollOffset; // account for scroll
            if (idx >= mobSuggestions.size()) break;
            int rowY = sy + i * 12;

            // Highlight the currently selected row
            if (idx == selectedSuggestion) {
                g.fill(sx, rowY, sx + sw, rowY + 12, 0xFF444488);
            }

            // Draw the suggestion with the matched portion highlighted in yellow
            String suggestion = mobSuggestions.get(idx);
            String typed = mobTypeField.getValue();
            int matchStart = suggestion.toLowerCase().indexOf(typed.toLowerCase());

            if (matchStart >= 0) {
                // Split the suggestion into three parts: before match, matched, after match
                String before  = suggestion.substring(0, matchStart);
                String matched = suggestion.substring(matchStart, matchStart + typed.length());
                String after   = suggestion.substring(matchStart + typed.length());
                int x1 = sx + 2;
                int x2 = x1 + font.width(before);  // start of the highlighted segment
                int x3 = x2 + font.width(matched); // start of the trailing segment
                g.text(font, before,  x1, rowY + 2, 0xFFAAAAAA, false); // grey
                g.text(font, matched, x2, rowY + 2, 0xFFFFFF55, false); // yellow highlight
                g.text(font, after,   x3, rowY + 2, 0xFFAAAAAA, false); // grey
            } else {
                // Fallback: whole suggestion in one color (white if selected, gray otherwise)
                g.text(font, suggestion, sx + 2, rowY + 2,
                        idx == selectedSuggestion ? 0xFFFFFFFF : 0xFFAAAAAA, false);
            }
        }
    }

    // ── Input handling ────────────────────────────────────────────────────────

    /**
     * Intercepts Tab (cycle through suggestions) and Enter (confirm selection)
     * when the autocomplete dropdown is visible. All other keys fall through to
     * the default handler (which routes them to the focused EditBox).
     */
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (detailView == DetailView.ADD_MOB && !mobSuggestions.isEmpty()) {
            if (event.isDown()) {
                selectedSuggestion = (selectedSuggestion + 1) % mobSuggestions.size();
                if (selectedSuggestion >= suggestionScrollOffset + MAX_VISIBLE_SUGGESTIONS) {
                    suggestionScrollOffset = selectedSuggestion - MAX_VISIBLE_SUGGESTIONS + 1;
                }
                updateSuggestionSuffix();
                return true;
            }
            if (event.isUp()) {
                selectedSuggestion = (selectedSuggestion - 1 + mobSuggestions.size()) % mobSuggestions.size();
                if (selectedSuggestion < suggestionScrollOffset) {
                    suggestionScrollOffset = selectedSuggestion;
                }
                updateSuggestionSuffix();
                return true;
            }
            if (event.isConfirmation()) {
                applySuggestion();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    /**
     * Handles mouse clicks on the autocomplete dropdown.
     * Checks if the click landed inside the dropdown rectangle before falling through
     * to the normal widget click handler.
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (detailView == DetailView.ADD_MOB && !mobSuggestions.isEmpty() && mobTypeField != null) {
            int sx = detailX() + PANEL_PAD;
            int sy = dropdownY();
            int sw = detailW() - PANEL_PAD * 2;
            int visible = Math.min(MAX_VISIBLE_SUGGESTIONS, mobSuggestions.size());
            int mx = (int) event.x();
            int my = (int) event.y();

            if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + visible * 12) {
                // Convert pixel Y to row index (accounting for scroll offset)
                int clicked = (my - sy) / 12 + suggestionScrollOffset;
                if (clicked >= 0 && clicked < mobSuggestions.size()) {
                    selectedSuggestion = clicked;
                    applySuggestion();
                    return true; // consume — don't propagate to widgets underneath
                }
            }
        }
        return super.mouseClicked(event, bl);
    }

    /**
     * Handles scroll wheel events over the autocomplete dropdown.
     * Scrolls the visible window of suggestions up/down.
     */
    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        // Autocomplete dropdown scroll
        if (detailView == DetailView.ADD_MOB && !mobSuggestions.isEmpty()) {
            int sx = detailX() + PANEL_PAD;
            int sy = dropdownY();
            int sw = detailW() - PANEL_PAD * 2;
            int visible = Math.min(MAX_VISIBLE_SUGGESTIONS, mobSuggestions.size());
            if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + visible * 12) {
                suggestionScrollOffset = (int) Math.max(0,
                        Math.min(suggestionScrollOffset - vertical,
                                mobSuggestions.size() - MAX_VISIBLE_SUGGESTIONS));
                return true;
            }
        }

        // Sidebar arena list scroll
        int maxVisible = (guiHeight() - 40) / ROW_H;
        if (mx >= sidebarX() && mx <= sidebarX() + SIDEBAR_W
                && my >= guiTop() && my <= guiTop() + guiHeight()) {
            arenaScroll = (int) Math.max(0,
                    Math.min(arenaScroll - vertical, Math.max(0, arenas.size() - maxVisible)));
            rebuildWidgets();
            return true;
        }

        // Wave list scroll
        if (detailView == DetailView.OVERVIEW
                && mx >= detailX() && mx <= detailX() + detailW()
                && my >= guiTop() && my <= guiTop() + guiHeight()) {
            if (selectedArena >= 0 && selectedArena < arenas.size()) {
                int waveCount = arenas.get(selectedArena).waves().size();
                int maxWaves = (guiHeight() - 76) / ROW_H;
                waveScroll = (int) Math.max(0,
                        Math.min(waveScroll - vertical, Math.max(0, waveCount - maxWaves)));
                rebuildWidgets();
            }
        }

        return super.mouseScrolled(mx, my, horizontal, vertical);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final java.io.File PREFS_FILE = new java.io.File(
            net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().toFile(),
            "succorstadiums/gui_prefs.json"
    );

    private static boolean loadDarkMode() {
        try {
            if (!PREFS_FILE.exists()) return false;
            String json = new String(java.nio.file.Files.readAllBytes(PREFS_FILE.toPath()));
            return json.contains("\"darkMode\":true");
        } catch (Exception e) {
            return false;
        }
    }

    private static void saveDarkMode(boolean darkMode) {
        try {
            PREFS_FILE.getParentFile().mkdirs();
            java.nio.file.Files.writeString(PREFS_FILE.toPath(), "{\"darkMode\":" + darkMode + "}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the currently highlighted suggestion into the mob type field,
     * then clears the dropdown entirely.
     */
    private void applySuggestion() {
        if (selectedSuggestion >= 0 && selectedSuggestion < mobSuggestions.size() && mobTypeField != null) {
            mobTypeField.setValue(mobSuggestions.get(selectedSuggestion));
            mobTypeField.setSuggestion(null); // remove greyed-out suffix
            mobSuggestions = new ArrayList<>();
            selectedSuggestion = 0;
            suggestionScrollOffset = 0;
        }
    }

    /**
     * Updates the greyed-out suffix hint in the mob type field to match
     * whichever suggestion is currently highlighted (e.g. user pressed Tab).
     */
    private void updateSuggestionSuffix() {
        if (mobTypeField == null || mobSuggestions.isEmpty()) return;
        String selected = mobSuggestions.get(selectedSuggestion);
        String typed = mobTypeField.getValue();
        // Only show the suffix if the suggestion actually starts with what's typed
        mobTypeField.setSuggestion(selected.startsWith(typed) ? selected.substring(typed.length()) : null);
    }

    /**
     * Creates an EditBox, sets its hint text, and registers it as a renderable widget.
     *
     * @param x    left edge
     * @param y    top edge
     * @param w    width
     * @param h    height
     * @param hint placeholder text shown when the field is empty
     * @return the created and registered EditBox
     */
    private EditBox makeField(int x, int y, int w, int h, String hint) {
        EditBox field = new EditBox(font, x, y, w, h, Component.literal(hint));
        field.setHint(Component.literal(hint));
        field.setBordered(true);
        addRenderableWidget(field);
        return field;
    }

    /**
     * Clips a string to `max` characters, appending "…" if it was truncated.
     */
    private static String truncate(String s) {

        return s.length() <= 11 ? s : s.substring(0, 11 - 1) + "...";
    }

    /**
     * Returning false means the game keeps running while this screen is open
     * (the world doesn't pause). Appropriate for a management GUI used in multiplayer.
     */
    @Override
    public boolean isPauseScreen() {
        return false; }
}