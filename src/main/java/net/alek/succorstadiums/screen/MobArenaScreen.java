package net.alek.succorstadiums.screen;

import net.alek.succorstadiums.network.ArenaActionPayload;
import net.alek.succorstadiums.network.ArenaDataPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
 */
public class MobArenaScreen extends Screen {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ── Layout constants ─────────────────────────────────────────────────────

    private static final int SIDEBAR_W        = 110;
    private static final int PANEL_PAD        = 8;
    private static final int BTN_H            = 16;
    private static final int ROW_H            = 18;
    private static final int DETAIL_LINE_HEIGHT = 12;

    // ── Theme ─────────────────────────────────────────────────────────────────

    private boolean darkMode = loadDarkMode();

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

    private int colBg()      { return darkMode ? DARK_BG      : LIGHT_BG; }
    private int colSidebar() { return darkMode ? DARK_SIDEBAR : LIGHT_SIDEBAR; }
    private int colPanel()   { return darkMode ? DARK_PANEL   : LIGHT_PANEL; }
    private int colBorder()  { return darkMode ? DARK_BORDER  : LIGHT_BORDER; }
    private int colHeader()  { return darkMode ? DARK_HEADER  : LIGHT_HEADER; }
    private int colText()    { return darkMode ? DARK_TEXT     : LIGHT_TEXT; }
    private int colSubtext() { return darkMode ? DARK_SUBTEXT  : LIGHT_SUBTEXT; }

    // ── UI state ─────────────────────────────────────────────────────────────

    private List<ArenaDataPayload.ArenaEntry> arenas = new ArrayList<>();
    private int selectedArena = -1;
    private int selectedWave  = -1;

    private enum DetailView { OVERVIEW, ADD_ARENA, VIEW_MOB, ADD_MOB, DEL_MOB, EDIT_ARENA }
    private DetailView detailView = DetailView.OVERVIEW;

    // Collapsible section toggles
    private boolean showEquipmentFields    = false;
    private boolean showRidingMobField     = false;
    private boolean showPotionEffectsField = false;
    private boolean showEnchantmentsField  = false;

    // ── Multi-entry lists for potion effects and enchantments ─────────────────

    /** Each entry: {effectId, durationSeconds, amplifier} */
    private final List<String[]> potionEffectEntries = new ArrayList<>();

    /** Each entry: {enchantId, level, targetKey} */
    private final List<String[]> enchantmentEntries  = new ArrayList<>();

    private static final String[] ENCHANT_TARGETS     = {"Main Hand", "Off Hand", "Helmet", "Chestplate", "Leggings", "Boots"};
    private static final String[] ENCHANT_TARGET_KEYS = {"mainhand",  "offhand",  "helmet", "chestplate", "leggings", "boots"};

    /** Which target slot the pending-enchant row currently points to. */
    private int pendingEnchantTargetIndex = 0;

    // ── Backing fields: survive rebuildWidgets() so toggling sections doesn't wipe typed values ──

    // ADD_MOB core
    private String savedMobType    = "";
    private String savedMobCount   = "";

    // ADD_MOB equipment
    private String savedMainHand   = "";
    private String savedOffHand    = "";
    private String savedHelmet     = "";
    private String savedChestplate = "";
    private String savedLeggings   = "";
    private String savedBoots      = "";

    // ADD_MOB riding
    private String savedRidingMob  = "";

    // ── Input fields ──────────────────────────────────────────────────────────

    // ADD_ARENA / EDIT_ARENA
    private EditBox nameField, xField, yField, zField, radiusField, delayField;

    // ADD_MOB — core
    private EditBox mobTypeField, mobCountField;

    // ADD_MOB — equipment
    private EditBox mainHandItemField, offHandItemField;
    private EditBox helmetField, chestplateField, leggingsField, bootsField;

    // ADD_MOB — riding
    private EditBox ridingMobField;

    // ADD_MOB — pending potion row
    private EditBox pendingPotionIdField, pendingPotionDurationField, pendingPotionAmplifierField;
    private String pendingPotionId = "";
    private String pendingPotionDuration = "";
    private String pendingPotionAmplifier = "";

    // ADD_MOB — pending enchant row
    private EditBox pendingEnchantIdField, pendingEnchantLevelField;
    private String pendingEnchantId = "";
    private String pendingEnchantLevel = "";

    // ── Suggestion managers ───────────────────────────────────────────────────

    private SuggestionManager mobTypeSuggestionManager;
    private SuggestionManager ridingMobSuggestionManager;
    private SuggestionManager mainHandItemSuggestionManager;
    private SuggestionManager offHandItemSuggestionManager;
    private SuggestionManager helmetSuggestionManager;
    private SuggestionManager chestplateSuggestionManager;
    private SuggestionManager leggingsSuggestionManager;
    private SuggestionManager bootsSuggestionManager;
    private SuggestionManager pendingPotionSuggestionManager;
    private SuggestionManager pendingEnchantSuggestionManager;

    private static final int MAX_VISIBLE_SUGGESTIONS = 8;

    // ── Inline label lists (populated during buildAddMobWidgets, drawn in render) ──

    private final List<int[]>  inlineLabelPositions = new ArrayList<>();
    private final List<String> inlineLabelTexts     = new ArrayList<>();

    // ── Player picker ─────────────────────────────────────────────────────────

    private final List<String> selectedPlayers = new ArrayList<>();
    private boolean showPlayerPicker = false;

    // ── Scroll / tick ─────────────────────────────────────────────────────────

    private int waveScroll  = 0;
    private int arenaScroll = 0;
    private int tickCounter = 0;
    private int addMobScroll = 0;

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

    /**
     * The Y coordinate where scrollable ADD_MOB content begins (just below the header).
     */
    private int addMobScrollTop() { return guiTop() + 16; }

    /**
     * The Y coordinate where scrollable ADD_MOB content ends (just above the pinned buttons).
     */
    private int addMobScrollBottom() { return guiTop() + guiHeight() - BTN_H - PANEL_PAD; }

    // ── Widget construction ───────────────────────────────────────────────────

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        inlineLabelPositions.clear();
        inlineLabelTexts.clear();

