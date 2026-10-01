package net.alek.succorstadiums.screen.mobarenagui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.Font;

import java.util.stream.Collectors;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.alek.succorstadiums.network.arena.AdvancementIdCache;
import net.alek.succorstadiums.network.arena.ArenaDataPayload;
import net.alek.succorstadiums.config.Theme;

// RewardScreen class
public class RewardScreen {

    private static final int PANEL_PAD = 8;
    private static final int BTN_H = 16;
    private static final int ROW_H = 16;
    private static final int MAX_VISIBLE_SUGGESTIONS = 8;

    public interface Submit {
        void submit(List<ArenaDataPayload.RewardEntry> rewards);
    }

    // Each entry: {itemId (empty if xp/advancement), count, "true"/"false" for xp, "true"/"false" for levels, advancementId (empty if not an advancement)}
    private final List<String[]> entries = new ArrayList<>();

    private EditBox pendingItemField, pendingCountField;
    private String pendingItemId = "";
    private String pendingAdvancementId = "";
    private String pendingCount = "1";
    private boolean pendingIsXp = false;
    private boolean pendingIsAdvancement = false;
    private boolean pendingIsLevels = false;
    private SuggestionManager pendingItemSuggestionManager;

    private String validationError = "";

    public void prefill(List<ArenaDataPayload.RewardEntry> current) {
        entries.clear();
        if (current != null) {
            for (ArenaDataPayload.RewardEntry r : current) {
                entries.add(new String[]{
                        r.itemId() == null ? "" : r.itemId(),
                        String.valueOf(r.count()),
                        String.valueOf(r.xp()),
                        String.valueOf(r.levels()),
                        r.advancementId() == null ? "" : r.advancementId()
                });
            }
        }
        pendingItemId = "";
        pendingAdvancementId = "";
        pendingCount = "1";
        pendingIsXp = false;
        pendingIsAdvancement = false;
        pendingIsLevels = false;
        validationError = "";
    }

