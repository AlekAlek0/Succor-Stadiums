package net.alek.succorstadiums.network.item.equipment.pouch;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.alek.succorstadiums.item.equipment.pouch.PouchSettingsHelper;

// SetPouchAutoDepositHandler class
public class SetPouchAutoDepositHandler {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SetPouchAutoDepositPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            PouchSettingsHelper.setAutoDepositEnabled(player, payload.pouchTypeId(), payload.enabled());
        });
    }
}