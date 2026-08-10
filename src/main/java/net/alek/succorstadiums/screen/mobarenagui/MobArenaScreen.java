package net.alek.succorstadiums.screen.mobarenagui;

import net.alek.succorstadiums.client.ModKeyBindings;
import net.alek.succorstadiums.network.arena.ArenaActionPayload;
import net.alek.succorstadiums.network.arena.ArenaDataPayload;
import net.alek.succorstadiums.network.arena.ArenaPasteWavePayload;
import net.alek.succorstadiums.network.arena.ArenaSetRewardsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

public class MobArenaScreen extends Screen {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ── Layout constants ─────────────────────────────────────────────────────

    private static final int SIDEBAR_W = 175;
    private static final int PANEL_PAD = 8;
    private static final int BTN_H = 16;
    private static final int ROW_H = 18;

    // ── Screens ─────────────────────────────────────────────────────

    private final MobViewScreen mobViewScreen = new MobViewScreen();
    private final ArenaFormScreen arenaFormScreen = new ArenaFormScreen();
    private final WaveFormScreen waveFormScreen = new WaveFormScreen();
    private final DelMobScreen delMobScreen = new DelMobScreen();
    private final PlayerPickerScreen playerPickerScreen = new PlayerPickerScreen();
    private final RewardScreen rewardScreen = new RewardScreen();

    // ── Theme ─────────────────────────────────────────────────────

    private final GuiTheme theme = new GuiTheme();

    // ── UI state ─────────────────────────────────────────────────────────────

    private List<ArenaDataPayload.ArenaEntry> arenas = new ArrayList<>();
    private int selectedArena = -1;
    private int selectedWave  = -1;

    private enum DetailView { OVERVIEW, ADD_ARENA, VIEW_MOB, ADD_MOB, DEL_MOB, EDIT_ARENA, EDIT_WAVE, REWARD_ARENA, REWARD_WAVE }
    private DetailView detailView = DetailView.OVERVIEW;

    private boolean addMobFromEditWave = false;
    private ArenaDataPayload.MobEntry editingMobOriginal = null;

    private final AddMobScreen addMobScreen = new AddMobScreen();

    private boolean showPlayerPicker = false;

    private boolean showNewWavePrompt = false;
    private EditBox newWaveNameBox;
    private EditBox newWaveDelayBox;

    // ── Wave clipboard ───────────────────────────────────────────────────────

    private static ArenaDataPayload.WaveEntry copiedWave = null;
    private String pasteError = "";

    // ── Arena groups (sidebar collapse state) ────────────────────────────────

    private static final String UNGROUPED_LABEL = "Ungrouped";
    private final Set<String> collapsedGroups = new HashSet<>();

    // ── Rewards ───────────────────────────────────────────────────────────────

    // Holds rewards for a NOT-YET-CREATED arena (client-side only, until Create is
    // pressed). Editing an already-existing arena's or wave's rewards saves straight
    // to the server instead — see buildRewardArenaWidgets()/buildRewardWaveWidgets().
    private List<ArenaDataPayload.RewardEntry> pendingArenaRewards = new ArrayList<>();
    // Which DetailView the reward screen's Save/Cancel should return to.
    private DetailView rewardReturnView = DetailView.OVERVIEW;

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
        if (showNewWavePrompt) return;
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

    // ── Sidebar grouping ──────────────────────────────────────────────────────

    /** Sealed-ish row descriptor: either a group header (String) or an arena index (Integer). */
    private List<Object> buildSidebarRows() {
        List<Object> rows = new ArrayList<>();
        Map<String, List<Integer>> byGroup = new LinkedHashMap<>();
        List<Integer> ungrouped = new ArrayList<>();

        for (int i = 0; i < arenas.size(); i++) {
            String g = arenas.get(i).group();
            if (g == null || g.isBlank()) {
                ungrouped.add(i);
            } else {
                byGroup.computeIfAbsent(g, k -> new ArrayList<>()).add(i);
            }
        }

        List<String> sortedGroups = new ArrayList<>(byGroup.keySet());
        sortedGroups.sort(String.CASE_INSENSITIVE_ORDER);

        for (String g : sortedGroups) {
            rows.add(g);
            if (!collapsedGroups.contains(g)) {
                rows.addAll(byGroup.get(g));
            }
        }

        if (!ungrouped.isEmpty()) {
            rows.add(UNGROUPED_LABEL);
            if (!collapsedGroups.contains(UNGROUPED_LABEL)) {
                rows.addAll(ungrouped);
            }
        }

        return rows;
    }

