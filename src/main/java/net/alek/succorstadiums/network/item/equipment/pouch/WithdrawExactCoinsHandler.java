package net.alek.succorstadiums.network.item.equipment.pouch;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import net.alek.succorstadiums.component.ModComponents;
import net.alek.succorstadiums.datagen.ModItemTagProvider;
import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.item.equipment.pouch.PouchContents;

public class WithdrawExactCoinsHandler {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(WithdrawExactCoinsPayload.TYPE, (payload, context) -> {
            System.out.println("[succorstadiums] server received withdraw payload, tradeIndex=" + payload.tradeIndex());
            ServerPlayer player = context.player();

            if (!(player.containerMenu instanceof MerchantMenu merchantMenu)) {
                System.out.println("[succorstadiums] player.containerMenu is not a MerchantMenu: " + player.containerMenu.getClass());
                return;
            }

            MerchantOffers offers = merchantMenu.getOffers();
            int tradeIndex = payload.tradeIndex();
            if (tradeIndex < 0 || tradeIndex >= offers.size()) {
                return;
            }

            MerchantOffer offer = offers.get(tradeIndex);
            int needed = 0;
            if (offer.getCostA().is(ModItems.EMERALD_COIN)) {
                needed += offer.getCostA().getCount();
            }
            if (offer.getCostB().is(ModItems.EMERALD_COIN)) {
                needed += offer.getCostB().getCount();
            }
            if (needed <= 0) {
                return;
            }

            withdrawExact(player, needed);
            merchantMenu.tryMoveItems(tradeIndex);
        });
    }

    private static void withdrawExact(ServerPlayer player, int amount) {
        ItemStack pouchStack = findPouch(player);
        if (pouchStack.isEmpty()) {
            return;
        }

        PouchContents contents = pouchStack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
        NonNullList<ItemStack> items = contents.copyItems();

        int remaining = amount;
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

        pouchStack.set(ModComponents.POUCH_CONTENTS, PouchContents.of(items));
    }

    private static ItemStack findPouch(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.PLAINS_COIN_POUCH)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}