package net.alek.succorstadiums.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;

public class ScreenCloseHandler {

    private static final Component BACKPACK_TITLE = Component.translatable("container.succorstadiums.backpack");

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.allowKeyPress(screen).register((scr, event) -> {
                if (ModKeyBindings.OPEN_BACKPACK.matches(event)
                        && scr instanceof ContainerScreen cs
                        && cs.getTitle().equals(BACKPACK_TITLE)) {
                    client.setScreen(null);
                    return false;
                }
                return true;
            });
        });
    }
}