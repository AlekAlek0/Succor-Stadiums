package net.alek.succorstadiums.screen.mobarenagui;

import net.alek.succorstadiums.network.ArenaDataPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static net.alek.succorstadiums.screen.mobarenagui.MobViewScreen.variantDisplayFor;

public class DelMobScreen {

    private static final int PANEL_PAD = 8;
    private static final int BTN_H = 16;

    public interface RemoveMob {
        void remove(String mobType, int count);
    }

    public void buildWidgets(Consumer<net.minecraft.client.gui.components.AbstractWidget> addRenderableWidget,
                             int detailX, int guiTop, int guiHeight,
                             ArenaDataPayload.WaveEntry wave,
                             RemoveMob onRemove, Runnable onBack) {

        int cx = detailX + PANEL_PAD;
        int currentY = guiTop + 36;
        int by = guiTop + guiHeight - BTN_H - PANEL_PAD;

        if (wave != null) {
            for (ArenaDataPayload.MobEntry mob : wave.mobs()) {
                int buttonY = currentY + 1;

                addRenderableWidget.accept(Button.builder(Component.literal("-1"),
                        btn -> onRemove.remove(mob.mobType(), 1)
                ).bounds(cx, buttonY, 24, BTN_H).build());

                addRenderableWidget.accept(Button.builder(Component.literal("-5"),
                        btn -> onRemove.remove(mob.mobType(), 5)
                ).bounds(cx + 28, buttonY, 24, BTN_H).build());

                addRenderableWidget.accept(Button.builder(Component.literal("-10"),
                        btn -> onRemove.remove(mob.mobType(), 10)
                ).bounds(cx + 56, buttonY, 28, BTN_H).build());

                addRenderableWidget.accept(Button.builder(Component.literal("-20"),
                        btn -> onRemove.remove(mob.mobType(), 20)
                ).bounds(cx + 88, buttonY, 28, BTN_H).build());

                addRenderableWidget.accept(Button.builder(Component.literal("✕ All"),
                        btn -> onRemove.remove(mob.mobType(), mob.count())
                ).bounds(cx + 120, buttonY, 36, BTN_H).build());

                currentY += MobViewScreen.computeMobEntryHeight(mob);
            }
        }

        addRenderableWidget.accept(Button.builder(Component.literal("< Back"), btn -> onBack.run())
                .bounds(cx, by, 50, BTN_H).build());
    }

    public void render(GuiGraphicsExtractor g, Font font, GuiTheme theme,
                       int dx, int dt, int dw, int guiTop, int selectedWave,
                       ArenaDataPayload.WaveEntry wave) {

        g.fill(dx, dt, dx + dw, dt + 16, 0xFFAA3333);
        g.text(font, "Remove Mobs from Wave " + selectedWave, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

        if (wave == null) return;

        if (wave.mobs().isEmpty()) {
            g.text(font, "No mobs in this wave.", dx + PANEL_PAD, guiTop + 36, theme.subtext(), false);
            return;
        }

        int currentY = guiTop + 36;
        for (int i = 0; i < wave.mobs().size(); i++) {
            ArenaDataPayload.MobEntry mob = wave.mobs().get(i);
            int mobEntryTotalHeight = MobViewScreen.computeMobEntryHeight(mob);

            if (i % 2 == 0) {
                g.fill(dx, currentY, dx + dw, currentY + mobEntryTotalHeight,
                        theme.getTheme() != Theme.LIGHT ? 0x15FFFFFF : 0x11000000);
            }

            String display = MobViewScreen.formatIdentifierForDisplay(mob.mobType());
            if (mob.size() != null) {
                String variant = variantDisplayFor(mob);
                if (!variant.isEmpty()) {
                    display += " (" + variant + ")";
                } else if (mob.size() != 0) {
                    display += " (Variant: " + mob.size() + ")";
                }
            }
            g.text(font, mob.count() + "x  " + display, dx + PANEL_PAD + 162, currentY + 4, theme.text(), false);

            MobViewScreen.renderMobDetailLines(g, font, theme, mob, dx + PANEL_PAD + 10, currentY + 18);

            currentY += mobEntryTotalHeight;
        }
    }
}