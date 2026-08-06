package net.alek.succorstadiums.screen.mobarenagui;

import net.alek.succorstadiums.network.arena.ArenaDataPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static net.alek.succorstadiums.screen.mobarenagui.MobViewScreen.variantDisplayFor;

public class WaveFormScreen {

    private static final int PANEL_PAD = 8;
    private static final int BTN_H = 16;

    private EditBox nameField;
    private EditBox delayField;
    private int mobScroll = 0;

    public interface Submit {
        /** delaySeconds is null if the field was left blank, meaning "use the arena's default delay" */
        void submit(String name, Integer delaySeconds);
    }

    public interface RemoveMob {
        void remove(ArenaDataPayload.MobEntry mob, int count);
    }

    public interface EditMob {
        void edit(ArenaDataPayload.MobEntry mob);
    }

    /** Call when switching to a different wave, so scroll position doesn't carry over. */
    public void resetScroll() {
        mobScroll = 0;
    }

    /**
     * @param wave       wave being edited (name/delay/mobs pre-filled from this)
     * @param arenaDefaultDelay pre-fills the delay field when the wave has no override
     * @param onSubmit   called with the trimmed name and parsed delay when Save is pressed
     * @param onCancel   called when Cancel is pressed
     * @param onRemoveMob called with (mob, count) when a count-control button is pressed
     * @param onEditMob  called with the mob entry when its ✎ button is pressed (opens AddMobScreen in edit mode)
     */
    public void buildWidgets(Consumer<AbstractWidget> addRenderableWidget,
                             Font font,
                             int detailX, int detailW, int guiTop, int guiHeight,
                             ArenaDataPayload.WaveEntry wave,
                             int arenaDefaultDelay,
                             Submit onSubmit, Runnable onCancel,
                             RemoveMob onRemoveMob, EditMob onEditMob) {

        int cx = detailX + PANEL_PAD;
        int cy = guiTop + 20;
        int fw = detailW - PANEL_PAD * 2;
        int by = guiTop + guiHeight - BTN_H - PANEL_PAD;

        nameField = makeField(addRenderableWidget, font, cx, cy + 10, fw / 2 - 2, "Wave name");
        if (wave != null) {
            nameField.setValue(wave.name() != null && !wave.name().isEmpty()
                    ? wave.name() : "Wave " + wave.waveNumber());
        }

        delayField = makeField(addRenderableWidget, font, cx + fw / 2 + 2, cy + 10, fw / 2 - 2, "Delay (s)");
        if (wave != null && wave.delaySeconds() != null) {
            delayField.setValue(String.valueOf(wave.delaySeconds()));
        } else {
            delayField.setValue(String.valueOf(arenaDefaultDelay));
        }

        // ── Mob list ─────────────────────────────────────────────────────
        int mobAreaTop = cy + 52;
        int mobAreaBottom = by - 4;

        if (wave != null) {
            int currentY = mobAreaTop;
            for (int i = mobScroll; i < wave.mobs().size(); i++) {
                ArenaDataPayload.MobEntry mob = wave.mobs().get(i);
                int rowHeight = MobViewScreen.computeMobEntryHeight(mob);

                if (currentY + rowHeight > mobAreaBottom) break;

                int totalCtrlWidth = 14 + 2 + 24 + 2 + 24 + 2 + 28 + 2 + 28 + 2 + 36;
                int ctrlStart = detailX + detailW - PANEL_PAD - totalCtrlWidth;

                addRenderableWidget.accept(Button.builder(Component.literal("✎"),
                        btn -> onEditMob.edit(mob)
                ).bounds(ctrlStart, currentY + 1, 14, BTN_H).build());

                int countCtrlStart = ctrlStart + 16;

                addRenderableWidget.accept(Button.builder(Component.literal("-1"),
                        btn -> onRemoveMob.remove(mob, 1)
                ).bounds(countCtrlStart, currentY + 1, 24, BTN_H).build());

                addRenderableWidget.accept(Button.builder(Component.literal("-5"),
                        btn -> onRemoveMob.remove(mob, 5)
                ).bounds(countCtrlStart + 26, currentY + 1, 24, BTN_H).build());

                addRenderableWidget.accept(Button.builder(Component.literal("-10"),
                        btn -> onRemoveMob.remove(mob, 10)
                ).bounds(countCtrlStart + 52, currentY + 1, 28, BTN_H).build());

                addRenderableWidget.accept(Button.builder(Component.literal("-20"),
                        btn -> onRemoveMob.remove(mob, 20)
                ).bounds(countCtrlStart + 82, currentY + 1, 28, BTN_H).build());

                addRenderableWidget.accept(Button.builder(Component.literal("✕ All"),
                        btn -> onRemoveMob.remove(mob, mob.count())
                ).bounds(countCtrlStart + 112, currentY + 1, 36, BTN_H).build());

                currentY += rowHeight;
            }
        }

        addRenderableWidget.accept(Button.builder(Component.literal("✔ Save"), btn -> trySubmit(onSubmit))
                .bounds(cx, by, 50, BTN_H).build());
        addRenderableWidget.accept(Button.builder(Component.literal("✕ Cancel"), btn -> onCancel.run())
                .bounds(cx + 54, by, 50, BTN_H).build());
    }

