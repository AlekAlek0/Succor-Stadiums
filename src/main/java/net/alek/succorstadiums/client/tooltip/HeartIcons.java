package net.alek.succorstadiums.client.tooltip;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

// HeartIcons class
public final class HeartIcons {
    private static final FontDescription FONT = new FontDescription.Resource(
            Identifier.fromNamespaceAndPath("succorstadiums", "icons"));

    public static final Component FULL = Component.literal("\uE002")
            .withStyle(s -> s.withFont(FONT).withColor(0xFFFFFF).withItalic(false));
    public static final Component HALF = Component.literal("\uE003")
            .withStyle(s -> s.withFont(FONT).withColor(0xFFFFFF).withItalic(false));

    private HeartIcons() {}
}