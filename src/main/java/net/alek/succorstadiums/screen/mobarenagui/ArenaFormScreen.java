package net.alek.succorstadiums.screen.mobarenagui;

import net.alek.succorstadiums.network.arena.ArenaDataPayload;
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

    private EditBox groupField, nameField, xField, yField, zField, radiusField, delayField;

    // Backing fields: survive rebuildWidgets() (e.g. navigating to the Rewards
    // screen and back) so in-progress input isn't lost, same pattern AddMobScreen
    // uses for its own form state.
    private String savedName = "";
    private String savedGroup = "";
    private String savedX = "";
    private String savedY = "";
    private String savedZ = "";
    private String savedRadius = "";
    private String savedDelay = "";

    public interface Submit {
        void submit(String name, double x, double y, double z, int radius, int delay, String group);
    }

    /**
     * @param existing   arena to pre-fill from, or null for a fresh "New Arena" form
     * @param onSubmit   called with parsed field values when Create/Save is pressed
     * @param onCancel   called when Cancel is pressed
     */
    public void buildWidgets(Consumer<AbstractWidget> addRenderableWidget,
                             Font font,
                             int detailX, int detailW, int guiTop, int guiHeight,
                             ArenaDataPayload.ArenaEntry existing, int rewardCount, int participationRewardCount,
                             Submit onSubmit, Runnable onCancel,
                             Runnable onEditRewards, Runnable onEditParticipationRewards) {

        int cx = detailX + PANEL_PAD;
        int cy = guiTop + 20;
        int fw = detailW - PANEL_PAD * 2;
        int by = guiTop + guiHeight - BTN_H - PANEL_PAD;
        int posW = (fw - 54) / 3 - 2;
        int halfW = fw / 2 - 2;

        // Only pull fresh values from `existing` the first time we see it (i.e. we
        // haven't got anything saved yet) — otherwise keep whatever the user typed,
        // even across rebuilds triggered by navigating to the reward screens and back.
        if (existing != null && savedName.isEmpty() && savedX.isEmpty()) {
            savedName = existing.name();
            savedGroup = existing.group() != null ? existing.group() : "";
            savedX = String.valueOf((int) existing.x());
            savedY = String.valueOf((int) existing.y());
            savedZ = String.valueOf((int) existing.z());
            savedRadius = String.valueOf(existing.radius());
            savedDelay = String.valueOf(existing.delaySeconds());
        }

        groupField = makeField(addRenderableWidget, font, cx, cy + 10, halfW, "Group (optional)");
        groupField.setValue(savedGroup);
        groupField.setResponder(text -> savedGroup = text);

        nameField = makeField(addRenderableWidget, font, cx + halfW + 4, cy + 10, halfW, "Arena name");
        nameField.setValue(savedName);
        nameField.setResponder(text -> savedName = text);

        xField = makeField(addRenderableWidget, font, cx, cy + 50, posW, "X");
        xField.setValue(savedX);
        xField.setResponder(text -> savedX = text);

        yField = makeField(addRenderableWidget, font, cx + posW + 1, cy + 50, posW, "Y");
        yField.setValue(savedY);
        yField.setResponder(text -> savedY = text);

        zField = makeField(addRenderableWidget, font, cx + (posW + 1) * 2, cy + 50, posW, "Z");
        zField.setValue(savedZ);
        zField.setResponder(text -> savedZ = text);

        radiusField = makeField(addRenderableWidget, font, cx, cy + 90, halfW, "Radius");
        radiusField.setValue(savedRadius);
        radiusField.setResponder(text -> savedRadius = text);

        delayField = makeField(addRenderableWidget, font, cx + halfW + 4, cy + 90, halfW, "Delay (s)");
        delayField.setValue(savedDelay);
        delayField.setResponder(text -> savedDelay = text);

        addRenderableWidget.accept(Button.builder(Component.literal("My Pos"),
                btn -> {
                    var player = Minecraft.getInstance().player;
                    if (player != null) {
                        savedX = String.valueOf((int) player.getX());
                        savedY = String.valueOf((int) player.getY());
                        savedZ = String.valueOf((int) player.getZ());
                        xField.setValue(savedX);
                        yField.setValue(savedY);
                        zField.setValue(savedZ);
                    }
                }
        ).bounds(detailX + detailW - PANEL_PAD - 50, cy + 50, 50, BTN_H - 2).build());

        addRenderableWidget.accept(Button.builder(
                Component.literal("🎁 Rewards" + (rewardCount > 0 ? " (" + rewardCount + ")" : "")),
                btn -> onEditRewards.run()
        ).bounds(cx, cy + 130, fw, BTN_H).build());

        addRenderableWidget.accept(Button.builder(
                Component.literal("🎖 Participation Reward" + (participationRewardCount > 0 ? " (" + participationRewardCount + ")" : "")),
                btn -> onEditParticipationRewards.run()
        ).bounds(cx, cy + 150, fw, BTN_H).build());

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
                    zField == null || radiusField == null || delayField == null || groupField == null) return;
            String name = nameField.getValue().trim();
            if (name.isEmpty()) return;
            double x = Double.parseDouble(xField.getValue().trim());
            double y = Double.parseDouble(yField.getValue().trim());
            double z = Double.parseDouble(zField.getValue().trim());
            int radius = Integer.parseInt(radiusField.getValue().trim());
            int delay = Integer.parseInt(delayField.getValue().trim());
            String group = groupField.getValue().trim();
            onSubmit.submit(name, x, y, z, radius, delay, group);
        } catch (NumberFormatException ignored) {}
    }

    /** Clears all saved form state. Call this after a successful Create/Save, on Cancel,
     *  and before opening a fresh New/Edit Arena session, so stale input never bleeds
     *  into an unrelated arena's form. */
    public void reset() {
        savedName = "";
        savedGroup = "";
        savedX = "";
        savedY = "";
        savedZ = "";
        savedRadius = "";
        savedDelay = "";
    }

    public void render(GuiGraphicsExtractor g, Font font, GuiTheme theme,
                       int dx, int dt, int dw, String headerTitle) {
        g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
        g.text(font, headerTitle, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

        int fw = dw - PANEL_PAD * 2;
        int halfW = fw / 2 - 2;

        g.text(font, "Group", dx + PANEL_PAD, dt + 20, theme.subtext(), false);
        g.text(font, "Name", dx + PANEL_PAD + halfW + 4, dt + 20, theme.subtext(), false);
        g.text(font, "Position", dx + PANEL_PAD, dt + 60, theme.subtext(), false);
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