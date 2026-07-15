package net.alek.succorstadiums.client;

import net.alek.succorstadiums.network.OpenMobArenaRequestPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class KeyInputHandler {

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (ModKeyBindings.OPEN_MOB_ARENA_GUI.consumeClick()) {

                if (client.screen != null) {

                    System.out.println("Closing screen: " + client.screen.getClass().getName());

                    client.setScreen(null);

                } else {

                    ClientPlayNetworking.send(
                            new OpenMobArenaRequestPayload()
                    );

                }
            }

        });

    }
}