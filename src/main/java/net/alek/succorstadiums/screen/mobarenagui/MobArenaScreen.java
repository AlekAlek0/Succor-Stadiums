package net.alek.succorstadiums.screen.mobarenagui;

import net.alek.succorstadiums.client.ModKeyBindings;
import net.alek.succorstadiums.network.ArenaActionPayload;
import net.alek.succorstadiums.network.ArenaDataPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

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
 *
 * All detail-view panels have been extracted into their own classes
 * (ArenaFormScreen, MobViewScreen, DelMobScreen, PlayerPickerScreen,
 * AddMobScreen). This class wires them together: it owns arena/wave
 * selection state and forwards build/render/input calls to whichever
 * panel is active.
 */
public class MobArenaScreen extends Screen {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ── Layout constants ─────────────────────────────────────────────────────

    private static final int SIDEBAR_W        = 110;
    private static final int PANEL_PAD        = 8;
    private static final int BTN_H            = 16;
    private static final int ROW_H            = 18;

    // ── Screens ─────────────────────────────────────────────────────

    private final MobViewScreen mobViewScreen = new MobViewScreen();
    private final ArenaFormScreen arenaFormScreen = new ArenaFormScreen();
    private final DelMobScreen delMobScreen = new DelMobScreen();
    private final PlayerPickerScreen playerPickerScreen = new PlayerPickerScreen();

    // ── Theme ─────────────────────────────────────────────────────

    private final GuiTheme theme = new GuiTheme();

    // ── UI state ─────────────────────────────────────────────────────────────

    private List<ArenaDataPayload.ArenaEntry> arenas = new ArrayList<>();
    private int selectedArena = -1;
    private int selectedWave  = -1;

    private enum DetailView { OVERVIEW, ADD_ARENA, VIEW_MOB, ADD_MOB, DEL_MOB, EDIT_ARENA }
    private DetailView detailView = DetailView.OVERVIEW;

    // ── ADD_MOB — extracted into its own class; it's a stateful subsystem in
    // its own right (suggestion managers, scroll state, collapsible sections)
    // rather than a simple detail view. ─────────────────────────────────────
    private final AddMobScreen addMobScreen = new AddMobScreen();

    // ── Player picker ─────────────────────────────────────────────────────────

    private boolean showPlayerPicker = false;

    // ── Scroll / tick ─────────────────────────────────────────────────────────

    private int waveScroll  = 0;
    private int arenaScroll = 0;
    private int tickCounter = 0;

    // ── Constructor ───────────────────────────────────────────────────────────

