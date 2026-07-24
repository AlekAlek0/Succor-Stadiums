package net.alek.succorstadiums.client;

import net.alek.succorstadiums.network.OpenBackpackPayload;
import net.alek.succorstadiums.network.OpenMobArenaRequestPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class KeyInputHandler {

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (ModKeyBindings.OPEN_MOB_ARENA_GUI.consumeClick()) {
                ClientPlayNetworking.send(new OpenMobArenaRequestPayload());
            }

            while (ModKeyBindings.OPEN_BACKPACK.consumeClick()) {
                ClientPlayNetworking.send(new OpenBackpackPayload());
            }
        });
    }
}