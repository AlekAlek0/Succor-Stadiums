package net.alek.succorstadiums.client.tooltip;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

// ManaIcons class
public final class ManaIcons {
    private static final FontDescription FONT = new FontDescription.Resource(
            Identifier.fromNamespaceAndPath("succorstadiums", "icons"));

    // White so the font doesn't tint the texture not italic so lore lines don't slant it
    public static final Component FULL = Component.literal("\uE000")
            .withStyle(s -> s.withFont(FONT).withColor(0xFFFFFF).withItalic(false));
    public static final Component HALF = Component.literal("\uE001")
            .withStyle(s -> s.withFont(FONT).withColor(0xFFFFFF).withItalic(false));

    private ManaIcons() {}
}