    public void buildWidgets(Consumer<AbstractWidget> adder, Font font,
                             int detailX, int detailW, int guiTop, int guiHeight,
                             Submit onSubmit, Runnable onCancel, Runnable rebuildScreen) {

        int cx = detailX + PANEL_PAD;
        int fw = detailW - PANEL_PAD * 2;
        int currentY = guiTop + 30;
        int by = guiTop + guiHeight - BTN_H - PANEL_PAD;

        for (int i = 0; i < entries.size(); i++) {
            final int idx = i;
            adder.accept(Button.builder(Component.literal("✕"),
                    btn -> { entries.remove(idx); rebuildScreen.run(); }
            ).bounds(cx + fw - 20, currentY, 20, 14).build());
            currentY += ROW_H;
        }

        currentY += 14;

        int toggleW = 40;
        int unitToggleW = 48; // only shown/usable when pendingIsXp is true
        int countW = 60;
        int addW = 52;
        int itemW = fw - toggleW - 4 - unitToggleW - 4 - countW - 4 - addW - 4;

        addRowToggleButton(adder, cx, currentY, toggleW, rebuildScreen);

        int unitToggleX = cx + toggleW + 4;
        adder.accept(Button.builder(
                Component.literal(pendingIsXp ? (pendingIsLevels ? "Levels" : "Points") : "—"),
                btn -> { pendingIsLevels = !pendingIsLevels; rebuildScreen.run(); }
        ).bounds(unitToggleX, currentY, unitToggleW, 14).build());

        int itemFieldX = unitToggleX + unitToggleW + 4;
        String itemHint = pendingIsXp ? "N/A (XP reward)"
                : pendingIsAdvancement ? "e.g. succorstadiums:vincible"
                  : "e.g. minecraft:diamond";

        pendingItemField = new EditBox(font, itemFieldX, currentY, itemW, 14, Component.literal(itemHint));
        pendingItemField.setBordered(true);
        pendingItemField.setMaxLength(128);
        pendingItemField.setEditable(!pendingIsXp);
        pendingItemField.setValue(pendingIsXp ? "" : (pendingIsAdvancement ? pendingAdvancementId : pendingItemId));
        pendingItemField.setHint(Component.literal(itemHint));
        adder.accept(pendingItemField);

        // Item mode suggests from the item registry, Adv mode from the advancement IDs the server sent, XP mode has none.
        if (pendingIsAdvancement) {
            pendingItemSuggestionManager = new SuggestionManager(
                    pendingItemField, AdvancementIdCache.get(), MAX_VISIBLE_SUGGESTIONS, 14, false);
        } else if (!pendingIsXp) {
            pendingItemSuggestionManager = new SuggestionManager(
                    pendingItemField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
        } else {
            pendingItemSuggestionManager = null;
        }
        pendingItemField.setResponder(text -> {
            if (pendingIsAdvancement) {
                pendingAdvancementId = text;
            } else {
                pendingItemId = text;
            }
            if (pendingItemSuggestionManager != null) pendingItemSuggestionManager.filterSuggestions(text);
        });

        String countHint = pendingIsAdvancement ? "N/A"
                : pendingIsXp ? (pendingIsLevels ? "Levels" : "XP points")
                  : "Count";
        pendingCountField = new EditBox(font, itemFieldX + itemW + 4, currentY, countW, 14,
                Component.literal(countHint));
        pendingCountField.setBordered(true);
        pendingCountField.setHint(Component.literal(countHint));
        pendingCountField.setEditable(!pendingIsAdvancement);
        pendingCountField.setValue(pendingIsAdvancement ? "" : pendingCount);
        pendingCountField.setResponder(text -> { if (!pendingIsAdvancement) pendingCount = text; });
        adder.accept(pendingCountField);

        int addX = itemFieldX + itemW + 4 + countW + 4;
        adder.accept(Button.builder(Component.literal("+ Add"),
                btn -> {
                    if (pendingIsAdvancement) {
                        String advId = pendingItemField.getValue().trim();
                        if (advId.isEmpty()) return;
                        boolean validFormat;
                        try {
                            Identifier.parse(advId);
                            validFormat = true;
                        } catch (Exception e) {
                            validFormat = false;
                        }
                        if (!validFormat) {
                            validationError = "Invalid advancement ID '" + advId + "'.";
                            rebuildScreen.run();
                            return;
                        }
                        entries.add(new String[]{"", "1", "false", "false", advId});
                        pendingAdvancementId = "";
                        validationError = "";
                        rebuildScreen.run();
                        return;
                    }

                    if (pendingIsXp) {
                        int amount = 1;
                        try { amount = Integer.parseInt(pendingCountField.getValue().trim()); } catch (Exception ignored) {}
                        amount = Math.max(1, amount);
                        entries.add(new String[]{"", String.valueOf(amount), "true", String.valueOf(pendingIsLevels), ""});
                        pendingCount = "1";
                        validationError = "";
                        rebuildScreen.run();
                        return;
                    }

                    String id = pendingItemField.getValue().trim();
                    if (id.isEmpty()) return;
                    boolean valid;
                    try {
                        valid = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).isPresent();
                    } catch (Exception e) {
                        valid = false;
                    }
                    if (!valid) {
                        validationError = "Unknown item '" + id + "'.";
                        rebuildScreen.run();
                        return;
                    }
                    int count = 1;
                    try { count = Integer.parseInt(pendingCountField.getValue().trim()); } catch (Exception ignored) {}
                    count = Math.max(1, count);

                    entries.add(new String[]{id, String.valueOf(count), "false", "false", ""});
                    pendingItemId = "";
                    pendingCount = "1";
                    validationError = "";
                    rebuildScreen.run();
                }
        ).bounds(addX, currentY, addW, 14).build());

        adder.accept(Button.builder(Component.literal("✔ Save"),
                btn -> {
                    List<ArenaDataPayload.RewardEntry> result = entries.stream()
                            .map(e -> new ArenaDataPayload.RewardEntry(
                                    e[0].isEmpty() ? null : e[0],
                                    Integer.parseInt(e[1]),
                                    Boolean.parseBoolean(e[2]),
                                    Boolean.parseBoolean(e[3]),
                                    e[4].isEmpty() ? null : e[4]))
                            .collect(Collectors.toList());
                    onSubmit.submit(result);
                }
        ).bounds(cx, by, 50, BTN_H).build());

        adder.accept(Button.builder(Component.literal("✕ Cancel"),
                btn -> onCancel.run()
        ).bounds(cx + 54, by, 50, BTN_H).build());
    }

    // Cycles the pending reward type: Item -> XP -> Adv -> Item
    private void addRowToggleButton(Consumer<AbstractWidget> adder, int x, int y, int w, Runnable rebuildScreen) {
        String label = pendingIsAdvancement ? "Adv" : (pendingIsXp ? "XP" : "Item");
        adder.accept(Button.builder(Component.literal(label),
                btn -> {
                    if (pendingIsAdvancement) {
                        pendingIsAdvancement = false;
                    } else if (pendingIsXp) {
                        pendingIsXp = false;
                        pendingIsAdvancement = true;
                    } else {
                        pendingIsXp = true;
                    }
                    validationError = "";
                    rebuildScreen.run();
                }
        ).bounds(x, y, w, 14).build());
    }

