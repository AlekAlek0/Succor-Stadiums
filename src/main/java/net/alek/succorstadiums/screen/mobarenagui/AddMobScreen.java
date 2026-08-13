package net.alek.succorstadiums.screen.mobarenagui;

import net.alek.succorstadiums.network.arena.ArenaDataPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

/**
 * AddMobScreen owns the "Add Mob(s) to Wave" form: mob type/count/variant,
 * the collapsible Equipment / Potion Effects / Enchantments / Riding Mob
 * sections, their suggestion dropdowns, and the scroll state for the panel.
 *
 * This mirrors ArenaFormScreen / MobViewScreen / DelMobScreen / PlayerPickerScreen:
 * the owning MobArenaScreen calls buildWidgets() from rebuildWidgets(), render()
 * from its render pass, and forwards input events while DetailView.ADD_MOB is active.
 *
 * Unlike the simpler detail views, this one keeps its own scroll offset and a raft
 * of suggestion managers, so it also exposes renderDropdown()/keyPressed()/
 * mouseClicked()/mouseScrolled() hooks for the parent to forward into.
 */
public class AddMobScreen {

    private static final int BTN_H = 16;
    private static final int PANEL_PAD = 8;
    private static final int MAX_VISIBLE_SUGGESTIONS = 8;

    public static final String[] ENCHANT_TARGETS     = {"Main Hand", "Off Hand", "Helmet", "Chestplate", "Leggings", "Boots"};
    public static final String[] ENCHANT_TARGET_KEYS = {"mainhand",  "offhand",  "helmet", "chestplate", "leggings", "boots"};

    /** Fired when the user presses "Add" with valid input. */
    public interface SubmitListener {
        void onSubmit(String mobType, int count, Integer size, String ridingMob,
                      String mainHandItem, String offHandItem, List<String> armorItems,
                      String potionEffects, String enchantments);
    }

    // ── Collapsible section toggles ──────────────────────────────────────────
    private boolean showEquipmentFields    = false;
    private boolean showRidingMobField     = false;
    private boolean showPotionEffectsField = false;
    private boolean showEnchantmentsField  = false;
    private boolean editMode = false;

    // ── Multi-entry lists for potion effects and enchantments ────────────────

    /** Each entry: {effectId, durationSeconds, amplifier} */
    private final List<String[]> potionEffectEntries = new ArrayList<>();
    /** Each entry: {enchantId, level, targetKey} */
    private final List<String[]> enchantmentEntries  = new ArrayList<>();

    /** Which target slot the pending-enchant row currently points to. */
    private int pendingEnchantTargetIndex = 0;

    // ── Backing fields: survive rebuildWidgets() so toggling sections doesn't wipe typed values ──

    private String savedMobType    = "";
    private String savedMobCount   = "";
    private String savedMobVariant = "";

    private String selectedSlimeVariant  = "";
    private String selectedZombieVariant = "";
    private String validationError       = "";

    private String savedMainHand   = "";
    private String savedOffHand    = "";
    private String savedHelmet     = "";
    private String savedChestplate = "";
    private String savedLeggings   = "";
    private String savedBoots      = "";

    private String savedRidingMob  = "";

    // ── Input fields ──────────────────────────────────────────────────────────

    private EditBox mobTypeField, mobCountField;
    private EditBox mainHandItemField, offHandItemField;
    private EditBox helmetField, chestplateField, leggingsField, bootsField;
    private EditBox ridingMobField;

    private EditBox pendingPotionIdField, pendingPotionDurationField, pendingPotionAmplifierField;
    private String pendingPotionId = "";
    private String pendingPotionDuration = "";
    private String pendingPotionAmplifier = "";

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

    // ── Inline label lists (populated during buildWidgets, drawn in render) ──

    private final List<int[]>  inlineLabelPositions = new ArrayList<>();
    private final List<String> inlineLabelTexts     = new ArrayList<>();

    // ── Scroll / misc state ───────────────────────────────────────────────────

    private int addMobScroll = 0;
    private int waveNumber = -1;

    // ── Layout helpers ────────────────────────────────────────────────────────

    /** Y where scrollable content begins (just below the header). */
    public int scrollTop(int guiTop) { return guiTop + 16; }

    /** Y where scrollable content ends (just above the pinned Add/Cancel buttons). */
    public int scrollBottom(int guiTop, int guiHeight) { return guiTop + guiHeight - BTN_H - PANEL_PAD; }

