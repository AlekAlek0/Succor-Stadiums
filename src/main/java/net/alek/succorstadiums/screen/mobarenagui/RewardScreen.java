package net.alek.succorstadiums.screen.mobarenagui;

import net.alek.succorstadiums.network.arena.ArenaDataPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Edits a list of reward items (item id + count). Used for both an arena's
 * completion reward and a single wave's completion reward — the caller
 * supplies the initial list and gets the edited list back on Save.
 */
public class RewardScreen {

    private static final int PANEL_PAD = 8;
    private static final int BTN_H = 16;
    private static final int ROW_H = 16;
    private static final int MAX_VISIBLE_SUGGESTIONS = 8;

    public interface Submit {
        void submit(List<ArenaDataPayload.RewardEntry> rewards);
    }

    private final List<String[]> entries = new ArrayList<>(); // {itemId, count}

    private EditBox pendingItemField, pendingCountField;
    private String pendingItemId = "";
    private String pendingCount = "1";
    private SuggestionManager pendingItemSuggestionManager;

    private String validationError = "";

    /** Call before buildWidgets() to load the current reward list into the form. */
    public void prefill(List<ArenaDataPayload.RewardEntry> current) {
        entries.clear();
        if (current != null) {
            for (ArenaDataPayload.RewardEntry r : current) {
                entries.add(new String[]{r.itemId(), String.valueOf(r.count())});
            }
        }
        pendingItemId = "";
        pendingCount = "1";
        validationError = "";
    }

    public void buildWidgets(Consumer<AbstractWidget> adder, Font font,
                             int detailX, int detailW, int guiTop, int guiHeight,
                             Submit onSubmit, Runnable onCancel, Runnable rebuildScreen) {

        int cx = detailX + PANEL_PAD;
        int fw = detailW - PANEL_PAD * 2;
        int currentY = guiTop + 30; // was guiTop + 24 — leave room below the header bar
        int by = guiTop + guiHeight - BTN_H - PANEL_PAD;

        for (int i = 0; i < entries.size(); i++) {
            final int idx = i;
            adder.accept(Button.builder(Component.literal("✕"),
                    btn -> { entries.remove(idx); rebuildScreen.run(); }
            ).bounds(cx + fw - 20, currentY, 20, 14).build());
            currentY += ROW_H;
        }

        currentY += 14; // gap before the "Item / Count" column labels + input row

        int itemW = fw - 60 - 60 - 8;

        pendingItemField = new EditBox(font, cx, currentY, itemW, 14, Component.literal("e.g. minecraft:diamond"));
        pendingItemField.setHint(Component.literal("e.g. minecraft:diamond"));
        pendingItemField.setBordered(true);
        pendingItemField.setMaxLength(64);
        pendingItemField.setValue(pendingItemId);
        adder.accept(pendingItemField);

        pendingItemSuggestionManager = new SuggestionManager(
                pendingItemField, BuiltInRegistries.ITEM, MAX_VISIBLE_SUGGESTIONS, 14, false);
        pendingItemField.setResponder(text -> {
            pendingItemId = text;
            pendingItemSuggestionManager.filterSuggestions(text);
        });

        pendingCountField = new EditBox(font, cx + itemW + 4, currentY, 60, 14, Component.literal("Count"));
        pendingCountField.setHint(Component.literal("Count"));
        pendingCountField.setBordered(true);
        pendingCountField.setValue(pendingCount);
        pendingCountField.setResponder(text -> pendingCount = text);
        adder.accept(pendingCountField);

        adder.accept(Button.builder(Component.literal("+ Add"),
                btn -> {
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
                    try {
                        count = Integer.parseInt(pendingCountField.getValue().trim());
                    } catch (Exception ignored) {}
                    count = Math.max(1, count);

                    entries.add(new String[]{id, String.valueOf(count)});
                    pendingItemId = "";
                    pendingCount = "1";
                    validationError = "";
                    rebuildScreen.run();
                }
        ).bounds(cx + itemW + 4 + 64, currentY, 52, 14).build());

        adder.accept(Button.builder(Component.literal("✔ Save"),
                btn -> {
                    List<ArenaDataPayload.RewardEntry> result = entries.stream()
                            .map(e -> new ArenaDataPayload.RewardEntry(e[0], Integer.parseInt(e[1])))
                            .collect(Collectors.toList());
                    onSubmit.submit(result);
                }
        ).bounds(cx, by, 50, BTN_H).build());

        adder.accept(Button.builder(Component.literal("✕ Cancel"),
                btn -> onCancel.run()
        ).bounds(cx + 54, by, 50, BTN_H).build());
    }

    public void render(GuiGraphicsExtractor g, Font font, GuiTheme theme,
                       int dx, int dt, int dw, int guiTop, int guiHeight, String headerTitle) {
        g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
        g.text(font, headerTitle, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

        int currentY = guiTop + 30;

        if (entries.isEmpty()) {
            g.text(font, "No rewards set.", dx + PANEL_PAD, currentY + 2, theme.subtext(), false);
        } else {
            for (String[] e : entries) {
                String display = e[1] + "x  " + formatIdentifierForDisplay(e[0]);
                g.text(font, display, dx + PANEL_PAD, currentY + 2, theme.text(), false);
                currentY += ROW_H;
            }
        }

        currentY += 14;

        int fw = dw - PANEL_PAD * 2;
        int itemW = fw - 60 - 60 - 8;
        g.text(font, "Item", dx + PANEL_PAD, currentY - 10, theme.subtext(), false);
        g.text(font, "Count", dx + PANEL_PAD + itemW + 4, currentY - 10, theme.subtext(), false);

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