    public void render(GuiGraphicsExtractor g, Font font, Theme theme,
                       int dx, int dt, int dw, int guiTop, int guiHeight, String headerTitle) {
        g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
        g.text(font, headerTitle, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

        int currentY = guiTop + 30;

        if (entries.isEmpty()) {
            g.text(font, "No rewards set.", dx + PANEL_PAD, currentY + 2, theme.subtext.getRGB(), false);
            currentY += ROW_H;
        } else {
            for (String[] e : entries) {
                boolean isXp = Boolean.parseBoolean(e[2]);
                boolean isLevels = Boolean.parseBoolean(e[3]);
                boolean isAdvancement = !e[4].isEmpty();
                String display;
                if (isAdvancement) {
                    display = "Advancement: " + e[4];
                } else if (isXp) {
                    display = e[1] + (isLevels ? " Level" + (e[1].equals("1") ? "" : "s") : " XP");
                } else {
                    display = e[1] + "x  " + formatIdentifierForDisplay(e[0]);
                }
                g.text(font, display, dx + PANEL_PAD, currentY + 2, theme.text.getRGB(), false);
                currentY += ROW_H;
            }
        }

        currentY += 14;

        int fw = dw - PANEL_PAD * 2;
        int toggleW = 40, unitToggleW = 48, countW = 60, addW = 52;
        int itemW = fw - toggleW - 4 - unitToggleW - 4 - countW - 4 - addW - 4;
        int itemFieldX = dx + PANEL_PAD + toggleW + 4 + unitToggleW + 4;
        g.text(font, pendingIsAdvancement ? "Advancement ID" : "Item / XP", itemFieldX, currentY - 10, theme.subtext.getRGB(), false);
        g.text(font, "Amount", itemFieldX + itemW + 4, currentY - 10, theme.subtext.getRGB(), false);

        if (!validationError.isEmpty()) {
            g.text(font, validationError, dx + PANEL_PAD,
                    guiTop + guiHeight - BTN_H - PANEL_PAD - 12, 0xFFFF5555, false);
        }
    }

    public void renderDropdown(GuiGraphicsExtractor g, Font font) {
        if (pendingItemSuggestionManager != null
                && pendingItemSuggestionManager.getEditBox().isFocused()
                && pendingItemSuggestionManager.hasSuggestions()) {
            int sx = pendingItemSuggestionManager.getDropdownX();
            int sy = pendingItemSuggestionManager.getDropdownY();
            int sw = pendingItemSuggestionManager.getDropdownWidth();
            int visible = pendingItemSuggestionManager.getVisibleSuggestionsCount();

            g.fill(sx, sy, sx + sw, sy + visible * 12, 0xFF333333);
            for (int i = 0; i < visible; i++) {
                int idx = i + pendingItemSuggestionManager.getSuggestionScrollOffset();
                if (idx >= pendingItemSuggestionManager.getFilteredSuggestions().size()) break;
                int rowY = sy + i * 12;
                if (idx == pendingItemSuggestionManager.getSelectedSuggestionIndex()) {
                    g.fill(sx, rowY, sx + sw, rowY + 12, 0xFF444488);
                }
                g.text(font, pendingItemSuggestionManager.getSuggestion(i), sx + 2, rowY + 2,
                        idx == pendingItemSuggestionManager.getSelectedSuggestionIndex() ? 0xFFFFFFFF : 0xFFAAAAAA, false);
            }
        }
    }

    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (pendingItemSuggestionManager != null
                && pendingItemSuggestionManager.getEditBox().isFocused()
                && pendingItemSuggestionManager.hasSuggestions()) {
            if (event.isDown())         { pendingItemSuggestionManager.selectNextSuggestion();     return true; }
            if (event.isUp())           { pendingItemSuggestionManager.selectPreviousSuggestion(); return true; }
            if (event.isConfirmation()) { pendingItemSuggestionManager.applySuggestion();          return true; }
        }
        return false;
    }

    private static String formatIdentifierForDisplay(String identifier) {
        if (identifier == null || identifier.isEmpty()) return "";
        String path = identifier.contains(":") ? identifier.substring(identifier.indexOf(":") + 1) : identifier;
        return Arrays.stream(path.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }
}