    private void trySubmit(Submit onSubmit) {
        if (nameField == null || delayField == null) return;
        String name = nameField.getValue().trim();

        Integer delay = null;
        String delayText = delayField.getValue().trim();
        if (!delayText.isEmpty()) {
            try {
                delay = Integer.parseInt(delayText);
            } catch (NumberFormatException ignored) {
                return; // bad input, don't submit
            }
        }

        onSubmit.submit(name, delay);
    }

    // ── Scroll handling ───────────────────────────────────────────────────

    public boolean mouseScrolled(double mx, double my, double vertical,
                                 int detailX, int detailW, int guiTop, int guiHeight,
                                 ArenaDataPayload.WaveEntry wave, Runnable rebuild) {
        int cy = guiTop + 20;
        int mobAreaTop = cy + 52;
        int by = guiTop + guiHeight - BTN_H - PANEL_PAD;
        int mobAreaBottom = by - 4;

        if (mx < detailX || mx > detailX + detailW || my < mobAreaTop || my > mobAreaBottom) {
            return false;
        }
        if (wave == null || wave.mobs().isEmpty()) return false;

        int maxScroll = Math.max(0, wave.mobs().size() - 1);
        mobScroll = (int) Math.max(0, Math.min(mobScroll - vertical, maxScroll));
        rebuild.run();
        return true;
    }

    // ── Rendering ──────────────────────────────────────────────────────────

    public void render(GuiGraphicsExtractor g, Font font, GuiTheme theme,
                       int dx, int dt, int dw, int guiTop, int guiHeight,
                       String headerTitle, ArenaDataPayload.WaveEntry wave) {
        g.fill(dx, dt, dx + 849, dt + 16, 0xFF5C7ABA);
        g.text(font, headerTitle, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);
        g.text(font, "Name / Delay (s)", dx + PANEL_PAD, dt + 20, theme.subtext(), false);

        int cy = dt + 20;
        g.text(font, "Mobs", dx + PANEL_PAD, cy + 42, theme.subtext(), false);

        int mobAreaTop = cy + 52;
        int by = guiTop + guiHeight - BTN_H - PANEL_PAD;
        int mobAreaBottom = by - 4;

        if (wave == null) return;

        if (wave.mobs().isEmpty()) {
            g.text(font, "No mobs in this wave.", dx + PANEL_PAD, mobAreaTop + 4, theme.subtext(), false);
            return;
        }

        int currentY = mobAreaTop;
        int rowIndex = 0;
        for (int i = mobScroll; i < wave.mobs().size(); i++) {
            ArenaDataPayload.MobEntry mob = wave.mobs().get(i);
            int rowHeight = MobViewScreen.computeMobEntryHeight(mob);

            if (currentY + rowHeight > mobAreaBottom) break;

            if (rowIndex % 2 == 0) {
                g.fill(dx, currentY, dx + dw - 3, currentY + rowHeight,
                        theme.getTheme() != Theme.LIGHT ? 0x15FFFFFF : 0x11000000);
            }

            String display = MobViewScreen.formatIdentifierForDisplay(mob.mobType());
            if (mob.size() != null) {
                String variant = variantDisplayFor(mob);
                display += !variant.isEmpty() ? " (" + variant + ")" : " (Variant: " + mob.size() + ")";
            }
            g.text(font, mob.count() + "x  " + display, dx + PANEL_PAD, currentY + 4, theme.text(), false);

            MobViewScreen.renderMobDetailLines(g, font, theme, mob, dx + PANEL_PAD + 10, currentY + 18);

            currentY += rowHeight;
            rowIndex++;
        }
    }

    private static EditBox makeField(Consumer<AbstractWidget> addRenderableWidget,
                                     Font font, int x, int y, int w, String hint) {
        EditBox field = new EditBox(font, x, y, w, 14, Component.literal(hint));
        field.setHint(Component.literal(hint));
        field.setBordered(true);
        addRenderableWidget.accept(field);
        return field;
    }
}