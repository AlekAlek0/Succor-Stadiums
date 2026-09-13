package net.alek.succorstadiums.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.alek.succorstadiums.network.arena.OpenMobArenaRequestPayload;
import net.alek.succorstadiums.network.backpack.OpenBackpackPayload;
import net.alek.succorstadiums.config.SuccorStadiumsConfigScreen;

public class KeyInputHandler {

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (ModKeyBindings.OPEN_CONFIG.consumeClick()) {
                client.gui.setScreen(
                        SuccorStadiumsConfigScreen.create(client.gui.screen())
                );
            }

            while (ModKeyBindings.OPEN_MOB_ARENA_GUI.consumeClick()) {
                ClientPlayNetworking.send(new OpenMobArenaRequestPayload());
            }

            while (ModKeyBindings.OPEN_BACKPACK.consumeClick()) {
                ClientPlayNetworking.send(new OpenBackpackPayload());
            }
        });
    }
}