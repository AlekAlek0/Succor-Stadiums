package net.alek.succorstadiums.screen;

import net.alek.succorstadiums.network.ArenaDataPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class MobArenaScreenHandler {

    public static void register() {

        ClientPlayNetworking.registerGlobalReceiver(ArenaDataPayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof MobArenaScreen screen) {
                        screen.receiveData(payload);
                    }
                })
        );
    }
}