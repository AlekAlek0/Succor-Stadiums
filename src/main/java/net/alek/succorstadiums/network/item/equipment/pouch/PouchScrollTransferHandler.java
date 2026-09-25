package net.alek.succorstadiums.network.item.equipment.pouch;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.alek.succorstadiums.item.equipment.pouch.PouchContents;
import net.alek.succorstadiums.datagen.ModItemTagProvider;
import net.alek.succorstadiums.component.ModComponents;
import net.alek.succorstadiums.item.ModItems;

// PouchScrollTransferHandler class
public class PouchScrollTransferHandler {

    private PouchScrollTransferHandler() {}

    // Register method
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(PouchScrollTransferPayload.TYPE, (payload, context) -> {

            // Get the player and pouch stack
            ServerPlayer player = context.player();
            ItemStack pouchStack = findPouchInHotbar(player);

            // If pouch is empty return
            if (pouchStack.isEmpty()) {
                return;
            }

            // If payload is to deposit then run depositOneCoin method else run withdrawOneCoin method
            if (payload.deposit()) {
                depositOneCoin(player, pouchStack);
            } else {
                withdrawOneCoin(player, pouchStack);
            }
        });
    }

    // Iterate through the hotbar and find what stack contains the plains coin pouch and return it else return empty
    private static ItemStack findPouchInHotbar(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getNonEquipmentItems().get(i);
            if (stack.is(ModItems.PLAINS_COIN_POUCH)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void depositOneCoin(ServerPlayer player, ItemStack pouchStack) {

        // Get the players inventory and iterate through it getting each stack
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getNonEquipmentItems().size(); i++) {
            ItemStack stack = inventory.getNonEquipmentItems().get(i);

            // If stack has coin item tag continue
            if (stack.is(ModItemTagProvider.COINS)) {

                // Get pouch current contents component and unpack it into a mutable working list
                PouchContents contents = pouchStack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
                NonNullList<ItemStack> items = contents.copyItems();
                boolean inserted = false;

                // First pass to try and merge into an existing matching stack
                for (int slot = 0; slot < items.size() && !inserted; slot++) {
                    ItemStack slotStack = items.get(slot);
                    if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, stack) && slotStack.getCount() < PouchContents.MAX_STACK_SIZE) {
                        slotStack.grow(1);
                        inserted = true;
                    }
                }

                // Second pass if first pass found nothing find the next available empty slot and place a fresh 1 count copy of the coin
                if (!inserted) {
                    for (int slot = 0; slot < items.size() && !inserted; slot++) {
                        if (items.get(slot).isEmpty()) {
                            items.set(slot, stack.copyWithCount(1));
                            inserted = true;
                        }
                    }
                }

                // If either pass succeeded write the item list back to pouch contents shrink the stack by 1 and return
                if (inserted) {
                    pouchStack.set(ModComponents.POUCH_CONTENTS, PouchContents.of(items));
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        inventory.getNonEquipmentItems().set(i, ItemStack.EMPTY);
                    }
                }
                return;
            }
        }
    }

    private static void withdrawOneCoin(ServerPlayer player, ItemStack pouchStack) {

        // Get pouch current contents component and unpack it into a mutable working list
        PouchContents contents = pouchStack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
        NonNullList<ItemStack> items = contents.copyItems();

        // Iterate through the slots if it matches coin tag peel off 1 coin as a fresh single count stack
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack slotStack = items.get(slot);
            if (slotStack.is(ModItemTagProvider.COINS)) {
                ItemStack single = slotStack.copyWithCount(1);

                // Try to insert into players inventory if full drop the coin and shrink the stack by 1 clears it if empty
                if (!player.getInventory().add(single)) {
                    player.drop(single, false);
                }
                slotStack.shrink(1);
                if (slotStack.isEmpty()) {
                    items.set(slot, ItemStack.EMPTY);
                }

                // Write the whole updated list back onto the pouch component and return
                pouchStack.set(ModComponents.POUCH_CONTENTS, PouchContents.of(items));
                return;
            }
        }
    }
}