    /**
     * How tall the form's scrollable content is right now, given the current
     * toggle states and entry counts. Single source of truth — both buildWidgets()
     * and mouseScrolled() call this instead of each keeping their own copy of the math.
     */
    private int computeContentHeight() {
        int h = 10 + 18; // Mob Type: label + field
        h += 10 + 18;    // Count + Variant row

        h += BTN_H + 4;  // Equipment toggle
        if (showEquipmentFields) {
            h += (10 + 18) * 6; // 6 fields: main hand, offhand, helmet, chest, legs, boots
        }

        h += BTN_H + 4;  // Potion Effects toggle
        if (showPotionEffectsField) {
            h += potionEffectEntries.size() * 14; // existing entries
            h += 10;                              // column header labels
            h += 18;                              // pending-entry input row
            h += BTN_H + 4;                        // "+ Add Effect" button
        }

        h += BTN_H + 4;  // Enchantments toggle
        if (showEnchantmentsField) {
            h += enchantmentEntries.size() * 14;
            h += 10;
            h += 18;
            h += BTN_H + 4;
        }

        h += BTN_H + 4;  // Riding Mob toggle
        if (showRidingMobField) {
            h += 10 + 18;
        }

        return h;
    }

    // ── Widget construction ───────────────────────────────────────────────────