    // ── Widget construction ───────────────────────────────────────────────────

    @Override
    protected void rebuildWidgets() {
        clearWidgets();

        addRenderableWidget(Button.builder(
                Component.literal("Theme: " + theme.getThemeName()),
                btn -> { theme.nextTheme(); rebuildWidgets(); }
        ).bounds(sidebarX(), guiTop() - 14, SIDEBAR_W, 12).build());

        buildSidebarButtons();

        if (showNewWavePrompt)                         buildNewWavePromptWidgets();
        else if (showPlayerPicker)                     buildPlayerPickerButtons();
        else if (detailView == DetailView.ADD_ARENA)   buildAddArenaWidgets();
        else if (detailView == DetailView.VIEW_MOB)    buildViewMobWidgets();
        else if (detailView == DetailView.ADD_MOB)     buildAddMobWidgets();
        else if (detailView == DetailView.DEL_MOB)     buildDelMobWidgets();
        else if (detailView == DetailView.EDIT_ARENA)  buildEditArenaWidgets();
        else if (detailView == DetailView.EDIT_WAVE)   buildEditWaveWidgets();
        else if (detailView == DetailView.REWARD_ARENA) buildRewardArenaWidgets();
        else if (detailView == DetailView.REWARD_WAVE)  buildRewardWaveWidgets();
        else                                            buildDetailButtons();
    }

