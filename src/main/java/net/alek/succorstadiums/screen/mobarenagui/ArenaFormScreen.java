package net.alek.succorstadiums.screen.mobarenagui;

import net.alek.succorstadiums.network.ArenaDataPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ArenaFormScreen {

    private static final int PANEL_PAD = 8;
    private static final int BTN_H = 16;

    private EditBox nameField, xField, yField, zField, radiusField, delayField;

    public interface Submit {
        void submit(String name, double x, double y, double z, int radius, int delay);
    }

    /**
     * @param existing   arena to pre-fill from, or null for a fresh "New Arena" form
     * @param onSubmit   called with parsed field values when Create/Save is pressed
     * @param onCancel   called when Cancel is pressed
     */
    public void buildWidgets(Consumer<net.minecraft.client.gui.components.AbstractWidget> addRenderableWidget,
                             Font font,
                             int detailX, int detailW, int guiTop, int guiHeight,
                             ArenaDataPayload.ArenaEntry existing,
                             Submit onSubmit, Runnable onCancel) {

        int cx = detailX + PANEL_PAD;
        int cy = guiTop + 20;
        int fw = detailW - PANEL_PAD * 2;
        int by = guiTop + guiHeight - BTN_H - PANEL_PAD;
        int posW = (fw - 54) / 3 - 2;

        nameField = makeField(addRenderableWidget, font, cx, cy + 10, fw, "Arena name");
        xField = makeField(addRenderableWidget, font, cx, cy + 50, posW, "X");
        yField = makeField(addRenderableWidget, font, cx + posW + 1, cy + 50, posW, "Y");
        zField = makeField(addRenderableWidget, font, cx + (posW + 1) * 2, cy + 50, posW, "Z");
        radiusField = makeField(addRenderableWidget, font, cx, cy + 90, fw / 2 - 2, "Radius");
        delayField = makeField(addRenderableWidget, font, cx + fw / 2 + 2, cy + 90, fw / 2 - 2, "Delay (s)");

        addRenderableWidget.accept(Button.builder(Component.literal("My Pos"),
                btn -> {
                    var player = Minecraft.getInstance().player;
                    if (player != null) {
                        xField.setValue(String.valueOf((int) player.getX()));
                        yField.setValue(String.valueOf((int) player.getY()));
                        zField.setValue(String.valueOf((int) player.getZ()));
                    }
                }
        ).bounds(detailX + detailW - PANEL_PAD - 50, cy + 50, 50, BTN_H - 2).build());

        if (existing != null) {
            nameField.setValue(existing.name());
            xField.setValue(String.valueOf((int) existing.x()));
            yField.setValue(String.valueOf((int) existing.y()));
            zField.setValue(String.valueOf((int) existing.z()));
            radiusField.setValue(String.valueOf(existing.radius()));
            delayField.setValue(String.valueOf(existing.delaySeconds()));
        }

        String confirmLabel = existing != null ? "✔ Save" : "✔ Create";
        int confirmWidth = existing != null ? 50 : 60;
        addRenderableWidget.accept(Button.builder(Component.literal(confirmLabel), btn -> trySubmit(onSubmit))
                .bounds(cx, by, confirmWidth, BTN_H).build());
        addRenderableWidget.accept(Button.builder(Component.literal("✕ Cancel"), btn -> onCancel.run())
                .bounds(cx + confirmWidth + 4, by, 50, BTN_H).build());
    }

    private void trySubmit(Submit onSubmit) {
        try {
            if (nameField == null || xField == null || yField == null ||
                    zField == null || radiusField == null || delayField == null) return;
            String name = nameField.getValue().trim();
            if (name.isEmpty()) return;
            double x = Double.parseDouble(xField.getValue().trim());
            double y = Double.parseDouble(yField.getValue().trim());
            double z = Double.parseDouble(zField.getValue().trim());
            int radius = Integer.parseInt(radiusField.getValue().trim());
            int delay = Integer.parseInt(delayField.getValue().trim());
            onSubmit.submit(name, x, y, z, radius, delay);
        } catch (NumberFormatException ignored) {}
    }

    public void render(GuiGraphicsExtractor g, Font font, GuiTheme theme,
                       int dx, int dt, String headerTitle) {
        g.fill(dx, dt, dx + 1000,dt + 16, 0xFF5C7ABA);
        g.text(font, headerTitle, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);
        g.text(font, "Name", dx + PANEL_PAD, dt + 20, theme.subtext(), false);
        g.text(font, "Position", dx + PANEL_PAD, dt + 60,  theme.subtext(), false);
        g.text(font, "Radius / Delay (s)", dx + PANEL_PAD, dt + 100, theme.subtext(), false);
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