        // Theme toggle
        addRenderableWidget(Button.builder(
                Component.literal(darkMode ? "☀ Light" : "☾ Dark"),
                btn -> { darkMode = !darkMode; saveDarkMode(darkMode); rebuildWidgets(); }
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
        int cx   = detailX() + PANEL_PAD;
        int cy   = guiTop() + 20;
        int fw   = detailW() - PANEL_PAD * 2;
        int by   = guiTop() + guiHeight() - BTN_H - PANEL_PAD;
        int posW = (fw - 54) / 3 - 2;

        nameField   = makeField(cx, cy + 10, fw, 14, "Arena name");
        xField      = makeField(cx, cy + 50, posW, 14, "X");
        yField      = makeField(cx + posW + 1, cy + 50, posW, 14, "Y");
        zField      = makeField(cx + (posW + 1) * 2, cy + 50, posW, 14, "Z");
        radiusField = makeField(cx, cy + 90, fw / 2 - 2, 14, "Radius");
        delayField  = makeField(cx + fw / 2 + 2, cy + 90, fw / 2 - 2, 14, "Delay (s)");

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

    private void buildEditArenaWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);

        int cx   = detailX() + PANEL_PAD;
        int cy   = guiTop() + 20;
        int fw   = detailW() - PANEL_PAD * 2;
        int by   = guiTop() + guiHeight() - BTN_H - PANEL_PAD;
        int posW = (fw - 54) / 3 - 2;

        nameField   = makeField(cx, cy + 10, fw, 14, "Arena name");
        xField      = makeField(cx, cy + 50, posW, 14, "X");
        yField      = makeField(cx + posW + 1, cy + 50, posW, 14, "Y");
        zField      = makeField(cx + (posW + 1) * 2, cy + 50, posW, 14, "Z");
        radiusField = makeField(cx, cy + 90, fw / 2 - 2, 14, "Radius");
        delayField  = makeField(cx + fw / 2 + 2, cy + 90, fw / 2 - 2, 14, "Delay (s)");

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

    private void buildAddMobWidgets() {
        int cx       = detailX() + PANEL_PAD;
        int fw       = detailW() - PANEL_PAD * 2;
        // Logical (unscrolled) Y where scrollable content begins
        int startY   = guiTop() + 20;
        int currentY = startY;
        // Fixed Y for the pinned Add / Cancel buttons
        int by       = addMobScrollBottom();

        // Visible window for scrolled widgets
        int scrollTop    = addMobScrollTop();
        int scrollBottom = addMobScrollBottom();

        int contentHeight = 0;

        // ── Mob Type ──────────────────────────────────────────────────────────
        drawInlineLabel(cx, currentY, "Mob Type");
        currentY += 10; contentHeight += 10;
        mobTypeField = addScrolledField(cx, currentY, fw, 14, "e.g. minecraft:zombie", scrollTop, scrollBottom);
        if (mobTypeField != null) {
            mobTypeField.setValue(savedMobType);
            mobTypeSuggestionManager = new SuggestionManager(
                    mobTypeField, BuiltInRegistries.ENTITY_TYPE, MAX_VISIBLE_SUGGESTIONS, 14, false);
            mobTypeField.setResponder(text -> {
                savedMobType = text;
                mobTypeSuggestionManager.filterSuggestions(text);
            });
        }
        currentY += 18; contentHeight += 18;

        // ── Count ─────────────────────────────────────────────────────────────
        drawInlineLabel(cx, currentY, "Count");
        currentY += 10; contentHeight += 10;
        mobCountField = addScrolledField(cx, currentY, fw / 3, 14, "1", scrollTop, scrollBottom);
        if (mobCountField != null) {
            mobCountField.setValue(savedMobCount);
            mobCountField.setResponder(text -> savedMobCount = text);
        }
        currentY += 18; contentHeight += 18;

        // ── Equipment toggle ──────────────────────────────────────────────────
        int equipmentCount = 0;
        if (!savedMainHand.isBlank()) equipmentCount++;
        if (!savedOffHand.isBlank()) equipmentCount++;
        if (!savedHelmet.isBlank()) equipmentCount++;
        if (!savedChestplate.isBlank()) equipmentCount++;
        if (!savedLeggings.isBlank()) equipmentCount++;
        if (!savedBoots.isBlank()) equipmentCount++;

        addScrolledButton(
                Component.literal(
                        (showEquipmentFields ? "▼" : "▶")
                                + " Equipment"
                                + (equipmentCount == 0 ? "" : " (" + equipmentCount + ")")
                ),
                btn -> {
                    showEquipmentFields = !showEquipmentFields;
                    rebuildWidgets();
                },
                cx, currentY, fw, BTN_H, scrollTop, scrollBottom
        );
        currentY += BTN_H + 4; contentHeight += BTN_H + 4;

        if (showEquipmentFields) {
            drawInlineLabel(cx, currentY, "Main Hand Item"); currentY += 10; contentHeight += 10;
            mainHandItemField = addScrolledField(cx, currentY, fw, 14, "e.g. minecraft:diamond_sword", scrollTop, scrollBottom);
            if (mainHandItemField != null) {
                mainHandItemField.setValue(savedMainHand);
                mainHandItemSuggestionManager = new SuggestionManager(
                        mainHandItemField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                mainHandItemField.setResponder(text -> {
                    savedMainHand = text;
                    mainHandItemSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18; contentHeight += 18;

            drawInlineLabel(cx, currentY, "Off Hand Item"); currentY += 10; contentHeight += 10;
            offHandItemField = addScrolledField(cx, currentY, fw, 14, "e.g. minecraft:shield", scrollTop, scrollBottom);
            if (offHandItemField != null) {
                offHandItemField.setValue(savedOffHand);
                offHandItemSuggestionManager = new SuggestionManager(
                        offHandItemField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                offHandItemField.setResponder(text -> {
                    savedOffHand = text;
                    offHandItemSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18; contentHeight += 18;

            drawInlineLabel(cx, currentY, "Helmet"); currentY += 10; contentHeight += 10;
            helmetField = addScrolledField(cx, currentY, fw, 14, "e.g. minecraft:diamond_helmet", scrollTop, scrollBottom);
            if (helmetField != null) {
                helmetField.setValue(savedHelmet);
                helmetSuggestionManager = new SuggestionManager(
                        helmetField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                helmetField.setResponder(text -> {
                    savedHelmet = text;
                    helmetSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18; contentHeight += 18;

            drawInlineLabel(cx, currentY, "Chestplate"); currentY += 10; contentHeight += 10;
            chestplateField = addScrolledField(cx, currentY, fw, 14, "e.g. minecraft:diamond_chestplate", scrollTop, scrollBottom);
            if (chestplateField != null) {
                chestplateField.setValue(savedChestplate);
                chestplateSuggestionManager = new SuggestionManager(
                        chestplateField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                chestplateField.setResponder(text -> {
                    savedChestplate = text;
                    chestplateSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18; contentHeight += 18;

            drawInlineLabel(cx, currentY, "Leggings"); currentY += 10; contentHeight += 10;
            leggingsField = addScrolledField(cx, currentY, fw, 14, "e.g. minecraft:diamond_leggings", scrollTop, scrollBottom);
            if (leggingsField != null) {
                leggingsField.setValue(savedLeggings);
                leggingsSuggestionManager = new SuggestionManager(
                        leggingsField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                leggingsField.setResponder(text -> {
                    savedLeggings = text;
                    leggingsSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18; contentHeight += 18;

            drawInlineLabel(cx, currentY, "Boots"); currentY += 10; contentHeight += 10;
            bootsField = addScrolledField(cx, currentY, fw, 14, "e.g. minecraft:diamond_boots", scrollTop, scrollBottom);
            if (bootsField != null) {
                bootsField.setValue(savedBoots);
                bootsSuggestionManager = new SuggestionManager(
                        bootsField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                bootsField.setResponder(text -> {
                    savedBoots = text;
                    bootsSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18; contentHeight += 18;
        }

        // ── Potion Effects toggle ─────────────────────────────────────────────
        addScrolledButton(
                Component.literal((showPotionEffectsField ? "▼" : "▶") + " Potion Effects"
                        + (potionEffectEntries.isEmpty() ? "" : " (" + potionEffectEntries.size() + ")")),
                btn -> { showPotionEffectsField = !showPotionEffectsField; rebuildWidgets(); },
                cx, currentY, fw, BTN_H, scrollTop, scrollBottom
        );
        currentY += BTN_H + 4; contentHeight += BTN_H + 4;

        if (showPotionEffectsField) {
            int c0 = (fw - 26) / 2;
            int c1 = (fw - 26) / 4;
            int c2 = (fw - 26) / 4 - 2;

            for (int i = 0; i < potionEffectEntries.size(); i++) {
                final int idx = i;
                String[] e = potionEffectEntries.get(i);
                drawInlineLabel(cx,              currentY, formatIdentifierForDisplay(e[0]));
                drawInlineLabel(cx + c0 + 2,     currentY, ("-1".equals(e[1]) || "0".equals(e[1])) ? "Infinite" : e[1] + "s");
                drawInlineLabel(cx + c0 + c1 + 4, currentY, "Amp " + e[2]);
                addScrolledButton(
                        Component.literal("✕"),
                        btn -> { potionEffectEntries.remove(idx); rebuildWidgets(); },
                        cx + fw - 20, currentY - 2, 20, 12, scrollTop, scrollBottom
                );
                currentY += 14; contentHeight += 14;
            }

            drawInlineLabel(cx,               currentY, "Effect");
            drawInlineLabel(cx + c0 + 2, currentY, "Duration (s/inf)");
            drawInlineLabel(cx + c0 + c1 + 4, currentY, "Amplifier (0-255)");
            currentY += 10; contentHeight += 10;

            pendingPotionIdField = addScrolledField(cx, currentY, c0, 14, "e.g. minecraft:strength", scrollTop, scrollBottom);
            if (pendingPotionIdField != null) {
                pendingPotionIdField.setValue(pendingPotionId);
                pendingPotionSuggestionManager = new SuggestionManager(
                        pendingPotionIdField, BuiltInRegistries.MOB_EFFECT, MAX_VISIBLE_SUGGESTIONS, 14, false);
                pendingPotionIdField.setResponder(text -> { pendingPotionId = text; pendingPotionSuggestionManager.filterSuggestions(text); });
            }

            pendingPotionDurationField = addScrolledField(cx + c0 + 2, currentY, c1, 14, "60 / inf", scrollTop, scrollBottom);
            if (pendingPotionDurationField != null) {
                pendingPotionDurationField.setValue(pendingPotionDuration);
                pendingPotionDurationField.setResponder(text -> pendingPotionDuration = text);
            }

            pendingPotionAmplifierField = addScrolledField(cx + c0 + c1 + 4, currentY, c2, 14, "0 (0-255)", scrollTop, scrollBottom);
            if (pendingPotionAmplifierField != null) {
                pendingPotionAmplifierField.setValue(pendingPotionAmplifier);
                pendingPotionAmplifierField.setResponder(text -> pendingPotionAmplifier = text);
            }
            currentY += 18; contentHeight += 18;

            addScrolledButton(
                    Component.literal("+ Add Effect"),
                    btn -> {
                        if (pendingPotionIdField == null) return;
                        String id  = pendingPotionIdField.getValue().trim();

                        String dur = pendingPotionDurationField != null
                                ? pendingPotionDurationField.getValue().trim()
                                : "";

                        if (dur.equalsIgnoreCase("inf")
                                || dur.equalsIgnoreCase("infinite")) {
                            dur = "-1";
                        }

                        int amplifier = 0;

                        try {
                            amplifier = Integer.parseInt(
                                    pendingPotionAmplifierField != null
                                            ? pendingPotionAmplifierField.getValue().trim()
                                            : "0"
                            );
                        } catch (Exception ignored) {
                        }

                        amplifier = Math.max(0, Math.min(255, amplifier));

                        potionEffectEntries.add(new String[]{
                                id,
                                dur.isEmpty() ? "60" : dur,
                                String.valueOf(amplifier)
                        });

                        pendingPotionId = "";
                        pendingPotionDuration = "";
                        pendingPotionAmplifier = "";
                        rebuildWidgets();
                    },
                    cx, currentY, 82, BTN_H, scrollTop, scrollBottom
            );
            currentY += BTN_H + 4; contentHeight += BTN_H + 4;
        }

        // ── Enchantments toggle ───────────────────────────────────────────────
        addScrolledButton(
                Component.literal((showEnchantmentsField ? "▼" : "▶") + " Enchantments"
                        + (enchantmentEntries.isEmpty() ? "" : " (" + enchantmentEntries.size() + ")")),
                btn -> { showEnchantmentsField = !showEnchantmentsField; rebuildWidgets(); },
                cx, currentY, fw, BTN_H, scrollTop, scrollBottom
        );
        currentY += BTN_H + 4; contentHeight += BTN_H + 4;

        if (showEnchantmentsField) {
            int e0 = (fw - 26) / 2;
            int e1 = (fw - 26) / 6;
            int e2 = fw - e0 - e1 - 28;

            for (int i = 0; i < enchantmentEntries.size(); i++) {
                final int idx = i;
                String[] en = enchantmentEntries.get(i);
                String targetDisplay = en[2];
                for (int t = 0; t < ENCHANT_TARGET_KEYS.length; t++) {
                    if (ENCHANT_TARGET_KEYS[t].equals(en[2])) { targetDisplay = ENCHANT_TARGETS[t]; break; }
                }
                drawInlineLabel(cx, currentY, formatIdentifierForDisplay(en[0]));
                drawInlineLabel(cx + e0 + 2, currentY, "Lvl " + en[1]);
                drawInlineLabel(cx + e0 + e1 + 4, currentY, "→ " + targetDisplay);
                addScrolledButton(
                        Component.literal("✕"),
                        btn -> { enchantmentEntries.remove(idx); rebuildWidgets(); },
                        cx + fw - 20, currentY - 2, 20, 12, scrollTop, scrollBottom
                );
                currentY += 14; contentHeight += 14;
            }

            drawInlineLabel(cx,               currentY, "Enchantment");

            int maxEnchantLevel = -1;

            try {
                String enchantId = pendingEnchantId.trim();

                if (!enchantId.isEmpty()) {
                    Identifier id = Identifier.parse(enchantId);

                    RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
                    var enchantmentRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
                    var holder = enchantmentRegistry.get(id);

                    if (holder.isPresent()) {
                        maxEnchantLevel = holder.get().value().getMaxLevel();
                    }
                }
            } catch (Exception ignored) {
            }

            drawInlineLabel(
                    cx + e0 + 2,
                    currentY,
                    maxEnchantLevel > 0
                            ? "Level (1-" + maxEnchantLevel + ")"
                            : "Level"
            );

            drawInlineLabel(cx + e0 + e1 + 4, currentY, "Apply To");
            currentY += 10; contentHeight += 10;

            assert Minecraft.getInstance().level != null;
            RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
            var enchantmentRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

            pendingEnchantIdField = addScrolledField(cx, currentY, e0, 14, "e.g. minecraft:sharpness", scrollTop, scrollBottom);
            if (pendingEnchantIdField != null) {
                pendingEnchantIdField.setValue(pendingEnchantId);
                pendingEnchantSuggestionManager = new SuggestionManager(
                        pendingEnchantIdField, enchantmentRegistry, MAX_VISIBLE_SUGGESTIONS, 14, false);
                pendingEnchantIdField.setResponder(text -> { pendingEnchantId = text; pendingEnchantSuggestionManager.filterSuggestions(text); });
            }

            String levelHint =
                    maxEnchantLevel > 0
                            ? "1-" + maxEnchantLevel
                            : "1";

            pendingEnchantLevelField = addScrolledField(
                    cx + e0 + 2,
                    currentY,
                    e1,
                    14,
                    levelHint,
                    scrollTop,
                    scrollBottom
            );
            if (pendingEnchantLevelField != null) {
                pendingEnchantLevelField.setValue(pendingEnchantLevel);
                pendingEnchantLevelField.setResponder(text -> pendingEnchantLevel = text);
            }

            final String currentTarget = ENCHANT_TARGETS[pendingEnchantTargetIndex];
            addScrolledButton(
                    Component.literal(currentTarget),
                    btn -> { pendingEnchantTargetIndex = (pendingEnchantTargetIndex + 1) % ENCHANT_TARGETS.length; rebuildWidgets(); },
                    cx + e0 + e1 + 4, currentY, e2, 14, scrollTop, scrollBottom
            );
            currentY += 18; contentHeight += 18;

            int finalMaxEnchantLevel = maxEnchantLevel;
            addScrolledButton(
                    Component.literal("+ Add Enchant"),
                    btn -> {
                        if (pendingEnchantIdField == null) return;
                        String id  = pendingEnchantIdField.getValue().trim();
                        String lvl = pendingEnchantLevelField != null ? pendingEnchantLevelField.getValue().trim() : "";
                        if (id.isEmpty()) return;

                        int level = 1;

                        try {
                            level = Integer.parseInt(lvl);
                        } catch (Exception ignored) {
                        }

                        level = Math.max(1, level);

                        if (finalMaxEnchantLevel > 0) {
                            level = Math.min(level, finalMaxEnchantLevel);
                        }

                        enchantmentEntries.add(new String[]{
                                id,
                                String.valueOf(level),
                                ENCHANT_TARGET_KEYS[pendingEnchantTargetIndex]
                        });
                        pendingEnchantId = "";
                        pendingEnchantLevel = "";
                        rebuildWidgets();
                    },
                    cx, currentY, 88, BTN_H, scrollTop, scrollBottom
            );
            currentY += BTN_H + 4; contentHeight += BTN_H + 4;
        }

        // ── Riding Mob toggle ─────────────────────────────────────────────────
        boolean hasRidingMob = !savedRidingMob.isEmpty();
        addScrolledButton(
                Component.literal((showRidingMobField ? "▼" : "▶") + " Riding Mob" + (hasRidingMob ? " (1)" : "")),
                btn -> { showRidingMobField = !showRidingMobField; rebuildWidgets(); },
                cx, currentY, fw, BTN_H, scrollTop, scrollBottom
        );
        currentY += BTN_H + 4; contentHeight += BTN_H + 4;

        if (showRidingMobField) {
            drawInlineLabel(cx, currentY, "Riding Mob"); currentY += 10; contentHeight += 10;
            ridingMobField = addScrolledField(cx, currentY, fw, 14, "e.g. minecraft:spider", scrollTop, scrollBottom);
            if (ridingMobField != null) {
                ridingMobField.setValue(savedRidingMob);
                ridingMobSuggestionManager = new SuggestionManager(
                        ridingMobField, BuiltInRegistries.ENTITY_TYPE, MAX_VISIBLE_SUGGESTIONS, 14, false);
                ridingMobField.setResponder(text -> {
                    savedRidingMob = text;
                    ridingMobSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18; contentHeight += 18;
        }

        // FIX 4: Clamp addMobScroll AFTER contentHeight is fully accumulated.
        int scrollableAreaHeight = scrollBottom - scrollTop;
        int maxScroll = Math.max(0, contentHeight - scrollableAreaHeight);
        addMobScroll = Math.max(0, Math.min(addMobScroll, maxScroll));

        // ── Add / Cancel — pinned to bottom (always visible, unaffected by scroll) ──
        addRenderableWidget(Button.builder(Component.literal("✔ Add"), btn -> submitAddMob())
                .bounds(cx, by, 50, BTN_H).build());
        addRenderableWidget(Button.builder(Component.literal("✕ Cancel"),
                        btn -> {
                            detailView = DetailView.OVERVIEW;
                            clearAddMobState();
                            rebuildWidgets();
                        })
                .bounds(cx + 54, by, 50, BTN_H).build());
    }

    // ── Scrolled-widget helpers ───────────────────────────────────────────────

    /**
     * Creates an EditBox at the given LOGICAL (unscrolled) y, offset by addMobScroll,
     * and only registers it if the resulting screen-space Y is within [scrollTop, scrollBottom].
     *
     * Returns the field so callers can attach responders / set values; returns null when
     * the widget is outside the visible window (callers must null-check).
     */
    private EditBox addScrolledField(int x, int logicalY, int w, int h, String hint,
                                     int scrollTop, int scrollBottom) {
        int screenY = logicalY - addMobScroll;
        EditBox field = new EditBox(font, x, screenY, w, h, Component.literal(hint));
        field.setHint(Component.literal(hint));
        field.setBordered(true);
        field.setMaxLength(64);
        if (screenY + h > scrollTop && screenY < scrollBottom) {
            addRenderableWidget(field);
        }
        return field;
    }

    /**
     * Creates a Button at the given LOGICAL (unscrolled) y, offset by addMobScroll,
     * and only registers it if the resulting screen-space Y is within [scrollTop, scrollBottom].
     */
    private void addScrolledButton(Component label, Button.OnPress onPress,
                                   int x, int logicalY, int w, int h,
                                   int scrollTop, int scrollBottom) {
        int screenY = logicalY - addMobScroll;
        if (screenY + h > scrollTop && screenY < scrollBottom) {
            addRenderableWidget(Button.builder(label, onPress)
                    .bounds(x, screenY, w, h).build());
        }
    }

    // ── Remaining widget builders ─────────────────────────────────────────────

    private void buildDelMobWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);

        ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
        if (wave == null) return;

        int cx       = detailX() + PANEL_PAD;
        int currentY = guiTop() + 36;
        int by       = guiTop() + guiHeight() - BTN_H - PANEL_PAD;

        for (int i = 0; i < wave.mobs().size(); i++) {
            ArenaDataPayload.MobEntry mob = wave.mobs().get(i);

            int mobDetailsHeight = ROW_H;
            if (mob.mainHandItem() != null && !mob.mainHandItem().isEmpty()) mobDetailsHeight += DETAIL_LINE_HEIGHT;
            if (mob.offHandItem()  != null && !mob.offHandItem().isEmpty())  mobDetailsHeight += DETAIL_LINE_HEIGHT;
            if (mob.armorItems()   != null && !mob.armorItems().isEmpty())   mobDetailsHeight += mob.armorItems().size() * DETAIL_LINE_HEIGHT;
            if (mob.ridingMob()    != null && !mob.ridingMob().isEmpty())    mobDetailsHeight += DETAIL_LINE_HEIGHT;
            if (mob.potionEffects() != null && !mob.potionEffects().isEmpty()) {
                mobDetailsHeight += DETAIL_LINE_HEIGHT; // For "Potion Effects:" label
                mobDetailsHeight += mob.potionEffects().split(",").length * DETAIL_LINE_HEIGHT;
            }
            if (mob.enchantments() != null && !mob.enchantments().isEmpty()) {
                mobDetailsHeight += DETAIL_LINE_HEIGHT; // For "Enchantments:" label
                mobDetailsHeight += mob.enchantments().split(",").length * DETAIL_LINE_HEIGHT;
            }
            mobDetailsHeight += 4;

            addRenderableWidget(Button.builder(Component.literal("-1"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(arena.name(), selectedWave, mob.mobType(), 1))
            ).bounds(cx, currentY, 24, BTN_H).build());

            addRenderableWidget(Button.builder(Component.literal("-5"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(arena.name(), selectedWave, mob.mobType(), 5))
            ).bounds(cx + 28, currentY, 24, BTN_H).build());

            addRenderableWidget(Button.builder(Component.literal("-10"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(arena.name(), selectedWave, mob.mobType(), 10))
            ).bounds(cx + 56, currentY, 28, BTN_H).build());

            addRenderableWidget(Button.builder(Component.literal("-20"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(arena.name(), selectedWave, mob.mobType(), 20))
            ).bounds(cx + 88, currentY, 28, BTN_H).build());

            addRenderableWidget(Button.builder(Component.literal("✕ All"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeMob(arena.name(), selectedWave, mob.mobType(), mob.count()))
            ).bounds(cx + 120, currentY, 36, BTN_H).build());

            currentY += mobDetailsHeight;
        }

        addRenderableWidget(Button.builder(Component.literal("< Back"),
                        btn -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); })
                .bounds(cx, by, 50, BTN_H).build());
    }

    private void buildPlayerPickerButtons() {
        List<String> online = new ArrayList<>();
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            conn.getOnlinePlayers().forEach(p -> online.add(p.getProfile().name()));
        }

        int cx = detailX() + PANEL_PAD;
        int cy = guiTop() + 36;
        int fw = detailW() - PANEL_PAD * 2;

        for (int i = 0; i < Math.min(online.size(), 8); i++) {
            final String pName = online.get(i);
            boolean sel = selectedPlayers.contains(pName);
            addRenderableWidget(Button.builder(
                    Component.literal((sel ? "☑ " : "☐ ") + pName),
                    btn -> {
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

    private void submitCreateArena() {
        try {
            if (nameField == null || xField == null || yField == null ||
                    zField == null || radiusField == null || delayField == null) return;
            String name = nameField.getValue().trim();
            if (name.isEmpty()) return;
            double x  = Double.parseDouble(xField.getValue().trim());
            double y  = Double.parseDouble(yField.getValue().trim());
            double z  = Double.parseDouble(zField.getValue().trim());
            int radius = Integer.parseInt(radiusField.getValue().trim());
            int delay  = Integer.parseInt(delayField.getValue().trim());
            ClientPlayNetworking.send(ArenaActionPayload.createArena(name, x, y, z, radius, delay));
            detailView = DetailView.OVERVIEW;
            rebuildWidgets();
        } catch (NumberFormatException ignored) {}
    }

    private void submitEditArena() {
        try {
            if (nameField == null || xField == null || yField == null || zField == null ||
                    radiusField == null || delayField == null) return;
            if (selectedArena < 0 || selectedArena >= arenas.size()) return;
            String oldName = arenas.get(selectedArena).name();
            String newName = nameField.getValue().trim();
            if (newName.isEmpty()) return;
            double x  = Double.parseDouble(xField.getValue().trim());
            double y  = Double.parseDouble(yField.getValue().trim());
            double z  = Double.parseDouble(zField.getValue().trim());
            int radius = Integer.parseInt(radiusField.getValue().trim());
            int delay  = Integer.parseInt(delayField.getValue().trim());
            ClientPlayNetworking.send(ArenaActionPayload.editArena(oldName, newName, x, y, z, radius, delay));
            detailView = DetailView.OVERVIEW;
            rebuildWidgets();
        } catch (NumberFormatException ignored) {}
    }

    private void submitAddMob() {
        try {
            if (mobTypeField == null || mobCountField == null) return;
            String mob   = mobTypeField.getValue().trim();
            int    count = Integer.parseInt(mobCountField.getValue().trim());
            if (mob.isEmpty() || count < 1 || selectedArena < 0 || selectedArena >= arenas.size()) return;

            String ridingMob    = savedRidingMob;
            String mainHandItem = savedMainHand;
            String offHandItem  = savedOffHand;

            List<String> armorItems = new ArrayList<>();
            if (!savedHelmet.isEmpty())     armorItems.add(savedHelmet);
            if (!savedChestplate.isEmpty()) armorItems.add(savedChestplate);
            if (!savedLeggings.isEmpty())   armorItems.add(savedLeggings);
            if (!savedBoots.isEmpty())      armorItems.add(savedBoots);

            String potionEffects = potionEffectEntries.stream()
                    .map(e -> e[0] + ":" + e[1] + ":" + e[2])
                    .collect(Collectors.joining(","));

            String enchantments = enchantmentEntries.stream()
                    .map(e -> e[2] + ":" + e[0] + ":" + e[1])
                    .collect(Collectors.joining(","));

            String arenaName = arenas.get(selectedArena).name();
            ClientPlayNetworking.send(ArenaActionPayload.addMob(
                    arenaName, selectedWave, mob, count,
                    ridingMob.isEmpty()    ? null : ridingMob,
                    mainHandItem.isEmpty() ? null : mainHandItem,
                    offHandItem.isEmpty()  ? null : offHandItem,
                    armorItems,
                    potionEffects.isEmpty() ? null : potionEffects,
                    enchantments.isEmpty()  ? null : enchantments
            ));

            clearAddMobState();
            detailView = DetailView.OVERVIEW;
            rebuildWidgets();
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Resets all ADD_MOB transient state — called on successful submit or cancel.
     */
    private void clearAddMobState() {
        potionEffectEntries.clear();
        enchantmentEntries.clear();
        pendingEnchantTargetIndex = 0;
        addMobScroll = 0;
        showEquipmentFields    = false;
        showRidingMobField     = false;
        showPotionEffectsField = false;
        showEnchantmentsField  = false;
        // Clear pending potion/enchant rows
        pendingPotionId = "";
        pendingPotionDuration = "";
        pendingPotionAmplifier = "";
        pendingEnchantId = "";
        pendingEnchantLevel = "";
        // Clear saved field values
        savedMobType    = "";
        savedMobCount   = "";
        savedMainHand   = "";
        savedOffHand    = "";
        savedHelmet     = "";
        savedChestplate = "";
        savedLeggings   = "";
        savedBoots      = "";
        savedRidingMob  = "";
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int gl = guiLeft(), gt = guiTop(), gw = guiWidth(), gh = guiHeight();

        g.fill(gl, gt, gl + gw, gt + gh, colBg());
        g.fill(sidebarX(), gt, sidebarX() + SIDEBAR_W, gt + gh, colSidebar());
        g.fill(sidebarX() + SIDEBAR_W, gt, sidebarX() + SIDEBAR_W + 1, gt + gh, colBorder());
        g.fill(detailX(), gt, detailX() + detailW(), gt + gh, colPanel());
        g.outline(gl, gt, gw, gh, colBorder());
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

        // ── Player picker ──
        if (showPlayerPicker) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "Select Players to Start Arena", dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);
            return;
        }

        // ── ADD_ARENA ──
        if (detailView == DetailView.ADD_ARENA) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "New Arena",          dx + PANEL_PAD, dt + 4,   0xFFFFFFFF, false);
            g.text(font, "Name",               dx + PANEL_PAD, dt + 20,  colSubtext(), false);
            g.text(font, "Position",           dx + PANEL_PAD, dt + 60,  colSubtext(), false);
            g.text(font, "Radius / Delay (s)", dx + PANEL_PAD, dt + 100, colSubtext(), false);
            return;
        }

        // ── EDIT_ARENA ──
        if (detailView == DetailView.EDIT_ARENA) {
            if (selectedArena < 0 || selectedArena >= arenas.size()) return;
            ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "Edit Arena: " + arena.name(), dx + PANEL_PAD, dt + 4,   0xFFFFFFFF, false);
            g.text(font, "Name",                        dx + PANEL_PAD, dt + 20,  colSubtext(), false);
            g.text(font, "Position",                    dx + PANEL_PAD, dt + 60,  colSubtext(), false);
            g.text(font, "Radius / Delay (s)",          dx + PANEL_PAD, dt + 100, colSubtext(), false);
            return;
        }

        // ── VIEW_MOB ──
        if (detailView == DetailView.VIEW_MOB) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "Mobs in Wave " + selectedWave, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

            if (selectedArena >= 0 && selectedArena < arenas.size()) {
                ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
                ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                        .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
                if (wave != null && !wave.mobs().isEmpty()) {
                    int currentY = guiTop() + 24;
                    for (int i = 0; i < wave.mobs().size(); i++) {
                        ArenaDataPayload.MobEntry mob = wave.mobs().get(i);
                        if (i % 2 == 0) g.fill(dx, currentY, dx + dw, currentY + ROW_H, darkMode ? 0x15FFFFFF : 0x11000000);
                        g.text(font, mob.count() + "x  " + formatIdentifierForDisplay(mob.mobType()),
                                dx + PANEL_PAD, currentY + 4, colText(), false);
                        currentY += ROW_H;

                        if (mob.mainHandItem() != null && !mob.mainHandItem().isEmpty()) {
                            g.text(font, "  Main Hand: " + formatIdentifierForDisplay(mob.mainHandItem()),
                                    dx + PANEL_PAD + 10, currentY + 4, colSubtext(), false);
                            currentY += DETAIL_LINE_HEIGHT;
                        }
                        if (mob.offHandItem() != null && !mob.offHandItem().isEmpty()) {
                            g.text(font, "  Off Hand: " + formatIdentifierForDisplay(mob.offHandItem()),
                                    dx + PANEL_PAD + 10, currentY + 4, colSubtext(), false);
                            currentY += DETAIL_LINE_HEIGHT;
                        }
                        if (mob.armorItems() != null && !mob.armorItems().isEmpty()) {
                            String[] slots = {"Helmet", "Chestplate", "Leggings", "Boots"};
                            for (int s = 0; s < Math.min(mob.armorItems().size(), 4); s++) {
                                g.text(font, "  " + slots[s] + ": " + formatIdentifierForDisplay(mob.armorItems().get(s)),
                                        dx + PANEL_PAD + 10, currentY + 4, colSubtext(), false);
                                currentY += DETAIL_LINE_HEIGHT;
                            }
                        }
                        if (mob.ridingMob() != null && !mob.ridingMob().isEmpty()) {
                            g.text(font, "  Riding: " + formatIdentifierForDisplay(mob.ridingMob()),
                                    dx + PANEL_PAD + 10, currentY + 4, colSubtext(), false);
                            currentY += DETAIL_LINE_HEIGHT;
                        }
                        if (mob.potionEffects() != null && !mob.potionEffects().isEmpty()) {
                            g.text(font, "  Potion Effects:", dx + PANEL_PAD + 10, currentY + 4, colSubtext(), false);
                            currentY += DETAIL_LINE_HEIGHT;
                            for (String effect : mob.potionEffects().split(",")) {
                                String[] parts = effect.split(":");
                                if (parts.length >= 3) {
                                    // The last two parts are always duration and amplifier
                                    String ampStr = parts[parts.length - 1];
                                    String durStr = parts[parts.length - 2];

                                    // The effectId is everything before the last two parts
                                    String effectId = String.join(":", Arrays.copyOfRange(parts, 0, parts.length - 2));

                                    g.text(font, "    - " + formatIdentifierForDisplay(effectId) + " (" + (("-1".equals(durStr) || "0".equals(durStr)) ? "Infinite" : durStr + "s") + ", Amp " + ampStr + ")",
                                            dx + PANEL_PAD + 20, currentY + 4, colSubtext(), false);
                                    currentY += DETAIL_LINE_HEIGHT;
                                }
                            }
                        }
                        if (mob.enchantments() != null && !mob.enchantments().isEmpty()) {
                            g.text(font, "  Enchantments:", dx + PANEL_PAD + 10, currentY + 4, colSubtext(), false);
                            currentY += DETAIL_LINE_HEIGHT;
                            for (String enchantment : mob.enchantments().split(",")) {
                                String[] parts = enchantment.split(":");
                                // Expected format: "target:enchantId:level"
                                // enchantId can be "sharpness" or "minecraft:sharpness"
                                if (parts.length >= 3) { // Changed from == 3 to >= 3
                                    // The last part is always the level
                                    String lvlStr = parts[parts.length - 1];
                                    // The first part is always the target
                                    String target = parts[0];
                                    // The enchantId is everything in between
                                    String enchantId = String.join(":", Arrays.copyOfRange(parts, 1, parts.length - 1));

                                    String targetDisplay = target;
                                    for (int t = 0; t < ENCHANT_TARGET_KEYS.length; t++) {
                                        if (ENCHANT_TARGET_KEYS[t].equals(target)) { targetDisplay = ENCHANT_TARGETS[t]; break; }
                                    }
                                    g.text(font, "    - " + formatIdentifierForDisplay(enchantId) + " (Lvl " + lvlStr + ") on " + targetDisplay,
                                            dx + PANEL_PAD + 20, currentY + 4, colSubtext(), false);
                                    currentY += DETAIL_LINE_HEIGHT;
                                }
                            }
                        }
                        currentY += 4;
                    }
                } else {
                    g.text(font, "No mobs in this wave.", dx + PANEL_PAD, guiTop() + 28, colSubtext(), false);
                }
            }
            return;
        }

        // ── ADD_MOB ──
        if (detailView == DetailView.ADD_MOB) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
            g.text(font, "Add Mob(s) to Wave " + selectedWave, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

            // Scissor to the scrollable area so labels don't bleed into the header or footer
            int scissorTop    = addMobScrollTop();
            int scissorBottom = addMobScrollBottom();
            g.enableScissor(dx, scissorTop, dx + dw, scissorBottom);

            // Labels were stored at unscrolled Y; subtract addMobScroll once here
            for (int i = 0; i < inlineLabelPositions.size(); i++) {
                int[] pos = inlineLabelPositions.get(i);
                int labelY = pos[1] - addMobScroll;
                // Only draw if inside the scissored area
                if (labelY >= scissorTop && labelY < scissorBottom) {
                    g.text(font, inlineLabelTexts.get(i), pos[0], labelY, colSubtext(), false);
                }
            }
            g.disableScissor();
            return;
        }

        // ── DEL_MOB ──
        if (detailView == DetailView.DEL_MOB) {
            g.fill(dx, dt, dx + dw, dt + 16, 0xFFAA3333);
            g.text(font, "Remove Mobs from Wave " + selectedWave, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

            if (selectedArena >= 0 && selectedArena < arenas.size()) {
                ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
                ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                        .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
                if (wave != null) {
                    int currentY = guiTop() + 36;
                    for (int i = 0; i < wave.mobs().size(); i++) {
                        ArenaDataPayload.MobEntry mob = wave.mobs().get(i);
                        if (i % 2 == 0) g.fill(dx, currentY, dx + dw, currentY + ROW_H, darkMode ? 0x15FFFFFF : 0x11000000);

                        String mobName = mob.mobType().replace("minecraft:", "");
                        String display = Character.toUpperCase(mobName.charAt(0)) + mobName.substring(1).replace("_", " ");
                        g.text(font, mob.count() + "x  " + display, dx + PANEL_PAD + 162, currentY + 4, colText(), false);
                        int currentDetailY = currentY + ROW_H;

                        if (mob.mainHandItem() != null && !mob.mainHandItem().isEmpty()) {
                            g.text(font, "  Main Hand: " + formatIdentifierForDisplay(mob.mainHandItem()),
                                    dx + PANEL_PAD + 10, currentDetailY + 4, colSubtext(), false);
                            currentDetailY += DETAIL_LINE_HEIGHT;
                        }
                        if (mob.offHandItem() != null && !mob.offHandItem().isEmpty()) {
                            g.text(font, "  Off Hand: " + formatIdentifierForDisplay(mob.offHandItem()),
                                    dx + PANEL_PAD + 10, currentDetailY + 4, colSubtext(), false);
                            currentDetailY += DETAIL_LINE_HEIGHT;
                        }
                        if (mob.armorItems() != null && !mob.armorItems().isEmpty()) {
                            String[] slots = {"Helmet", "Chestplate", "Leggings", "Boots"};
                            for (int s = 0; s < Math.min(mob.armorItems().size(), 4); s++) {
                                g.text(font, "  " + slots[s] + ": " + formatIdentifierForDisplay(mob.armorItems().get(s)),
                                        dx + PANEL_PAD + 10, currentDetailY + 4, colSubtext(), false);
                                currentDetailY += DETAIL_LINE_HEIGHT;
                            }
                        }
                        if (mob.ridingMob() != null && !mob.ridingMob().isEmpty()) {
                            g.text(font, "  Riding: " + formatIdentifierForDisplay(mob.ridingMob()),
                                    dx + PANEL_PAD + 10, currentDetailY + 4, colSubtext(), false);
                            currentDetailY += DETAIL_LINE_HEIGHT;
                        }
                        if (mob.potionEffects() != null && !mob.potionEffects().isEmpty()) {
                            g.text(font, "  Potion Effects:", dx + PANEL_PAD + 10, currentDetailY + 4, colSubtext(), false);
                            currentDetailY += DETAIL_LINE_HEIGHT;
                            for (String effect : mob.potionEffects().split(",")) {
                                String[] parts = effect.split(":");
                                if (parts.length >= 3) {
                                    String ampStr = parts[parts.length - 1];
                                    String durStr = parts[parts.length - 2];
                                    String effectId = String.join(":", Arrays.copyOfRange(parts, 0, parts.length - 2));
                                    g.text(font, "    - " + formatIdentifierForDisplay(effectId) + " (" + (("-1".equals(durStr) || "0".equals(durStr)) ? "Infinite" : durStr + "s") + ", Amp " + ampStr + ")",
                                            dx + PANEL_PAD + 20, currentDetailY + 4, colSubtext(), false);
                                    currentDetailY += DETAIL_LINE_HEIGHT;
                                }
                            }
                        }
                        if (mob.enchantments() != null && !mob.enchantments().isEmpty()) {
                            g.text(font, "  Enchantments:", dx + PANEL_PAD + 10, currentDetailY + 4, colSubtext(), false);
                            currentDetailY += DETAIL_LINE_HEIGHT;
                            for (String enchantment : mob.enchantments().split(",")) {
                                String[] parts = enchantment.split(":");
                                if (parts.length >= 3) {
                                    String lvlStr = parts[parts.length - 1];
                                    String target = parts[0];
                                    String enchantId = String.join(":", Arrays.copyOfRange(parts, 1, parts.length - 1));
                                    String targetDisplay = target;
                                    for (int t = 0; t < ENCHANT_TARGET_KEYS.length; t++) {
                                        if (ENCHANT_TARGET_KEYS[t].equals(target)) { targetDisplay = ENCHANT_TARGETS[t]; break; }
                                    }
                                    g.text(font, "    - " + formatIdentifierForDisplay(enchantId) + " (Lvl " + lvlStr + ") on " + targetDisplay,
                                            dx + PANEL_PAD + 20, currentDetailY + 4, colSubtext(), false);
                                    currentDetailY += DETAIL_LINE_HEIGHT;
                                }
                            }
                        }
                        currentY = currentDetailY + 4;
                    }
                    if (wave.mobs().isEmpty()) {
                        g.text(font, "No mobs in this wave.", dx + PANEL_PAD, guiTop() + 36, colSubtext(), false);
                    }
                }
            }
            return;
        }

        // ── OVERVIEW: no selection ──
        if (selectedArena < 0 || selectedArena >= arenas.size()) {
            g.text(font, "Select an arena or create a new one.", dx + PANEL_PAD, dt + 24, colSubtext(), false);
            return;
        }

        // ── OVERVIEW: arena selected ──
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
        int headerColor = arena.running() ? 0xFF43A047 : 0xFFCC2222;
        g.fill(dx, dt, dx + dw, dt + 16, headerColor);
        g.text(font, arena.name(), dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);
        String status = arena.running() ? "● RUNNING" : "● STOPPED";
        g.text(font, status, dx + dw - font.width(status) - PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

        g.text(font, "X:" + (int)arena.x() + " Y:" + (int)arena.y() + " Z:" + (int)arena.z(),
                dx + PANEL_PAD, dt + 20, colSubtext(), false);
        g.text(font, "Radius: " + arena.radius() + "  Delay: " + arena.delaySeconds() + "s",
                dx + PANEL_PAD, dt + 30, colSubtext(), false);

        g.fill(dx, dt + 42, dx + dw - 3, dt + 54, darkMode ? 0xFF313244 : 0xFFEEEEEE);
        g.text(font, "Waves (" + arena.waves().size() + ")", dx + PANEL_PAD, dt + 45, colHeader(), false);

        int waveAreaY = guiTop() + 56;
        int maxWaves  = (guiHeight() - 76) / ROW_H;
        for (int i = waveScroll; i < Math.min(arena.waves().size(), waveScroll + maxWaves); i++) {
            ArenaDataPayload.WaveEntry wave = arena.waves().get(i);
            int ry = waveAreaY + (i - waveScroll) * ROW_H;
            if (i % 2 == 0) g.fill(dx, ry, dx + dw - 3, ry + ROW_H, darkMode ? 0x15FFFFFF : 0x11000000);
            g.text(font, "Wave " + wave.waveNumber(), dx + PANEL_PAD, ry + 4, colText(), false);
        }
    }

    /**
     * Draws whichever autocomplete dropdown is currently active.
     * Called last so it renders on top of all widgets.
     */
    private void renderDropdown(GuiGraphicsExtractor g) {
        if (detailView != DetailView.ADD_MOB) return;

        List<SuggestionManager> managers = buildSuggestionManagerList();

        for (SuggestionManager manager : managers) {
            if (manager != null && manager.getEditBox().isFocused() && manager.hasSuggestions()) {
                int sx      = manager.getDropdownX();
                int sy      = manager.getDropdownY();
                int sw      = manager.getDropdownWidth();
                int visible = manager.getVisibleSuggestionsCount();

                sy -= addMobScroll;

                g.fill(sx, sy, sx + sw, sy + visible * 12, 0xFF333333);

                for (int i = 0; i < visible; i++) {
                    int idx = i + manager.getSuggestionScrollOffset();
                    if (idx >= manager.getFilteredSuggestions().size()) break;
                    int rowY = sy + i * 12;

                    if (idx == manager.getSelectedSuggestionIndex()) {
                        g.fill(sx, rowY, sx + sw, rowY + 12, 0xFF444488);
                    }

                    String suggestion   = manager.getSuggestion(i);
                    String typed        = manager.getTypedText();
                    int    matchStart   = suggestion.toLowerCase().indexOf(typed.toLowerCase());

                    if (matchStart >= 0) {
                        String before  = suggestion.substring(0, matchStart);
                        String matched = suggestion.substring(matchStart, matchStart + typed.length());
                        String after   = suggestion.substring(matchStart + typed.length());
                        int x1 = sx + 2;
                        int x2 = x1 + font.width(before);
                        int x3 = x2 + font.width(matched);
                        g.text(font, before,  x1, rowY + 2, 0xFFAAAAAA, false);
                        g.text(font, matched, x2, rowY + 2, 0xFFFFFF55, false);
                        g.text(font, after,   x3, rowY + 2, 0xFFAAAAAA, false);
                    } else {
                        g.text(font, suggestion, sx + 2, rowY + 2,
                                idx == manager.getSelectedSuggestionIndex() ? 0xFFFFFFFF : 0xFFAAAAAA, false);
                    }
                }
                return;
            }
        }
    }

    // ── Input handling ────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (detailView == DetailView.ADD_MOB) {
            for (SuggestionManager manager : buildSuggestionManagerList()) {
                if (manager != null && manager.getEditBox().isFocused() && manager.hasSuggestions()) {
                    if (event.isDown())         { manager.selectNextSuggestion();     return true; }
                    if (event.isUp())           { manager.selectPreviousSuggestion(); return true; }
                    if (event.isConfirmation()) { manager.applySuggestion();          return true; }
                }
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (detailView == DetailView.ADD_MOB) {
            for (SuggestionManager manager : buildSuggestionManagerList()) {
                if (manager != null && manager.getEditBox().isFocused() && manager.hasSuggestions()) {
                    int sx      = manager.getDropdownX();
                    int sy      = manager.getDropdownY() - addMobScroll;
                    int sw      = manager.getDropdownWidth();
                    int visible = manager.getVisibleSuggestionsCount();
                    int mx      = (int) event.x();
                    int my      = (int) event.y();

                    if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + visible * 12) {
                        int clicked = (my - sy) / 12;
                        if (clicked >= 0 && clicked < visible) {
                            manager.setSelectedSuggestion(clicked);
                            manager.applySuggestion();
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        if (detailView == DetailView.ADD_MOB) {
            int scrollTop    = addMobScrollTop();
            int scrollBottom = addMobScrollBottom();
            if (mx >= detailX() && mx <= detailX() + detailW() && my >= scrollTop && my <= scrollBottom) {
                // Recompute contentHeight for max scroll (mirrors buildAddMobWidgets accumulation)
                int contentHeight = 0;
                contentHeight += 10 + 18; // Mob Type
                contentHeight += 10 + 18; // Count
                contentHeight += BTN_H + 4; // Equipment toggle
                if (showEquipmentFields) {
                    contentHeight += (10 + 18) * 6;
                }
                contentHeight += BTN_H + 4; // Potion Effects toggle
                if (showPotionEffectsField) {
                    contentHeight += potionEffectEntries.size() * 14;
                    contentHeight += 10;
                    contentHeight += 18;
                    contentHeight += BTN_H + 4;
                }
                contentHeight += BTN_H + 4; // Enchantments toggle
                if (showEnchantmentsField) {
                    contentHeight += enchantmentEntries.size() * 14;
                    contentHeight += 10;
                    contentHeight += 18;
                    contentHeight += BTN_H + 4;
                }
                contentHeight += BTN_H + 4; // Riding Mob toggle
                if (showRidingMobField) {
                    contentHeight += 10 + 18;
                }

                int scrollableAreaHeight = scrollBottom - scrollTop;
                int maxScroll = Math.max(0, contentHeight - scrollableAreaHeight);
                addMobScroll = (int) Math.max(0, Math.min(addMobScroll - vertical * 10, maxScroll));
                rebuildWidgets();
                return true;
            }

            for (SuggestionManager manager : buildSuggestionManagerList()) {
                if (manager != null && manager.getEditBox().isFocused() && manager.hasSuggestions()) {
                    int sx      = manager.getDropdownX();
                    int sy      = manager.getDropdownY() - addMobScroll;
                    int sw      = manager.getDropdownWidth();
                    int visible = manager.getVisibleSuggestionsCount();
                    if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + visible * 12) {
                        manager.scrollSuggestions(vertical);
                        return true;
                    }
                }
            }
        }

        // Sidebar scroll
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
                int maxWaves  = (guiHeight() - 76) / ROW_H;
                waveScroll = (int) Math.max(0,
                        Math.min(waveScroll - vertical, Math.max(0, waveCount - maxWaves)));
                rebuildWidgets();
            }
        }

        return super.mouseScrolled(mx, my, horizontal, vertical);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<SuggestionManager> buildSuggestionManagerList() {
        List<SuggestionManager> list = new ArrayList<>();
        if (mobTypeSuggestionManager        != null) list.add(mobTypeSuggestionManager);
        if (mainHandItemSuggestionManager   != null) list.add(mainHandItemSuggestionManager);
        if (offHandItemSuggestionManager    != null) list.add(offHandItemSuggestionManager);
        if (helmetSuggestionManager         != null) list.add(helmetSuggestionManager);
        if (chestplateSuggestionManager     != null) list.add(chestplateSuggestionManager);
        if (leggingsSuggestionManager       != null) list.add(leggingsSuggestionManager);
        if (bootsSuggestionManager          != null) list.add(bootsSuggestionManager);
        if (pendingPotionSuggestionManager  != null) list.add(pendingPotionSuggestionManager);
        if (pendingEnchantSuggestionManager != null) list.add(pendingEnchantSuggestionManager);
        if (ridingMobSuggestionManager      != null) list.add(ridingMobSuggestionManager);
        return list;
    }

    /**
     * Registers a label to be drawn during renderDetailPanelBase for the ADD_MOB view.
     * Stores the UNSCROLLED logical Y; the render pass subtracts addMobScroll.
     */
    private void drawInlineLabel(int x, int y, String text) {
        inlineLabelPositions.add(new int[]{x, y});
        inlineLabelTexts.add(text);
    }

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
            LOGGER.error("", e);
        }
    }

    private EditBox makeField(int x, int y, int w, int h, String hint) {
        EditBox field = new EditBox(font, x, y, w, h, Component.literal(hint));
        field.setHint(Component.literal(hint));
        field.setBordered(true);
        addRenderableWidget(field);
        return field;
    }

    private static String formatIdentifierForDisplay(String identifier) {
        if (identifier == null || identifier.isEmpty()) return "";
        String path = identifier.contains(":") ? identifier.substring(identifier.indexOf(":") + 1) : identifier;
        return Arrays.stream(path.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static String truncate(String s) {
        return s.length() <= 11 ? s : s.substring(0, 10) + "...";
    }

    @Override
    public boolean isPauseScreen() { return false; }
}