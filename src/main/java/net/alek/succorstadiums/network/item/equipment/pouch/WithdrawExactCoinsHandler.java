package net.alek.succorstadiums.network.item.equipment.pouch;

import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.alek.succorstadiums.item.equipment.pouch.PouchContents;
import net.alek.succorstadiums.datagen.ModItemTagProvider;
import net.alek.succorstadiums.component.ModComponents;
import net.alek.succorstadiums.item.ModItems;

public class WithdrawExactCoinsHandler {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(WithdrawExactCoinsPayload.TYPE, (payload, context) -> {

            // Get player
            ServerPlayer player = context.player();

            // If the player is not in the merchantMenu return
            if (!(player.containerMenu instanceof MerchantMenu merchantMenu)) {
                return;
            }

            // Get merchant offers and trade index
            MerchantOffers offers = merchantMenu.getOffers();
            int tradeIndex = payload.tradeIndex();

            // Check if the trade index is a valid index in the offers list if not return
            if (tradeIndex < 0 || tradeIndex >= offers.size()) {
                return;
            }

            // Get the offer trade for the specific tradeIndex and initialize a variable of how many required coins
            MerchantOffer offer = offers.get(tradeIndex);
            int needed = 0;

            // Check if cost item has the coins tag if so set needed to the cost of the offer else return
            if (offer.getCostA().is(ModItems.EMERALD_COIN)) {
                needed += offer.getCostA().getCount();
            }
            if (offer.getCostB().is(ModItems.EMERALD_COIN)) {
                needed += offer.getCostB().getCount();
            }
            if (needed <= 0) {
                return;
            }

            // Withdraw the exact amount we need for the player and call the vanilla tryMoveItems method to autofill the slots for us with the coins
            withdrawExact(player, needed);
            merchantMenu.tryMoveItems(tradeIndex);
        });
    }

    // Helper method to get withdraw the exact amount of coins needed for the trade in our pouch
    private static void withdrawExact(ServerPlayer player, int amount) {

        // Get the pouch stack and return if the player doesn't have a pouch
        ItemStack pouchStack = findPouch(player);
        if (pouchStack.isEmpty()) {
            return;
        }

        // Get pouch current contents component and unpack it into a mutable working list
        PouchContents contents = pouchStack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
        NonNullList<ItemStack> items = contents.copyItems();

        // Set the remaining count to the amount we currently have
        int remaining = amount;

        // Withdraw coins from the pouch until it has withdrawn the requested amount
        for (int slot = 0; slot < items.size() && remaining > 0; slot++) {
            ItemStack slotStack = items.get(slot);
            if (slotStack.is(ModItemTagProvider.COINS)) {
                int take = Math.min(remaining, slotStack.getCount());
                ItemStack withdrawn = slotStack.copyWithCount(take);
                if (!player.getInventory().add(withdrawn)) {
                    player.drop(withdrawn, false);
                }
                slotStack.shrink(take);
                if (slotStack.isEmpty()) {
                    items.set(slot, ItemStack.EMPTY);
                }
                remaining -= take;
            }
        }

        // Write the whole updated list back onto the pouch component
        pouchStack.set(ModComponents.POUCH_CONTENTS, PouchContents.of(items));
    }

    // Iterate through the inventory and find what stack contains the plains coin pouch
    // and return it else return empty
    private static ItemStack findPouch(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.PLAINS_COIN_POUCH)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}