    /**
     * @param adder       normally {@code screen::addRenderableWidget}
     * @param font        screen's font
     * @param detailX     left edge of the detail panel
     * @param detailW     width of the detail panel
     * @param guiTop      top edge of the whole GUI
     * @param guiHeight   height of the whole GUI
     * @param waveNumber  the wave these mobs are being added to (just kept for reference/labels)
     * @param onSubmit    called with the fully validated form data when "Add" succeeds
     * @param onCancel    called when "Cancel" is pressed
     */
    public void buildWidgets(Consumer<AbstractWidget> adder, Font font,
                             int detailX, int detailW, int guiTop, int guiHeight,
                             int waveNumber, SubmitListener onSubmit,
                             Runnable onCancel,
                             Runnable rebuildScreen) {
        this.waveNumber = waveNumber;
        inlineLabelPositions.clear();
        inlineLabelTexts.clear();

        int cx = detailX + PANEL_PAD;
        int fw = detailW - PANEL_PAD * 2;
        int currentY = guiTop + 20;
        int by = scrollBottom(guiTop, guiHeight);

        int scrollTop    = scrollTop(guiTop);
        int scrollBottom = scrollBottom(guiTop, guiHeight);

        // ── Mob Type ──────────────────────────────────────────────────────────
        drawInlineLabel(cx, currentY, "Mob Type");
        currentY += 10;
        mobTypeField = addScrolledField(adder, font, cx, currentY, fw, 14, "e.g. minecraft:zombie", scrollTop, scrollBottom);
        if (mobTypeField != null) {
            mobTypeField.setValue(savedMobType);
            mobTypeSuggestionManager = new SuggestionManager(
                    mobTypeField, BuiltInRegistries.ENTITY_TYPE, MAX_VISIBLE_SUGGESTIONS, 14, false);
            mobTypeField.setResponder(text -> {
                String oldMobType = savedMobType;
                savedMobType = text;
                mobTypeSuggestionManager.filterSuggestions(text);

                boolean oldIsSlime = oldMobType.equals("minecraft:slime") || oldMobType.equals(MOD_ID + ":banana_slime");
                boolean oldIsZombieLike = oldMobType.equals("minecraft:zombie") || oldMobType.equals("minecraft:zombie_villager") || oldMobType.equals(MOD_ID + ":farmbie");

                boolean newIsSlime = savedMobType.equals("minecraft:slime") || savedMobType.equals(MOD_ID + ":banana_slime");
                boolean newIsZombieLike = savedMobType.equals("minecraft:zombie") || savedMobType.equals("minecraft:zombie_villager") || savedMobType.equals(MOD_ID + ":farmbie");

                if ((oldIsSlime != newIsSlime) || (oldIsZombieLike != newIsZombieLike)) {
                    rebuildScreen.run();
                }
            });
        }
        currentY += 18;

        // ── Count and Variant ────────────────────────────────────────────────────
        drawInlineLabel(cx, currentY, "Count");
        int countFieldWidth = fw / 2 - 2;
        mobCountField = addScrolledField(adder, font, cx, currentY + 10, countFieldWidth, 14, "1", scrollTop, scrollBottom);
        if (mobCountField != null) {
            mobCountField.setValue(savedMobCount);
            mobCountField.setResponder(text -> savedMobCount = text);
        }

        String currentMobType = mobTypeField != null ? mobTypeField.getValue().trim() : savedMobType;
        boolean isSlime = currentMobType.equals("minecraft:slime") || currentMobType.equals(MOD_ID + ":banana_slime");
        boolean isZombieLike = currentMobType.equals("minecraft:zombie") || currentMobType.equals("minecraft:zombie_villager") || currentMobType.equals(MOD_ID + ":farmbie");

        if (isSlime) {
            drawInlineLabel(cx + countFieldWidth + 4, currentY, "Variant");
            int buttonWidth = (fw - countFieldWidth - 4 - 4) / 3;
            int buttonX = cx + countFieldWidth + 4;

            addScrolledButton(adder,
                    Component.literal("Small" + (selectedSlimeVariant.equals("small") ? " ✔" : "")),
                    btn -> { selectedSlimeVariant = "small"; savedMobVariant = "small"; rebuildScreen.run(); },
                    buttonX, currentY + 10, buttonWidth, BTN_H, scrollTop, scrollBottom
            );
            addScrolledButton(adder,
                    Component.literal("Medium" + (selectedSlimeVariant.equals("medium") ? " ✔" : "")),
                    btn -> { selectedSlimeVariant = "medium"; savedMobVariant = "medium"; rebuildScreen.run(); },
                    buttonX + buttonWidth + 2, currentY + 10, buttonWidth, BTN_H, scrollTop, scrollBottom
            );
            addScrolledButton(adder,
                    Component.literal("Large" + (selectedSlimeVariant.equals("large") ? " ✔" : "")),
                    btn -> { selectedSlimeVariant = "large"; savedMobVariant = "large"; rebuildScreen.run(); },
                    buttonX + (buttonWidth + 2) * 2, currentY + 10, buttonWidth, BTN_H, scrollTop, scrollBottom
            );
            currentY += 10 + BTN_H + 4;
        } else if (isZombieLike) {
            drawInlineLabel(cx + countFieldWidth + 4, currentY, "Variant");
            int buttonWidth = (fw - countFieldWidth - 4 - 2) / 2;
            int buttonX = cx + countFieldWidth + 4;

            addScrolledButton(adder,
                    Component.literal("Baby" + (selectedZombieVariant.equals("baby") ? " ✔" : "")),
                    btn -> { selectedZombieVariant = "baby"; savedMobVariant = "baby"; rebuildScreen.run(); },
                    buttonX, currentY + 10, buttonWidth, BTN_H, scrollTop, scrollBottom
            );
            addScrolledButton(adder,
                    Component.literal("Adult" + (selectedZombieVariant.equals("adult") ? " ✔" : "")),
                    btn -> { selectedZombieVariant = "adult"; savedMobVariant = "adult"; rebuildScreen.run(); },
                    buttonX + buttonWidth + 2, currentY + 10, buttonWidth, BTN_H, scrollTop, scrollBottom
            );
            currentY += 10 + BTN_H + 4;
        } else {
            savedMobVariant = "";
            selectedSlimeVariant = "";
            selectedZombieVariant = "";
            currentY += 10 + 18;
        }

        // ── Equipment toggle ──────────────────────────────────────────────────
        int equipmentCount = 0;
        if (!savedMainHand.isBlank()) equipmentCount++;
        if (!savedOffHand.isBlank()) equipmentCount++;
        if (!savedHelmet.isBlank()) equipmentCount++;
        if (!savedChestplate.isBlank()) equipmentCount++;
        if (!savedLeggings.isBlank()) equipmentCount++;
        if (!savedBoots.isBlank()) equipmentCount++;

        addScrolledButton(adder,
                Component.literal(
                        (showEquipmentFields ? "▼" : "▶")
                                + " Equipment"
                                + (equipmentCount == 0 ? "" : " (" + equipmentCount + ")")
                ),
                btn -> {
                    showEquipmentFields = !showEquipmentFields;
                    rebuildScreen.run();
                },
                cx, currentY, fw, BTN_H, scrollTop, scrollBottom
        );
        currentY += BTN_H + 4;

        if (showEquipmentFields) {
            drawInlineLabel(cx, currentY, "Main Hand Item"); currentY += 10;
            mainHandItemField = addScrolledField(adder, font, cx, currentY, fw, 14, "e.g. minecraft:diamond_sword", scrollTop, scrollBottom);
            if (mainHandItemField != null) {
                mainHandItemField.setValue(savedMainHand);
                mainHandItemSuggestionManager = new SuggestionManager(
                        mainHandItemField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                mainHandItemField.setResponder(text -> {
                    savedMainHand = text;
                    mainHandItemSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18;

            drawInlineLabel(cx, currentY, "Off Hand Item"); currentY += 10;
            offHandItemField = addScrolledField(adder, font, cx, currentY, fw, 14, "e.g. minecraft:shield", scrollTop, scrollBottom);
            if (offHandItemField != null) {
                offHandItemField.setValue(savedOffHand);
                offHandItemSuggestionManager = new SuggestionManager(
                        offHandItemField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                offHandItemField.setResponder(text -> {
                    savedOffHand = text;
                    offHandItemSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18;

            drawInlineLabel(cx, currentY, "Helmet"); currentY += 10;
            helmetField = addScrolledField(adder, font, cx, currentY, fw, 14, "e.g. minecraft:diamond_helmet", scrollTop, scrollBottom);
            if (helmetField != null) {
                helmetField.setValue(savedHelmet);
                helmetSuggestionManager = new SuggestionManager(
                        helmetField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                helmetField.setResponder(text -> {
                    savedHelmet = text;
                    helmetSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18;

            drawInlineLabel(cx, currentY, "Chestplate"); currentY += 10;
            chestplateField = addScrolledField(adder, font, cx, currentY, fw, 14, "e.g. minecraft:diamond_chestplate", scrollTop, scrollBottom);
            if (chestplateField != null) {
                chestplateField.setValue(savedChestplate);
                chestplateSuggestionManager = new SuggestionManager(
                        chestplateField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                chestplateField.setResponder(text -> {
                    savedChestplate = text;
                    chestplateSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18;

            drawInlineLabel(cx, currentY, "Leggings"); currentY += 10;
            leggingsField = addScrolledField(adder, font, cx, currentY, fw, 14, "e.g. minecraft:diamond_leggings", scrollTop, scrollBottom);
            if (leggingsField != null) {
                leggingsField.setValue(savedLeggings);
                leggingsSuggestionManager = new SuggestionManager(
                        leggingsField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                leggingsField.setResponder(text -> {
                    savedLeggings = text;
                    leggingsSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18;

            drawInlineLabel(cx, currentY, "Boots"); currentY += 10;
            bootsField = addScrolledField(adder, font, cx, currentY, fw, 14, "e.g. minecraft:diamond_boots", scrollTop, scrollBottom);
            if (bootsField != null) {
                bootsField.setValue(savedBoots);
                bootsSuggestionManager = new SuggestionManager(
                        bootsField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
                bootsField.setResponder(text -> {
                    savedBoots = text;
                    bootsSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18;
        }

        // ── Potion Effects toggle ─────────────────────────────────────────────
        addScrolledButton(adder,
                Component.literal((showPotionEffectsField ? "▼" : "▶") + " Potion Effects"
                        + (potionEffectEntries.isEmpty() ? "" : " (" + potionEffectEntries.size() + ")")),
                btn -> { showPotionEffectsField = !showPotionEffectsField; rebuildScreen.run(); },
                cx, currentY, fw, BTN_H, scrollTop, scrollBottom
        );
        currentY += BTN_H + 4;

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
                addScrolledButton(adder,
                        Component.literal("✕"),
                        btn -> { potionEffectEntries.remove(idx); rebuildScreen.run(); },
                        cx + fw - 20, currentY - 2, 20, 12, scrollTop, scrollBottom
                );
                currentY += 14;
            }

            drawInlineLabel(cx,               currentY, "Effect");
            drawInlineLabel(cx + c0 + 2, currentY, "Duration (s/inf)");
            drawInlineLabel(cx + c0 + c1 + 4, currentY, "Amplifier (1-256)");
            currentY += 10;

            pendingPotionIdField = addScrolledField(adder, font, cx, currentY, c0, 14, "e.g. minecraft:strength", scrollTop, scrollBottom);
            if (pendingPotionIdField != null) {
                pendingPotionIdField.setValue(pendingPotionId);
                pendingPotionSuggestionManager = new SuggestionManager(
                        pendingPotionIdField, BuiltInRegistries.MOB_EFFECT, MAX_VISIBLE_SUGGESTIONS, 14, false);
                pendingPotionIdField.setResponder(text -> { pendingPotionId = text; pendingPotionSuggestionManager.filterSuggestions(text); });
            }

            pendingPotionDurationField = addScrolledField(adder, font, cx + c0 + 2, currentY, c1, 14, "60 / inf", scrollTop, scrollBottom);
            if (pendingPotionDurationField != null) {
                pendingPotionDurationField.setValue(pendingPotionDuration);
                pendingPotionDurationField.setResponder(text -> pendingPotionDuration = text);
            }

            pendingPotionAmplifierField = addScrolledField(adder, font, cx + c0 + c1 + 4, currentY, c2, 14, "1 (1-256)", scrollTop, scrollBottom);
            if (pendingPotionAmplifierField != null) {
                pendingPotionAmplifierField.setValue(pendingPotionAmplifier);
                pendingPotionAmplifierField.setResponder(text -> pendingPotionAmplifier = text);
            }
            currentY += 18;

            addScrolledButton(adder,
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

                        int amplifier = 1;

                        try {
                            amplifier = Integer.parseInt(
                                    pendingPotionAmplifierField != null
                                            ? pendingPotionAmplifierField.getValue().trim()
                                            : "1"
                            );
                        } catch (Exception ignored) {
                        }

                        amplifier = Math.clamp(amplifier, 1, 256);

                        potionEffectEntries.add(new String[]{
                                id,
                                dur.isEmpty() ? "60" : dur,
                                String.valueOf(amplifier)
                        });

                        pendingPotionId = "";
                        pendingPotionDuration = "";
                        pendingPotionAmplifier = "";
                        rebuildScreen.run();
                    },
                    cx, currentY, 82, BTN_H, scrollTop, scrollBottom
            );
            currentY += BTN_H + 4;
        }

        // ── Enchantments toggle ───────────────────────────────────────────────
        addScrolledButton(adder,
                Component.literal((showEnchantmentsField ? "▼" : "▶") + " Enchantments"
                        + (enchantmentEntries.isEmpty() ? "" : " (" + enchantmentEntries.size() + ")")),
                btn -> { showEnchantmentsField = !showEnchantmentsField; rebuildScreen.run(); },
                cx, currentY, fw, BTN_H, scrollTop, scrollBottom
        );
        currentY += BTN_H + 4;

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
                addScrolledButton(adder,
                        Component.literal("✕"),
                        btn -> { enchantmentEntries.remove(idx); rebuildScreen.run(); },
                        cx + fw - 20, currentY - 2, 20, 12, scrollTop, scrollBottom
                );
                currentY += 14;
            }

            drawInlineLabel(cx, currentY, "Enchantment");

            int maxEnchantLevel = -1;

            try {
                String enchantId = pendingEnchantId.trim();

                if (!enchantId.isEmpty()) {
                    Identifier id = Identifier.parse(enchantId);

                    assert Minecraft.getInstance().level != null;
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
            currentY += 10;

            assert Minecraft.getInstance().level != null;
            RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
            var enchantmentRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

            pendingEnchantIdField = addScrolledField(adder, font, cx, currentY, e0, 14, "e.g. minecraft:sharpness", scrollTop, scrollBottom);
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
                    adder, font,
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
            addScrolledButton(adder,
                    Component.literal(currentTarget),
                    btn -> { pendingEnchantTargetIndex = (pendingEnchantTargetIndex + 1) % ENCHANT_TARGETS.length; rebuildScreen.run(); },
                    cx + e0 + e1 + 4, currentY, e2, 14, scrollTop, scrollBottom
            );
            currentY += 18;

            int finalMaxEnchantLevel = maxEnchantLevel;
            addScrolledButton(adder,
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
                        rebuildScreen.run();
                    },
                    cx, currentY, 88, BTN_H, scrollTop, scrollBottom
            );
            currentY += BTN_H + 4;
        }

        // ── Riding Mob toggle ─────────────────────────────────────────────────
        boolean hasRidingMob = !savedRidingMob.isEmpty();
        addScrolledButton(adder,
                Component.literal((showRidingMobField ? "▼" : "▶") + " Riding Mob" + (hasRidingMob ? " (1)" : "")),
                btn -> { showRidingMobField = !showRidingMobField; rebuildScreen.run(); },
                cx, currentY, fw, BTN_H, scrollTop, scrollBottom
        );
        currentY += BTN_H + 4;

        if (showRidingMobField) {
            drawInlineLabel(cx, currentY, "Riding Mob"); currentY += 10;
            ridingMobField = addScrolledField(adder, font, cx, currentY, fw, 14, "e.g. minecraft:spider", scrollTop, scrollBottom);
            if (ridingMobField != null) {
                ridingMobField.setValue(savedRidingMob);
                ridingMobSuggestionManager = new SuggestionManager(
                        ridingMobField, BuiltInRegistries.ENTITY_TYPE, MAX_VISIBLE_SUGGESTIONS, 14, false);
                ridingMobField.setResponder(text -> {
                    savedRidingMob = text;
                    ridingMobSuggestionManager.filterSuggestions(text);
                });
            }
            currentY += 18;
        }

        int contentHeight = computeContentHeight();
        int scrollableAreaHeight = scrollBottom - scrollTop;
        int maxScroll = Math.max(0, contentHeight - scrollableAreaHeight);
        addMobScroll = Math.clamp(addMobScroll, 0, maxScroll);

        adder.accept(Button.builder(Component.literal(editMode ? "✔ Save" : "✔ Add"),
                        btn -> submit(adder, font, detailX, detailW, guiTop, guiHeight,
                                onSubmit, onCancel, rebuildScreen))
                .bounds(cx, by, 50, BTN_H).build());

        adder.accept(Button.builder(Component.literal("✕ Cancel"),
                        btn -> onCancel.run())
                .bounds(cx + 54, by, 50, BTN_H).build());
    }

    // ── Scrolled-widget helpers ───────────────────────────────────────────────

    private EditBox addScrolledField(Consumer<AbstractWidget> adder, Font font,
                                     int x, int logicalY, int w, int h, String hint,
                                     int scrollTop, int scrollBottom) {
        int screenY = logicalY - addMobScroll;
        EditBox field = new EditBox(font, x, screenY, w, h, Component.literal(hint));
        field.setHint(Component.literal(hint));
        field.setBordered(true);
        field.setMaxLength(64);
        if (screenY + h > scrollTop && screenY < scrollBottom) {
            adder.accept(field);
        }
        return field;
    }

    private void addScrolledButton(Consumer<AbstractWidget> adder,
                                   Component label, Button.OnPress onPress,
                                   int x, int logicalY, int w, int h,
                                   int scrollTop, int scrollBottom) {
        int screenY = logicalY - addMobScroll;
        if (screenY + h > scrollTop && screenY < scrollBottom) {
            adder.accept(Button.builder(label, onPress)
                    .bounds(x, screenY, w, h).build());
        }
    }

    // ── Form submission ───────────────────────────────────────────────────────

    private void submit(Consumer<AbstractWidget> adder, Font font,
                        int detailX, int detailW, int guiTop, int guiHeight,
                        SubmitListener onSubmit, Runnable onCancel,
                        Runnable rebuildScreen) {
        if (mobTypeField == null || mobCountField == null) return;

        String mob = mobTypeField.getValue().trim();
        if (mob.isEmpty()) {
            validationError = "Please enter a mob type.";
            rebuildScreen.run();
            return;
        }

        boolean validMobId;
        try {
            validMobId = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(mob)).isPresent();
        } catch (Exception e) {
            validMobId = false;
        }
        if (!validMobId) {
            validationError = "Unknown mob type '" + mob + "'.";
            rebuildScreen.run();
            return;
        }

        int count;
        try {
            count = Integer.parseInt(mobCountField.getValue().trim());
        } catch (NumberFormatException e) {
            validationError = "Count must be a whole number.";
            rebuildScreen.run();
            return;
        }
        if (count < 1) {
            validationError = "Count must be at least 1.";
            rebuildScreen.run();
            return;
        }

        boolean isSlime = mob.equals("minecraft:slime") || mob.equals(MOD_ID + ":banana_slime");
        boolean isZombieLike = mob.equals("minecraft:zombie") || mob.equals("minecraft:zombie_villager") || mob.equals(MOD_ID + ":farmbie");

        if (isSlime && selectedSlimeVariant.isEmpty()) {
            validationError = "Please select a slime size (Small/Medium/Large).";
            rebuildScreen.run();
            return;
        }
        if (isZombieLike && selectedZombieVariant.isEmpty()) {
            validationError = "Please select Baby or Adult.";
            rebuildScreen.run();
            return;
        }

        validationError = "";

        try {
            String ridingMob    = savedRidingMob;
            String mainHandItem = savedMainHand;
            String offHandItem  = savedOffHand;
            Integer size = switch (savedMobVariant) {
                case "small" -> 1;
                case "medium" -> 2;
                case "large" -> 4;
                case "baby" -> -1;
                case "adult" -> 0;
                default -> null;
            };

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

            onSubmit.onSubmit(
                    mob, count, size,
                    ridingMob.isEmpty()    ? null : ridingMob,
                    mainHandItem.isEmpty() ? null : mainHandItem,
                    offHandItem.isEmpty()  ? null : offHandItem,
                    armorItems,
                    potionEffects.isEmpty() ? null : potionEffects,
                    enchantments.isEmpty()  ? null : enchantments
            );

            reset();
        } catch (Exception e) {
            validationError = "Unexpected error: " + e.getMessage();
            rebuildScreen.run();
        }
    }

    /** Resets all form state. Call this after a successful submit or on Cancel. */
    public void reset() {
        potionEffectEntries.clear();
        enchantmentEntries.clear();
        pendingEnchantTargetIndex = 0;
        addMobScroll = 0;
        editMode = false;
        showEquipmentFields = false;
        showRidingMobField = false;
        showPotionEffectsField = false;
        showEnchantmentsField = false;
        pendingPotionId = "";
        pendingPotionDuration = "";
        pendingPotionAmplifier = "";
        pendingEnchantId = "";
        pendingEnchantLevel = "";
        savedMobType = "";
        savedMobCount = "";
        savedMobVariant = "";
        selectedSlimeVariant = "";
        selectedZombieVariant = "";
        validationError = "";
        savedMainHand = "";
        savedOffHand = "";
        savedHelmet = "";
        savedChestplate = "";
        savedLeggings = "";
        savedBoots = "";
        savedRidingMob = "";
    }

    /** Populates all form state from an existing mob entry and opens relevant sections. Call before buildWidgets(). */
    public void prefill(ArenaDataPayload.MobEntry mob) {
        reset();
        editMode = true;

        savedMobType = mob.mobType();
        savedMobCount = String.valueOf(mob.count());

        boolean isSlime = mob.mobType().equals("minecraft:slime") || mob.mobType().equals(MOD_ID + ":banana_slime");
        boolean isZombieLike = mob.mobType().equals("minecraft:zombie") || mob.mobType().equals("minecraft:zombie_villager") || mob.mobType().equals(MOD_ID + ":farmbie");

        if (mob.size() != null) {
            if (isSlime) {
                selectedSlimeVariant = switch (mob.size()) {
                    case 1 -> "small";
                    case 2 -> "medium";
                    case 4 -> "large";
                    default -> "";
                };
                savedMobVariant = selectedSlimeVariant;
            } else if (isZombieLike) {
                selectedZombieVariant = mob.size() == -1 ? "baby" : "adult";
                savedMobVariant = selectedZombieVariant;
            }
        }

        savedRidingMob = mob.ridingMob() != null ? mob.ridingMob() : "";
        savedMainHand  = mob.mainHandItem() != null ? mob.mainHandItem() : "";
        savedOffHand   = mob.offHandItem() != null ? mob.offHandItem() : "";

        List<String> armor = mob.armorItems();
        savedHelmet     = (armor != null && !armor.isEmpty()) ? armor.get(0) : "";
        savedChestplate = (armor != null && armor.size() > 1) ? armor.get(1) : "";
        savedLeggings   = (armor != null && armor.size() > 2) ? armor.get(2) : "";
        savedBoots      = (armor != null && armor.size() > 3) ? armor.get(3) : "";

        if (mob.potionEffects() != null && !mob.potionEffects().isEmpty()) {
            for (String entry : mob.potionEffects().split(",")) {
                String[] parts = entry.trim().split(":");
                if (parts.length >= 3) {
                    String amp = parts[parts.length - 1];
                    String dur = parts[parts.length - 2];
                    String effectId = String.join(":", Arrays.copyOfRange(parts, 0, parts.length - 2));
                    potionEffectEntries.add(new String[]{effectId, dur, amp});
                }
            }
        }

        if (mob.enchantments() != null && !mob.enchantments().isEmpty()) {
            for (String entry : mob.enchantments().split(",")) {
                String[] parts = entry.trim().split(":");
                if (parts.length >= 3) {
                    String lvl = parts[parts.length - 1];
                    String target = parts[0];
                    String enchantId = String.join(":", Arrays.copyOfRange(parts, 1, parts.length - 1));
                    enchantmentEntries.add(new String[]{enchantId, lvl, target});
                }
            }
        }

        showEquipmentFields = !savedMainHand.isEmpty() || !savedOffHand.isEmpty()
                || !savedHelmet.isEmpty() || !savedChestplate.isEmpty()
                || !savedLeggings.isEmpty() || !savedBoots.isEmpty();
        showPotionEffectsField = !potionEffectEntries.isEmpty();
        showEnchantmentsField  = !enchantmentEntries.isEmpty();
        showRidingMobField     = !savedRidingMob.isEmpty();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    /**
     * Draws the header bar, the inline field labels (with scissor clipping), and
     * the validation error line. Call this from the parent's detail-panel render
     * pass when DetailView.ADD_MOB is active — BEFORE the parent's super.extractRenderState
     * call renders the actual widgets.
     */
    public void render(GuiGraphicsExtractor g, Font font, GuiTheme theme,
                       int dx, int dt, int dw, int guiTop, int guiHeight, int waveNumber) {
        g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
        g.text(font, (editMode ? "Edit Mob in Wave " : "Add Mob(s) to Wave ") + waveNumber, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

        int scissorTop = scrollTop(guiTop);
        int scissorBottom = scrollBottom(guiTop, guiHeight);
        g.enableScissor(dx, scissorTop, dx + dw, scissorBottom);

        for (int i = 0; i < inlineLabelPositions.size(); i++) {
            int[] pos = inlineLabelPositions.get(i);
            int labelY = pos[1] - addMobScroll;
            if (labelY >= scissorTop && labelY < scissorBottom) {
                g.text(font, inlineLabelTexts.get(i), pos[0], labelY, theme.subtext(), false);
            }
        }
        g.disableScissor();

        if (!validationError.isEmpty()) {
            g.text(font, validationError, dx + PANEL_PAD,
                    guiTop + guiHeight - BTN_H - PANEL_PAD - 12, 0xFFFF5555, false);
        }
    }

    /**
     * Draws the active suggestion dropdown, if any. Call this AFTER the parent's
     * super.extractRenderState (i.e. after widgets have rendered) so the popup sits
     * on top of everything else — same as the old renderDropdown() in MobArenaScreen.
     */
    public void renderDropdown(GuiGraphicsExtractor g, Font font) {
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

                    String suggestion = manager.getSuggestion(i);
                    String typed      = manager.getTypedText();
                    int matchStart    = suggestion.toLowerCase().indexOf(typed.toLowerCase());

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
    // Call these from the parent's keyPressed/mouseClicked/mouseScrolled overrides
    // while DetailView.ADD_MOB is active. Each returns true if it consumed the event.

    public boolean keyPressed(KeyEvent event) {
        for (SuggestionManager manager : buildSuggestionManagerList()) {
            if (manager != null && manager.getEditBox().isFocused() && manager.hasSuggestions()) {
                if (event.isDown())         { manager.selectNextSuggestion();     return true; }
                if (event.isUp())           { manager.selectPreviousSuggestion(); return true; }
                if (event.isConfirmation()) { manager.applySuggestion();          return true; }
            }
        }
        return false;
    }

    public boolean mouseClicked(MouseButtonEvent event) {
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
        return false;
    }

    /**
     * @param detailX current detail-panel X (parent's detailX())
     * @param detailW current detail-panel width (parent's detailW())
     * @param guiTop  current GUI top (parent's guiTop())
     * @param guiHeight current GUI height (parent's guiHeight())
     * @param onScrollChanged called if the scroll offset changed, so the parent can rebuildWidgets()
     */
    public boolean mouseScrolled(double mx, double my, double vertical,
                                 int detailX, int detailW, int guiTop, int guiHeight,
                                 Runnable onScrollChanged) {
        int scrollTop    = scrollTop(guiTop);
        int scrollBottom = scrollBottom(guiTop, guiHeight);
        if (mx >= detailX && mx <= detailX + detailW && my >= scrollTop && my <= scrollBottom) {
            int contentHeight = computeContentHeight();
            int scrollableAreaHeight = scrollBottom - scrollTop;
            int maxScroll = Math.max(0, contentHeight - scrollableAreaHeight);
            addMobScroll = (int) Math.max(0, Math.min(addMobScroll - vertical * 10, maxScroll));
            onScrollChanged.run();
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

        return false;
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

    private void drawInlineLabel(int x, int y, String text) {
        inlineLabelPositions.add(new int[]{x, y});
        inlineLabelTexts.add(text);
    }

    private static String formatIdentifierForDisplay(String identifier) {
        if (identifier == null || identifier.isEmpty()) return "";
        String path = identifier.contains(":") ? identifier.substring(identifier.indexOf(":") + 1) : identifier;
        return Arrays.stream(path.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }
}