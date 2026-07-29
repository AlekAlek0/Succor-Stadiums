package net.alek.succorstadiums.screen.mobarenagui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class PlayerPickerScreen {

    private static final int PANEL_PAD = 8;
    private static final int BTN_H = 16;
    private static final int ROW_H = 18;

    private final List<String> selectedPlayers = new ArrayList<>();

    public List<String> getSelectedPlayers() { return selectedPlayers; }
    public void clearSelection() { selectedPlayers.clear(); }

    public void buildWidgets(Consumer<net.minecraft.client.gui.components.AbstractWidget> addRenderableWidget,
                             int detailX, int detailW, int guiTop, int guiHeight,
                             Runnable onRebuild, Runnable onCancel, Runnable onConfirm) {

        List<String> online = new ArrayList<>();
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            conn.getOnlinePlayers().forEach(p -> online.add(p.getProfile().name()));
        }

        int cx = detailX + PANEL_PAD;
        int cy = guiTop + 36;
        int fw = detailW - PANEL_PAD * 2;

        for (int i = 0; i < Math.min(online.size(), 8); i++) {
            final String pName = online.get(i);
            boolean sel = selectedPlayers.contains(pName);
            addRenderableWidget.accept(Button.builder(
                    Component.literal((sel ? "☑ " : "☐ ") + pName),
                    btn -> {
                        if (selectedPlayers.contains(pName)) selectedPlayers.remove(pName);
                        else selectedPlayers.add(pName);
                        onRebuild.run();
                    }
            ).bounds(cx, cy + i * ROW_H, fw - 74, BTN_H).build());
        }

        int by = guiTop + guiHeight - BTN_H - PANEL_PAD;

        boolean allSelected = !online.isEmpty() && new HashSet<>(selectedPlayers).containsAll(online);
        addRenderableWidget.accept(Button.builder(
                Component.literal(allSelected ? "☐ Deselect All" : "☑ Select All"),
                btn -> {
                    if (allSelected) selectedPlayers.clear();
                    else { selectedPlayers.clear(); selectedPlayers.addAll(online); }
                    onRebuild.run();
                }
        ).bounds(detailX + PANEL_PAD, by, 80, BTN_H).build());

        addRenderableWidget.accept(Button.builder(Component.literal("✕ Cancel"),
                btn -> { selectedPlayers.clear(); onCancel.run(); }
        ).bounds(detailX + detailW - PANEL_PAD - 134, by, 50, BTN_H).build());

        addRenderableWidget.accept(Button.builder(Component.literal("▶ Confirm"),
                btn -> onConfirm.run()
        ).bounds(detailX + detailW - PANEL_PAD - 80, by, 80, BTN_H).build());
    }

    public void renderHeader(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                             int dx, int dt, int dw) {
        g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
        g.text(font, "Select Players to Start Arena", dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);
    }
}