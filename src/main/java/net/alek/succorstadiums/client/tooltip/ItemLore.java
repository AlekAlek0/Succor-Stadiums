package net.alek.succorstadiums.client.tooltip;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.locale.Language;
import net.minecraft.ChatFormatting;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

/**
 * Builds item tooltips purely from lang keys. Adding an item needs no code, only lang entries.
 * <p>
 * Keys, where <id> is the item's registry path:
 *   item.succorstadiums.<id>.lore        -> gray italic flavor line directly under the name
 *   item.succorstadiums.<id>.tooltip_N   -> contiguous lines (0, 1, 2...) directly under the lore
 *   item.succorstadiums.<id>.bottom_N    -> contiguous lines appended at the very end (after attributes)
 * <p>
 * Colors live in the lang text itself with section-sign codes. An empty string is a blank spacer line.
 */
public final class ItemLore {
    private static final String NAMESPACE = "succorstadiums";
    private static final String PREFIX = "item." + NAMESPACE + ".";
    private static final String MOD_NAME = FabricLoader.getInstance()
            .getModContainer(NAMESPACE)
            .map(mod -> mod.getMetadata().getName())
            .orElse(NAMESPACE);

    private static final Object[] ICONS = {
            HeartIcons.FULL, HeartIcons.HALF, ManaIcons.FULL, ManaIcons.HALF
    };

    private ItemLore() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!NAMESPACE.equals(id.getNamespace())) return;

            Language language = Language.getInstance();
            String base = PREFIX + id.getPath();
            int insertAt = Math.min(1, tooltip.size());

            String loreKey = base + ".lore";
            if (language.has(loreKey)) {
                tooltip.add(insertAt++, Component.translatable(loreKey, ICONS)
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }

            for (int i = 0; ; i++) {
                String key = base + ".tooltip_" + i;
                if (!language.has(key)) break;
                tooltip.add(insertAt++, Component.translatable(key, ICONS)
                        .withStyle(ChatFormatting.GRAY));
            }

            int bottomAt = bottomInsertIndex(tooltip, id, type.isAdvanced());
            for (int i = 0; ; i++) {
                String key = base + ".bottom_" + i;
                if (!language.has(key)) break;
                tooltip.add(bottomAt++, Component.translatable(key, ICONS)
                        .withStyle(ChatFormatting.GRAY));
            }
        });
    }

    /**
     * Index just after the vanilla attribute/durability lines, before the trailing
     * extras: the mod-name line and, with F3+H, the registry id and component count.
     */
    private static int bottomInsertIndex(List<Component> tooltip, Identifier id, boolean advanced) {
        int end = tooltip.size();

        // Mod name line (added by a mod-name tooltip mod if it ran before us)
        while (end > 1 && tooltip.get(end - 1).getString().equals(MOD_NAME)) end--;

        // Advanced tooltip extras start at the line holding the registry id
        if (advanced) {
            String idString = id.toString();
            for (int i = end - 1; i >= 1; i--) {
                if (tooltip.get(i).getString().equals(idString)) return i;
            }
        }
        return end;
    }
}