    public MobArenaScreen(Component title) {
        super(title);
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    protected void init() {
        ClientPlayNetworking.send(ArenaActionPayload.requestData());
        rebuildWidgets();
    }

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

    public void receiveData(ArenaDataPayload payload) {
        this.arenas = new ArrayList<>(payload.arenas());
        if (selectedArena >= arenas.size()) selectedArena = arenas.size() - 1;
        if (detailView != DetailView.OVERVIEW
                && detailView != DetailView.DEL_MOB
                && detailView != DetailView.VIEW_MOB) return;
        rebuildWidgets();
    }

    // ── Layout helpers ────────────────────────────────────────────────────────

    private int guiLeft()  { return (width  - guiWidth())  / 2; }
    private int guiTop()   { return (height - guiHeight()) / 2; }
    private int guiWidth() { return Math.min(width  - 40, 960); }
    private int guiHeight(){ return Math.min(height - 40, 600); }
    private int sidebarX() { return guiLeft(); }
    private int detailX()  { return guiLeft() + SIDEBAR_W + 1; }
    private int detailW()  { return guiWidth() - SIDEBAR_W - 1; }
    private int detailH()  { return guiHeight(); }

    // ── Widget construction ───────────────────────────────────────────────────

    @Override
    protected void rebuildWidgets() {
        clearWidgets();

        // Theme toggle
        addRenderableWidget(Button.builder(
                Component.literal("Theme: " + theme.getThemeName()),
                btn -> { theme.nextTheme(); rebuildWidgets(); }
        ).bounds(sidebarX(), guiTop() - 14, SIDEBAR_W, 12).build());

        buildSidebarButtons();

        if (showPlayerPicker)                    buildPlayerPickerButtons();
        else if (detailView == DetailView.ADD_ARENA)  buildAddArenaWidgets();
        else if (detailView == DetailView.VIEW_MOB)   buildViewMobWidgets();
        else if (detailView == DetailView.ADD_MOB)    buildAddMobWidgets();
        else if (detailView == DetailView.DEL_MOB)    buildDelMobWidgets();
        else if (detailView == DetailView.EDIT_ARENA) buildEditArenaWidgets();
        else                                          buildDetailButtons();
    }

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
                        selectedWave  = -1;
                        waveScroll    = 0;
                        detailView    = DetailView.OVERVIEW;
                        rebuildWidgets();
                    }
            ).bounds(x, y + (i - arenaScroll) * ROW_H, SIDEBAR_W - PANEL_PAD * 2, BTN_H).build());
        }

        addRenderableWidget(Button.builder(Component.literal("+ New Arena"),
                btn -> { detailView = DetailView.ADD_ARENA; selectedArena = -1; rebuildWidgets(); }
        ).bounds(x, guiTop() + guiHeight() - BTN_H - PANEL_PAD, SIDEBAR_W - PANEL_PAD * 2, BTN_H).build());
    }

    private void buildDetailButtons() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);

        int bx = detailX() + PANEL_PAD;
        int by = guiTop() + guiHeight() - BTN_H - PANEL_PAD;

        if (!arena.running()) {
            addRenderableWidget(Button.builder(Component.literal("▶ Start Arena"),
                    btn -> { showPlayerPicker = true; rebuildWidgets(); }
            ).bounds(bx, by, 80, BTN_H).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("■ Stop Arena"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.stopArena(arena.name()))
            ).bounds(bx, by, 80, BTN_H).build());
        }

        addRenderableWidget(Button.builder(Component.literal("✎ Edit Arena"),
                btn -> { detailView = DetailView.EDIT_ARENA; rebuildWidgets(); }
        ).bounds(detailX() + detailW() - PANEL_PAD - 70, guiTop() + 18, 70, 14).build());

        addRenderableWidget(Button.builder(Component.literal("+ Wave"),
                btn -> ClientPlayNetworking.send(ArenaActionPayload.addWave(arena.name()))
        ).bounds(bx + 84, by, 50, BTN_H).build());

        addRenderableWidget(Button.builder(Component.literal("✕ Delete Arena"),
                btn -> {
                    ClientPlayNetworking.send(ArenaActionPayload.removeArena(arena.name()));
                    selectedArena = -1;
                    rebuildWidgets();
                }
        ).bounds(detailX() + detailW() - PANEL_PAD - 80, by, 80, BTN_H).build());

        int waveAreaY = guiTop() + 56;
        int maxWaves  = (guiHeight() - 76) / ROW_H;

        for (int i = waveScroll; i < Math.min(arena.waves().size(), waveScroll + maxWaves); i++) {
            ArenaDataPayload.WaveEntry wave = arena.waves().get(i);
            int wy = waveAreaY + (i - waveScroll) * ROW_H;
            final int waveNum = wave.waveNumber();

            int totalBtnWidth = 68 + 4 + 68 + 4 + 68 + 4 + 80;
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

    private void buildAddArenaWidgets() {
        arenaFormScreen.buildWidgets(this::addRenderableWidget, font,
                detailX(), detailW(), guiTop(), guiHeight(), null,
                (name, x, y, z, radius, delay) -> {
                    ClientPlayNetworking.send(ArenaActionPayload.createArena(name, x, y, z, radius, delay));
                    detailView = DetailView.OVERVIEW;
                    rebuildWidgets();
                },
                () -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); });
    }

    private void buildEditArenaWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
        arenaFormScreen.buildWidgets(this::addRenderableWidget, font,
                detailX(), detailW(), guiTop(), guiHeight(), arena,
                (newName, x, y, z, radius, delay) -> {
                    ClientPlayNetworking.send(ArenaActionPayload.editArena(arena.name(), newName, x, y, z, radius, delay));
                    detailView = DetailView.OVERVIEW;
                    rebuildWidgets();
                },
                () -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); });
    }

    private void buildViewMobWidgets() {
        mobViewScreen.buildWidgets(
                this::addRenderableWidget,
                detailX(), guiTop(), guiHeight(), BTN_H,
                () -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); }
        );
    }

    private void buildDelMobWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
        ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);

        delMobScreen.buildWidgets(this::addRenderableWidget,
                detailX(), guiTop(), guiHeight(), wave,
                (mob, count) -> {
                    if (wave != null) {
                        ClientPlayNetworking.send(
                                ArenaActionPayload.removeMob(
                                        arena.name(),
                                        selectedWave,
                                        mob.mobType(),
                                        count,
                                        mob.size(),
                                        mob.ridingMob(),
                                        mob.mainHandItem(),
                                        mob.offHandItem(),
                                        mob.armorItems()
                                )
                        );
                    }
                },
                () -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); });
    }

    private void buildPlayerPickerButtons() {
        playerPickerScreen.buildWidgets(this::addRenderableWidget,
                detailX(), detailW(), guiTop(), guiHeight(),
                this::rebuildWidgets,
                () -> { showPlayerPicker = false; playerPickerScreen.clearSelection(); rebuildWidgets(); },
                () -> {
                    List<String> selected = playerPickerScreen.getSelectedPlayers();
                    if (!selected.isEmpty() && selectedArena >= 0) {
                        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
                        ClientPlayNetworking.send(ArenaActionPayload.startArena(arena.name(), selected));
                    }
                    showPlayerPicker = false;
                    rebuildWidgets();
                });
    }

    private void buildAddMobWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);

        addMobScreen.buildWidgets(
                this::addRenderableWidget,
                font,
                detailX(),
                detailW(),
                guiTop(),
                guiHeight(),
                selectedWave,

                (mobType, count, size, ridingMob, mainHandItem, offHandItem,
                 armorItems, potionEffects, enchantments) -> {

                    ClientPlayNetworking.send(ArenaActionPayload.addMob(
                            arena.name(),
                            selectedWave,
                            mobType,
                            count,
                            size,
                            ridingMob,
                            mainHandItem,
                            offHandItem,
                            armorItems,
                            potionEffects,
                            enchantments
                    ));

                    detailView = DetailView.OVERVIEW;
                    rebuildWidgets();
                },

                () -> {
                    detailView = DetailView.OVERVIEW;
                    addMobScreen.reset();
                    rebuildWidgets();
                },

                this::rebuildWidgets
        );
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int gl = guiLeft(), gt = guiTop(), gw = guiWidth(), gh = guiHeight();

        g.fill(gl, gt, gl + gw, gt + gh, theme.bg());
        g.fill(sidebarX(), gt, sidebarX() + SIDEBAR_W, gt + gh,theme.sidebar());
        g.fill(sidebarX() + SIDEBAR_W, gt, sidebarX() + SIDEBAR_W + 1, gt + gh, theme.border());
        g.fill(detailX(), gt, detailX() + detailW(), gt + gh, theme.panel());
        g.outline(gl, gt, gw, gh, theme.border());
        g.fill(sidebarX(), gt, sidebarX() + SIDEBAR_W, gt + 16, 0xFF888888);
        g.text(font, "Arenas", sidebarX() + PANEL_PAD, gt + 4, 0xFFFFFFFF, false);

        renderDetailPanelBase(g);

        super.extractRenderState(g, mx, my, delta);

        renderDropdown(g);
    }

    private void renderDetailPanelBase(GuiGraphicsExtractor g) {
        int dx = detailX();
        int dt = guiTop();
        int dw = detailW();

        if (showPlayerPicker) {
            playerPickerScreen.renderHeader(g, font, dx, dt, dw);
            return;
        }

        if (detailView == DetailView.ADD_ARENA) {
            arenaFormScreen.render(g, font, theme, dx, dt, "New Arena");
            return;
        }

        if (detailView == DetailView.EDIT_ARENA) {
            if (selectedArena < 0 || selectedArena >= arenas.size()) return;
            ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
            arenaFormScreen.render(g, font, theme, dx, dt, "Edit Arena: " + arena.name());
            return;
        }

        if (detailView == DetailView.VIEW_MOB) {
            ArenaDataPayload.WaveEntry wave = null;
            if (selectedArena >= 0 && selectedArena < arenas.size()) {
                ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
                wave = arena.waves().stream()
                        .filter(w -> w.waveNumber() == selectedWave)
                        .findFirst().orElse(null);
            }
            mobViewScreen.render(g, font, theme, dx, dt, dw, guiTop(), selectedWave, wave);
            return;
        }

        if (detailView == DetailView.ADD_MOB) {
            addMobScreen.render(g, font, theme, dx, dt, dw, guiTop(), guiHeight(), selectedWave);
            return;
        }

        if (detailView == DetailView.DEL_MOB) {
            ArenaDataPayload.WaveEntry wave = null;
            if (selectedArena >= 0 && selectedArena < arenas.size()) {
                ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
                wave = arena.waves().stream()
                        .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
            }
            delMobScreen.render(g, font, theme, dx, dt, dw, guiTop(), selectedWave, wave);
            return;
        }

        if (selectedArena < 0 || selectedArena >= arenas.size()) {
            g.text(font, "Select an arena or create a new one.", dx + PANEL_PAD, dt + 24, theme.subtext(), false);
            return;
        }

        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
        int headerColor = arena.running() ? 0xFF43A047 : 0xFFCC2222;
        g.fill(dx, dt, dx + dw, dt + 16, headerColor);
        g.text(font, arena.name(), dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);
        String status = arena.running() ? "● RUNNING" : "● STOPPED";
        g.text(font, status, dx + dw - font.width(status) - PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

        g.text(font, "X:" + (int)arena.x() + " Y:" + (int)arena.y() + " Z:" + (int)arena.z(),
                dx + PANEL_PAD, dt + 20, theme.subtext(), false);
        g.text(font, "Radius: " + arena.radius() + "  Delay: " + arena.delaySeconds() + "s",
                dx + PANEL_PAD, dt + 30, theme.subtext(), false);

        g.fill(dx, dt + 42, dx + dw - 3, dt + 54, theme.getTheme() != Theme.LIGHT ? 0x15FFFFFF : 0x11000000);
        g.text(font, "Waves (" + arena.waves().size() + ")", dx + PANEL_PAD, dt + 45, theme.header(), false);

        int waveAreaY = guiTop() + 56;
        int maxWaves  = (guiHeight() - 76) / ROW_H;
        for (int i = waveScroll; i < Math.min(arena.waves().size(), waveScroll + maxWaves); i++) {
            ArenaDataPayload.WaveEntry wave = arena.waves().get(i);
            int ry = waveAreaY + (i - waveScroll) * ROW_H;
            if (i % 2 == 0) g.fill(dx, ry, dx + dw - 3, ry + ROW_H, theme.getTheme() != Theme.LIGHT ? 0x15FFFFFF : 0x11000000);
            g.text(font, "Wave " + wave.waveNumber(), dx + PANEL_PAD, ry + 4, theme.text(), false);
        }
    }

    private void renderDropdown(GuiGraphicsExtractor g) {
        if (detailView != DetailView.ADD_MOB) return;
        addMobScreen.renderDropdown(g, font);
    }

    // ── Input handling ────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {

        // Don't let keybind fire if typing in a text box
        if (!(this.getFocused() instanceof net.minecraft.client.gui.components.EditBox)) {
            if (ModKeyBindings.OPEN_MOB_ARENA_GUI.matches(event)) {
                this.onClose();
                return true;
            }
        }

        if (detailView == DetailView.ADD_MOB && addMobScreen.keyPressed(event)) {
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean bl) {
        if (detailView == DetailView.ADD_MOB && addMobScreen.mouseClicked(event)) {
            return true;
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        if (detailView == DetailView.ADD_MOB
                && addMobScreen.mouseScrolled(mx, my, vertical,
                detailX(), detailW(), guiTop(), guiHeight(), this::rebuildWidgets)) {
            return true;
        }

        int maxVisible = (guiHeight() - 40) / ROW_H;
        if (mx >= sidebarX() && mx <= sidebarX() + SIDEBAR_W
                && my >= guiTop() && my <= guiTop() + guiHeight()) {
            arenaScroll = (int) Math.max(0,
                    Math.min(arenaScroll - vertical, Math.max(0, arenas.size() - maxVisible)));
            rebuildWidgets();
            return true;
        }

        if (detailView == DetailView.OVERVIEW
                && mx >= detailX() && mx <= detailX() + detailW()
                && my >= guiTop() && my <= guiTop() + guiHeight()) {
            if (selectedArena >= 0 && selectedArena < arenas.size()) {
                int waveCount = arenas.get(selectedArena).waves().size();
                int maxWaves  = (guiHeight() - 76) / ROW_H;
                waveScroll = (int) Math.max(0,
                        Math.min(waveScroll - vertical, Math.max(0, waveCount - maxWaves)));
                rebuildWidgets();
            }
        }

        return super.mouseScrolled(mx, my, horizontal, vertical);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String truncate(String s) {
        return s.length() <= 11 ? s : s.substring(0, 10) + "...";
    }

    @Override
    public boolean isPauseScreen() { return false; }
}