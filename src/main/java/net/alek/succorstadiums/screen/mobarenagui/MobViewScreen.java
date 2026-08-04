package net.alek.succorstadiums.screen.mobarenagui;

import net.alek.succorstadiums.network.ArenaDataPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Consumer;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

public class MobViewScreen {

    private static final int ROW_H = 18;
    private static final int PANEL_PAD = 8;
    private static final int DETAIL_LINE_HEIGHT = 12;

    private static final String[] ARMOR_SLOTS = {"Helmet", "Chestplate", "Leggings", "Boots"};

    // ── Widgets ────────────────────────────────────────────────────────────

    public void buildWidgets(Consumer<Button> addRenderableWidget,
                             int detailX, int guiTop, int guiHeight, int btnH,
                             Runnable onBack) {
        Button back = Button.builder(Component.literal("< Back"),
                        btn -> onBack.run())
                .bounds(detailX + PANEL_PAD, guiTop + guiHeight - btnH - PANEL_PAD, 50, btnH)
                .build();
        addRenderableWidget.accept(back);
    }

    // ── Rendering ──────────────────────────────────────────────────────────

    public void render(GuiGraphicsExtractor g, Font font, GuiTheme theme,
                       int dx, int dt, int dw, int guiTop,
                       int selectedWave, ArenaDataPayload.WaveEntry wave) {

        g.fill(dx, dt, dx + dw, dt + 16, 0xFF5C7ABA);
        g.text(font, "Mobs in Wave " + selectedWave, dx + PANEL_PAD, dt + 4, 0xFFFFFFFF, false);

        if (wave == null || wave.mobs().isEmpty()) {
            g.text(font, "No mobs in this wave.", dx + PANEL_PAD, guiTop + 28, theme.subtext(), false);
            return;
        }

        int currentY = guiTop + 24;
        for (int i = 0; i < wave.mobs().size(); i++) {
            ArenaDataPayload.MobEntry mob = wave.mobs().get(i);

            if (i % 2 == 0) {
                int rowHeight = computeMobEntryHeight(mob);
                g.fill(dx, currentY, dx + dw, currentY + rowHeight,
                        theme.getTheme() != Theme.LIGHT ? 0x15FFFFFF : 0x11000000);
            }

            String mobDisplay = mob.count() + "x  " + formatIdentifierForDisplay(mob.mobType());
            String variantDisplay = variantDisplayFor(mob);
            if (mob.size() != null) {
                mobDisplay += variantDisplay.isEmpty()
                        ? " (Variant: " + mob.size() + ")"
                        : " (" + variantDisplay + ")";
            }
            g.text(font, mobDisplay, dx + PANEL_PAD, currentY + 4, theme.text(), false);
            currentY += ROW_H;

            currentY = renderMobDetailLines(g, font, theme, mob, dx + PANEL_PAD + 10, currentY);
            currentY += 4;
        }
    }

