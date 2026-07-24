package net.alek.succorstadiums.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;

public class BackpackPacketHandler {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(OpenBackpackPayload.TYPE, (payload, context) -> {
            var player = context.player();
            player.openMenu(new SimpleMenuProvider(
                    (syncId, inv, p) -> ChestMenu.threeRows(syncId, inv, player.getEnderChestInventory()),
                    Component.translatable("container.succorstadiums.backpack")
            ));
        });
    }
}