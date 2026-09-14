package net.alek.succorstadiums.mana;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;


public final class ManaRegenHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ManaHelper.tick(player);
            }
        });
    }

    private ManaRegenHandler() {}
}