    static int renderMobDetailLines(GuiGraphicsExtractor g, Font font, GuiTheme theme,
                                    ArenaDataPayload.MobEntry mob, int x, int startY) {
        int y = startY;

        if (mob.mainHandItem() != null && !mob.mainHandItem().isEmpty()) {
            g.text(font, "  Main Hand: " + formatIdentifierForDisplay(mob.mainHandItem()),
                    x, y + 4, theme.subtext(), false);
            y += DETAIL_LINE_HEIGHT;
        }
        if (mob.offHandItem() != null && !mob.offHandItem().isEmpty()) {
            g.text(font, "  Off Hand: " + formatIdentifierForDisplay(mob.offHandItem()),
                    x, y + 4, theme.subtext(), false);
            y += DETAIL_LINE_HEIGHT;
        }
        if (mob.armorItems() != null && !mob.armorItems().isEmpty()) {
            for (int s = 0; s < Math.min(mob.armorItems().size(), ARMOR_SLOTS.length); s++) {
                g.text(font, "  " + ARMOR_SLOTS[s] + ": " + formatIdentifierForDisplay(mob.armorItems().get(s)),
                        x, y + 4, theme.subtext(), false);
                y += DETAIL_LINE_HEIGHT;
            }
        }
        if (mob.ridingMob() != null && !mob.ridingMob().isEmpty()) {
            g.text(font, "  Riding: " + formatIdentifierForDisplay(mob.ridingMob()),
                    x, y + 4, theme.subtext(), false);
            y += DETAIL_LINE_HEIGHT;
        }
        if (mob.potionEffects() != null && !mob.potionEffects().isEmpty()) {
            g.text(font, "  Potion Effects:", x, y + 4, theme.subtext(), false);
            y += DETAIL_LINE_HEIGHT;
            for (String effect : mob.potionEffects().split(",")) {
                String[] parts = effect.split(":");
                if (parts.length >= 3) {
                    String ampStr = parts[parts.length - 1];
                    String durStr = parts[parts.length - 2];
                    String effectId = String.join(":", Arrays.copyOfRange(parts, 0, parts.length - 2));
                    String durDisplay = ("-1".equals(durStr) || "0".equals(durStr)) ? "Infinite" : durStr + "s";
                    g.text(font, "    - " + formatIdentifierForDisplay(effectId)
                                    + " (" + durDisplay + ", Amp " + ampStr + ")",
                            x + 10, y + 4, theme.subtext(), false);
                    y += DETAIL_LINE_HEIGHT;
                }
            }
        }
        if (mob.enchantments() != null && !mob.enchantments().isEmpty()) {
            g.text(font, "  Enchantments:", x, y + 4, theme.subtext(), false);
            y += DETAIL_LINE_HEIGHT;
            for (String enchantment : mob.enchantments().split(",")) {
                String[] parts = enchantment.split(":");
                if (parts.length >= 3) {
                    String lvlStr = parts[parts.length - 1];
                    String target = parts[0];
                    String enchantId = String.join(":", Arrays.copyOfRange(parts, 1, parts.length - 1));
                    g.text(font, "    - " + formatIdentifierForDisplay(enchantId)
                                    + " (Lvl " + lvlStr + ") on " + targetDisplayFor(target),
                            x + 10, y + 4, theme.subtext(), false);
                    y += DETAIL_LINE_HEIGHT;
                }
            }
        }

        return y;
    }

    public static int computeMobEntryHeight(ArenaDataPayload.MobEntry mob) {
        int h = ROW_H;
        if (mob.mainHandItem() != null && !mob.mainHandItem().isEmpty()) h += DETAIL_LINE_HEIGHT;
        if (mob.offHandItem()  != null && !mob.offHandItem().isEmpty())  h += DETAIL_LINE_HEIGHT;
        if (mob.armorItems()   != null && !mob.armorItems().isEmpty())   h += mob.armorItems().size() * DETAIL_LINE_HEIGHT;
        if (mob.ridingMob()    != null && !mob.ridingMob().isEmpty())    h += DETAIL_LINE_HEIGHT;
        if (mob.potionEffects() != null && !mob.potionEffects().isEmpty()) {
            h += DETAIL_LINE_HEIGHT;
            h += mob.potionEffects().split(",").length * DETAIL_LINE_HEIGHT;
        }
        if (mob.enchantments() != null && !mob.enchantments().isEmpty()) {
            h += DETAIL_LINE_HEIGHT;
            h += mob.enchantments().split(",").length * DETAIL_LINE_HEIGHT;
        }
        if (mob.size() != null) {
            h += DETAIL_LINE_HEIGHT;
        }
        return h + 4;
    }

    // ── Display helpers ───────────────────────────────────────────────────

    static String variantDisplayFor(ArenaDataPayload.MobEntry mob) {
        if (mob.size() == null) return "";
        boolean isSlime = mob.mobType().equals("minecraft:slime") || mob.mobType().equals(MOD_ID + ":banana_slime");
        boolean isZombieLike = mob.mobType().equals("minecraft:zombie") || mob.mobType().equals("minecraft:zombie_villager") || mob.mobType().equals(MOD_ID + ":zombie_farmer");
        if (isSlime) {
            if (mob.size() == 1) return "Small";
            if (mob.size() == 2) return "Medium";
            if (mob.size() == 4) return "Large";
        } else if (isZombieLike) {
            if (mob.size() == -1) return "Baby";
            if (mob.size() == 0) return "Adult";
        }
        return "";
    }

    private static String targetDisplayFor(String targetKey) {
        for (int t = 0; t < AddMobScreen.ENCHANT_TARGET_KEYS.length; t++) {
            if (AddMobScreen.ENCHANT_TARGET_KEYS[t].equals(targetKey)) {
                return AddMobScreen.ENCHANT_TARGETS[t];
            }
        }
        return targetKey;
    }

    static String formatIdentifierForDisplay(String identifier) {
        if (identifier == null || identifier.isEmpty()) return "";
        String path = identifier.contains(":") ? identifier.substring(identifier.indexOf(":") + 1) : identifier;
        return Arrays.stream(path.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }
}