    private void buildSidebarButtons() {
        int x = sidebarX() + PANEL_PAD;
        int y = guiTop() + 24;
        int maxVisible = (guiHeight() - 40) / ROW_H;

        List<Object> rows = buildSidebarRows();

        for (int i = arenaScroll; i < Math.min(rows.size(), arenaScroll + maxVisible); i++) {
            Object row = rows.get(i);
            int ry = y + (i - arenaScroll) * ROW_H;

            if (row instanceof String groupName) {
                boolean collapsed = collapsedGroups.contains(groupName);
                int available = SIDEBAR_W - PANEL_PAD * 2 - 14;
                addRenderableWidget(Button.builder(
                        Component.literal((collapsed ? "▶ " : "▼ ") + truncate(groupName, available)),
                        btn -> {
                            if (collapsed) collapsedGroups.remove(groupName);
                            else collapsedGroups.add(groupName);
                            rebuildWidgets();
                        }
                ).bounds(x, ry, SIDEBAR_W - PANEL_PAD * 2, BTN_H).build());
            } else {
                final int idx = (Integer) row;
                int available = SIDEBAR_W - PANEL_PAD * 2 - 10;
                String label = truncate(arenas.get(idx).name(), available);
                addRenderableWidget(Button.builder(Component.literal("  " + label),
                        btn -> {
                            selectedArena = idx;
                            selectedWave  = -1;
                            waveScroll    = 0;
                            detailView    = DetailView.OVERVIEW;
                            pasteError    = "";
                            rebuildWidgets();
                        }
                ).bounds(x, ry, SIDEBAR_W - PANEL_PAD * 2, BTN_H).build());
            }
        }

        addRenderableWidget(Button.builder(Component.literal("+ New Arena"),
                btn -> {
                    detailView = DetailView.ADD_ARENA;
                    selectedArena = -1;
                    pendingArenaRewards = new ArrayList<>();
                    rebuildWidgets();
                }
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
                btn -> { showNewWavePrompt = true; rebuildWidgets(); }
        ).bounds(bx + 84, by, 50, BTN_H).build());

        Button pasteBtn = Button.builder(Component.literal("⧉ Paste"),
                btn -> {
                    if (copiedWave == null) {
                        pasteError = "No wave copied. Use ⧉ on a wave to copy it first.";
                        rebuildWidgets();
                        return;
                    }
                    pasteError = "";
                    ClientPlayNetworking.send(new ArenaPasteWavePayload(
                            arena.name(), copiedWave.name(),
                            copiedWave.delaySeconds() != null ? copiedWave.delaySeconds() : -1,
                            copiedWave.mobs()
                    ));
                }
        ).bounds(bx + 140, by, 60, BTN_H).build();
        addRenderableWidget(pasteBtn);

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

            addRenderableWidget(Button.builder(Component.literal("⧉"),
                    btn -> { copiedWave = wave; pasteError = ""; }
            ).bounds(detailX() + PANEL_PAD, wy + 1, 14, 16).build());

            addRenderableWidget(Button.builder(Component.literal("✎"),
                    btn -> {
                        selectedWave = waveNum;
                        waveFormScreen.resetScroll();
                        detailView = DetailView.EDIT_WAVE;
                        rebuildWidgets();
                    }
            ).bounds(detailX() + PANEL_PAD + 16, wy + 1, 14, 16).build());

            int totalArrowWidth = 14 + 2 + 14 + 6;
            int totalBtnWidth = totalArrowWidth + 68 + 4 + 68 + 4 + 68 + 4 + 80;
            int btnStart = detailX() + detailW() - PANEL_PAD - totalBtnWidth;

            Button upBtn = Button.builder(Component.literal("▲"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.moveWaveUp(arena.name(), waveNum))
            ).bounds(btnStart, wy + 1, 14, 16).build();
            upBtn.active = i > 0;
            addRenderableWidget(upBtn);

            Button downBtn = Button.builder(Component.literal("▼"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.moveWaveDown(arena.name(), waveNum))
            ).bounds(btnStart + 16, wy + 1, 14, 16).build();
            downBtn.active = i < arena.waves().size() - 1;
            addRenderableWidget(downBtn);

            int actionStart = btnStart + 34;

            addRenderableWidget(Button.builder(Component.literal("View Mobs"),
                    btn -> { selectedWave = waveNum; detailView = DetailView.VIEW_MOB; rebuildWidgets(); }
            ).bounds(actionStart, wy + 1, 68, 16).build());

            addRenderableWidget(Button.builder(Component.literal("+ Add Mobs"),
                    btn -> {
                        selectedWave = waveNum;
                        addMobFromEditWave = false;
                        editingMobOriginal = null;
                        detailView = DetailView.ADD_MOB;
                        rebuildWidgets();
                    }
            ).bounds(actionStart + 72, wy + 1, 68, 16).build());

            addRenderableWidget(Button.builder(Component.literal("- Del Mobs"),
                    btn -> { selectedWave = waveNum; detailView = DetailView.DEL_MOB; rebuildWidgets(); }
            ).bounds(actionStart + 144, wy + 1, 68, 16).build());

            addRenderableWidget(Button.builder(Component.literal("✕ Delete Wave"),
                    btn -> ClientPlayNetworking.send(ArenaActionPayload.removeWave(arena.name(), waveNum))
            ).bounds(actionStart + 216, wy + 1, 80, 16).build());
        }
    }

    private void buildNewWavePromptWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) { showNewWavePrompt = false; return; }
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
        int nextWaveNum = arena.waves().size() + 1;

        int boxW = 220, boxH = 118;
        int bx = guiLeft() + (guiWidth() - boxW) / 2;
        int by = guiTop() + (guiHeight() - boxH) / 2;

        newWaveNameBox = new EditBox(font, bx + PANEL_PAD, by + 32, boxW - PANEL_PAD * 2, 16,
                Component.literal("Wave name"));
        newWaveNameBox.setBordered(true);
        newWaveNameBox.setMaxLength(24);
        newWaveNameBox.setValue("Wave " + nextWaveNum);
        addRenderableWidget(newWaveNameBox);

        newWaveDelayBox = new EditBox(font, bx + PANEL_PAD, by + 66, boxW - PANEL_PAD * 2, 16,
                Component.literal("Delay (s)"));
        newWaveDelayBox.setBordered(true);
        newWaveDelayBox.setMaxLength(6);
        newWaveDelayBox.setValue(String.valueOf(arena.delaySeconds()));
        addRenderableWidget(newWaveDelayBox);

        this.setFocused(newWaveNameBox);
        newWaveNameBox.setFocused(true);

        addRenderableWidget(Button.builder(Component.literal("✔ Create"),
                btn -> {
                    String name = newWaveNameBox.getValue().trim();
                    String delayText = newWaveDelayBox.getValue().trim();
                    int delay = -1;
                    if (!delayText.isEmpty()) {
                        try {
                            delay = Integer.parseInt(delayText);
                        } catch (NumberFormatException ignored) {
                            return;
                        }
                    }
                    ClientPlayNetworking.send(ArenaActionPayload.addWaveFull(arena.name(), name, delay));
                    showNewWavePrompt = false;
                    rebuildWidgets();
                }
        ).bounds(bx + PANEL_PAD, by + boxH - BTN_H - 6, 90, BTN_H).build());

        addRenderableWidget(Button.builder(Component.literal("✕ Cancel"),
                btn -> { showNewWavePrompt = false; rebuildWidgets(); }
        ).bounds(bx + boxW - PANEL_PAD - 90, by + boxH - BTN_H - 6, 90, BTN_H).build());
    }

    private void buildAddArenaWidgets() {
        arenaFormScreen.buildWidgets(this::addRenderableWidget, font,
                detailX(), detailW(), guiTop(), guiHeight(), null, pendingArenaRewards.size(),
                (name, x, y, z, radius, delay, group) -> {
                    ClientPlayNetworking.send(ArenaActionPayload.createArena(name, x, y, z, radius, delay));
                    if (!group.isEmpty()) {
                        ClientPlayNetworking.send(ArenaActionPayload.setArenaGroup(name, group));
                    }
                    if (!pendingArenaRewards.isEmpty()) {
                        ClientPlayNetworking.send(new ArenaSetRewardsPayload(name, 0, pendingArenaRewards));
                    }
                    pendingArenaRewards = new ArrayList<>();
                    detailView = DetailView.OVERVIEW;
                    rebuildWidgets();
                },
                () -> {
                    pendingArenaRewards = new ArrayList<>();
                    detailView = DetailView.OVERVIEW;
                    rebuildWidgets();
                },
                () -> {
                    rewardScreen.prefill(pendingArenaRewards);
                    rewardReturnView = DetailView.ADD_ARENA;
                    detailView = DetailView.REWARD_ARENA;
                    rebuildWidgets();
                });
    }

    private void buildEditArenaWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) return;
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
        arenaFormScreen.buildWidgets(this::addRenderableWidget, font,
                detailX(), detailW(), guiTop(), guiHeight(), arena, arena.rewards().size(),
                (newName, x, y, z, radius, delay, group) -> {
                    ClientPlayNetworking.send(ArenaActionPayload.editArena(arena.name(), newName, x, y, z, radius, delay));
                    String targetName = newName.isEmpty() ? arena.name() : newName;
                    ClientPlayNetworking.send(ArenaActionPayload.setArenaGroup(targetName, group));
                    detailView = DetailView.OVERVIEW;
                    rebuildWidgets();
                },
                () -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); },
                () -> {
                    rewardScreen.prefill(arena.rewards());
                    rewardReturnView = DetailView.EDIT_ARENA;
                    detailView = DetailView.REWARD_ARENA;
                    rebuildWidgets();
                });
    }

    private void buildRewardArenaWidgets() {
        boolean editingExisting = rewardReturnView == DetailView.EDIT_ARENA
                && selectedArena >= 0 && selectedArena < arenas.size();
        ArenaDataPayload.ArenaEntry arena = editingExisting ? arenas.get(selectedArena) : null;

        rewardScreen.buildWidgets(this::addRenderableWidget, font,
                detailX(), detailW(), guiTop(), guiHeight(),
                (rewards) -> {
                    if (editingExisting) {
                        ClientPlayNetworking.send(new ArenaSetRewardsPayload(arena.name(), 0, rewards));
                    } else {
                        pendingArenaRewards = rewards;
                    }
                    detailView = rewardReturnView;
                    rebuildWidgets();
                },
                () -> { detailView = rewardReturnView; rebuildWidgets(); },
                this::rebuildWidgets);
    }

    private void buildRewardWaveWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) { detailView = DetailView.OVERVIEW; rebuildWidgets(); return; }
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);

        rewardScreen.buildWidgets(this::addRenderableWidget, font,
                detailX(), detailW(), guiTop(), guiHeight(),
                (rewards) -> {
                    ClientPlayNetworking.send(new ArenaSetRewardsPayload(arena.name(), selectedWave, rewards));
                    detailView = DetailView.EDIT_WAVE;
                    rebuildWidgets();
                },
                () -> { detailView = DetailView.EDIT_WAVE; rebuildWidgets(); },
                this::rebuildWidgets);
    }

    private void buildEditWaveWidgets() {
        if (selectedArena < 0 || selectedArena >= arenas.size()) { detailView = DetailView.OVERVIEW; return; }
        ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
        ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
        if (wave == null) { detailView = DetailView.OVERVIEW; rebuildWidgets(); return; }

        waveFormScreen.buildWidgets(this::addRenderableWidget, font,
                detailX(), detailW(), guiTop(), guiHeight(), wave, arena.delaySeconds(),
                (name, delay) -> {
                    ClientPlayNetworking.send(ArenaActionPayload.renameWave(arena.name(), selectedWave, name));
                    ClientPlayNetworking.send(ArenaActionPayload.setWaveDelay(arena.name(), selectedWave, delay != null ? delay : -1));
                    detailView = DetailView.OVERVIEW;
                    rebuildWidgets();
                },
                () -> { detailView = DetailView.OVERVIEW; rebuildWidgets(); },
                (mob, count) -> ClientPlayNetworking.send(
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
                ),
                (mob) -> {
                    editingMobOriginal = mob;
                    addMobFromEditWave = true;
                    addMobScreen.prefill(mob);
                    detailView = DetailView.ADD_MOB;
                    rebuildWidgets();
                },
                () -> {
                    rewardScreen.prefill(wave.rewards());
                    rewardReturnView = DetailView.EDIT_WAVE;
                    detailView = DetailView.REWARD_WAVE;
                    rebuildWidgets();
                });
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

                    if (editingMobOriginal != null) {
                        ArenaDataPayload.MobEntry original = editingMobOriginal;
                        ClientPlayNetworking.send(ArenaActionPayload.removeMob(
                                arena.name(), selectedWave,
                                original.mobType(), original.count(), original.size(),
                                original.ridingMob(), original.mainHandItem(), original.offHandItem(),
                                original.armorItems()
                        ));
                    }

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

                    editingMobOriginal = null;
                    detailView = addMobFromEditWave ? DetailView.EDIT_WAVE : DetailView.OVERVIEW;
                    rebuildWidgets();
                },

                () -> {
                    editingMobOriginal = null;
                    detailView = addMobFromEditWave ? DetailView.EDIT_WAVE : DetailView.OVERVIEW;
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
        renderNewWavePrompt(g);

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
            arenaFormScreen.render(g, font, theme, dx, dt, dw, "New Arena");
            return;
        }

        if (detailView == DetailView.EDIT_ARENA) {
            if (selectedArena < 0 || selectedArena >= arenas.size()) return;
            ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
            arenaFormScreen.render(g, font, theme, dx, dt, dw, "Edit Arena: " + arena.name());
            return;
        }

        if (detailView == DetailView.REWARD_ARENA) {
            String title = (rewardReturnView == DetailView.EDIT_ARENA
                    && selectedArena >= 0 && selectedArena < arenas.size())
                    ? "Rewards: " + arenas.get(selectedArena).name()
                    : "Rewards: New Arena";
            rewardScreen.render(g, font, theme, dx, dt, dw, guiTop(), guiHeight(), title);
            return;
        }

        if (detailView == DetailView.REWARD_WAVE) {
            rewardScreen.render(g, font, theme, dx, dt, dw, guiTop(), guiHeight(), "Rewards: Wave " + selectedWave);
            return;
        }

        if (detailView == DetailView.EDIT_WAVE) {
            if (selectedArena < 0 || selectedArena >= arenas.size()) return;
            ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
            ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                    .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
            String label = (wave != null && wave.name() != null && !wave.name().isEmpty())
                    ? wave.name() : "Wave " + selectedWave;
            waveFormScreen.render(g, font, theme, dx, dt, dw, guiTop(), guiHeight(), "Edit Wave: " + label, wave);
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

        String groupLine = "X:" + (int)arena.x() + " Y:" + (int)arena.y() + " Z:" + (int)arena.z()
                + "   Group: " + (arena.group() != null ? arena.group() : UNGROUPED_LABEL);
        g.text(font, groupLine, dx + PANEL_PAD, dt + 20, theme.subtext(), false);
        g.text(font, "Radius: " + arena.radius() + "  Delay: " + arena.delaySeconds() + "s"
                        + "   Rewards: " + arena.rewards().size(),
                dx + PANEL_PAD, dt + 30, theme.subtext(), false);

        g.fill(dx, dt + 42, dx + dw - 3, dt + 54, theme.getTheme() != Theme.LIGHT ? 0x15FFFFFF : 0x11000000);
        g.text(font, "Waves (" + arena.waves().size() + ")", dx + PANEL_PAD, dt + 45, theme.header(), false);

        int waveAreaY = guiTop() + 56;
        int maxWaves  = (guiHeight() - 76) / ROW_H;
        for (int i = waveScroll; i < Math.min(arena.waves().size(), waveScroll + maxWaves); i++) {
            ArenaDataPayload.WaveEntry wave = arena.waves().get(i);
            int ry = waveAreaY + (i - waveScroll) * ROW_H;
            if (i % 2 == 0) g.fill(dx, ry, dx + dw - 3, ry + ROW_H, theme.getTheme() != Theme.LIGHT ? 0x15FFFFFF : 0x11000000);
            String label = (wave.name() != null && !wave.name().isEmpty())
                    ? wave.name() : "Wave " + wave.waveNumber();
            if (!wave.rewards().isEmpty()) {
                label += " 🎁" + wave.rewards().size();
            }
            g.text(font, label, dx + PANEL_PAD + 34, ry + 4, theme.text(), false);
        }

        if (!pasteError.isEmpty()) {
            int by = guiTop() + guiHeight() - BTN_H - PANEL_PAD;
            g.text(font, pasteError, dx + PANEL_PAD, by - 12, 0xFFFF5555, false);
        }
    }

    private void renderDropdown(GuiGraphicsExtractor g) {
        if (detailView == DetailView.ADD_MOB) {
            addMobScreen.renderDropdown(g, font);
            return;
        }
        if (detailView == DetailView.REWARD_ARENA || detailView == DetailView.REWARD_WAVE) {
            rewardScreen.renderDropdown(g, font);
        }
    }

    private void renderNewWavePrompt(GuiGraphicsExtractor g) {
        if (!showNewWavePrompt) return;

        int boxW = 220, boxH = 118;
        int bx = guiLeft() + (guiWidth() - boxW) / 2;
        int by = guiTop() + (guiHeight() - boxH) / 2;

        g.fill(guiLeft(), guiTop(), guiLeft() + guiWidth(), guiTop() + guiHeight(), 0x80000000);
        g.fill(bx, by, bx + boxW, by + boxH, theme.panel());
        g.outline(bx, by, boxW, boxH, theme.border());
        g.text(font, "New Wave", bx + PANEL_PAD, by + 6, theme.header(), false);
        g.text(font, "Name", bx + PANEL_PAD, by + 22, theme.subtext(), false);
        g.text(font, "Delay (s)", bx + PANEL_PAD, by + 56, theme.subtext(), false);
    }

    // ── Input handling ────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (!(this.getFocused() instanceof net.minecraft.client.gui.components.EditBox)) {
            if (ModKeyBindings.OPEN_MOB_ARENA_GUI.matches(event)) {
                this.onClose();
                return true;
            }
        }

        if (detailView == DetailView.ADD_MOB && addMobScreen.keyPressed(event)) {
            return true;
        }

        if ((detailView == DetailView.REWARD_ARENA || detailView == DetailView.REWARD_WAVE)
                && rewardScreen.keyPressed(event)) {
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
        if (showNewWavePrompt) {
            return true;
        }

        if (detailView == DetailView.ADD_MOB
                && addMobScreen.mouseScrolled(mx, my, vertical,
                detailX(), detailW(), guiTop(), guiHeight(), this::rebuildWidgets)) {
            return true;
        }

        if (detailView == DetailView.EDIT_WAVE && selectedArena >= 0 && selectedArena < arenas.size()) {
            ArenaDataPayload.ArenaEntry arena = arenas.get(selectedArena);
            ArenaDataPayload.WaveEntry wave = arena.waves().stream()
                    .filter(w -> w.waveNumber() == selectedWave).findFirst().orElse(null);
            if (waveFormScreen.mouseScrolled(mx, my, vertical,
                    detailX(), detailW(), guiTop(), guiHeight(), wave, this::rebuildWidgets)) {
                return true;
            }
        }

        int maxVisible = (guiHeight() - 40) / ROW_H;
        if (mx >= sidebarX() && mx <= sidebarX() + SIDEBAR_W
                && my >= guiTop() && my <= guiTop() + guiHeight()) {
            int rowCount = buildSidebarRows().size();
            arenaScroll = (int) Math.max(0,
                    Math.min(arenaScroll - vertical, Math.max(0, rowCount - maxVisible)));
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

    private String truncate(String s, int maxWidth) {
        if (font.width(s) <= maxWidth) return s;
        String ellipsis = "…";
        int ellipsisWidth = font.width(ellipsis);
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (font.width(sb.toString() + c) + ellipsisWidth > maxWidth) break;
            sb.append(c);
        }
        return sb + ellipsis;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}