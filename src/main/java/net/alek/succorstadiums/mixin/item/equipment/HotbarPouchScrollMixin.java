package net.alek.succorstadiums.mixin.item.equipment;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.MouseHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.alek.succorstadiums.network.item.equipment.pouch.PouchScrollTransferPayload;
import net.alek.succorstadiums.item.equipment.pouch.PouchContents;
import net.alek.succorstadiums.config.SuccorStadiumsConfigScreen;
import net.alek.succorstadiums.config.PouchScrollDirection;
import net.alek.succorstadiums.component.ModComponents;
import net.alek.succorstadiums.client.ModKeyBindings;
import net.alek.succorstadiums.item.ModItems;

// HotbarPouchScrollMixin class
@Mixin(MouseHandler.class)
public abstract class HotbarPouchScrollMixin {

    @Final @Shadow private Minecraft minecraft;

    // Initialize messages when no coins are left
    @Unique
    private static final Component NO_COINS_TO_DEPOSIT_MESSAGE = Component.translatable("message.succorstadiums.no_coins_to_deposit");
    @Unique
    private static final Component NO_COINS_TO_WITHDRAW_MESSAGE = Component.translatable("message.succorstadiums.no_coins_to_withdraw");

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void succorstadiums$hotbarPouchScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {

        // Get player and pouch stack
        Player player = this.minecraft.player;
        assert player != null;
        ItemStack pouchStack = succorstadiums$findPouchInHotbar(player);

        // If player is a screen or gui overlay return
        if (this.minecraft.gui.screen() != null || this.minecraft.gui.overlay() != null) {
            return;
        }

        // If player is a spectator return or if player does not have the modifier keybinding pressed return
        if (player.isSpectator()) {
            return;
        }
        if (!succorstadiums$isModifierDown()) {
            return;
        }

        // If pouch is empty or if yoffset is 0 return
        if (pouchStack.isEmpty()) {
            return;
        }
        if (yoffset == 0) {
            return;
        }

        boolean scrollUp = yoffset > 0;
        boolean deposit = (SuccorStadiumsConfigScreen.getConfig().pouchScrollDirection == PouchScrollDirection.DEFAULT) == scrollUp;

        // If player cant transfer then send the corresponding message depending on the deposit boolean
        if (!succorstadiums$canTransfer(player, pouchStack, deposit)) {
            player.sendOverlayMessage(deposit ? NO_COINS_TO_DEPOSIT_MESSAGE : NO_COINS_TO_WITHDRAW_MESSAGE);
            ci.cancel();
            return;
        }

        // Send the PouchScrollTransferPayload and play a sound effect
        ClientPlayNetworking.send(new PouchScrollTransferPayload(deposit));
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F));
        ci.cancel();
    }


    // Iterate through the hotbar and find what stack contains the plains coin pouch and return it else return empty
    @Unique
    private ItemStack succorstadiums$findPouchInHotbar(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getNonEquipmentItems().get(i);
            if (stack.is(ModItems.PLAINS_COIN_POUCH)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    // Method to check if the player can transfer if they have emerald coins in their inventory or in their pouch contents else return false
    @Unique
    private boolean succorstadiums$canTransfer(Player player, ItemStack pouchStack, boolean deposit) {
        if (deposit) {
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (stack.is(ModItems.EMERALD_COIN)) {
                    return true;
                }
            }
        } else {
            PouchContents contents = pouchStack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
            for (ItemStack stack : contents.asList()) {
                if (stack.is(ModItems.EMERALD_COIN)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Accessor method for if the player is holding down the transfer modifier keybind
    @Unique
    private boolean succorstadiums$isModifierDown() {
        return ModKeyBindings.COIN_POUCH_TRANSFER_MODIFIER.isDown();
    }
}