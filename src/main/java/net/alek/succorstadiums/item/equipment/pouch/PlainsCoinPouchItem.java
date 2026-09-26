package net.alek.succorstadiums.item.equipment.pouch;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.datagen.ModItemTagProvider;
import net.alek.succorstadiums.component.ModComponents;

// PlainsCoinPouchItem class
public class PlainsCoinPouchItem extends Item {
    public PlainsCoinPouchItem(Properties properties) {
        super(properties);
    }

    // Override the overrideOtherStackedOnMe method for our own pouch logic with coins
    @Override
    public boolean overrideOtherStackedOnMe(@NonNull ItemStack self, @NonNull ItemStack other,
                                            @NonNull Slot slot, @NonNull ClickAction clickAction,
                                            @NonNull Player player, @NonNull SlotAccess carriedItem) {

        // Get pouch current contents component and unpack it into a mutable working list
        PouchContents contents = self.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
        NonNullList<ItemStack> items = contents.copyItems();

        // If click action is not primary mouse button,
        // if itemstack in mouse is empty or does not contain the coins tag
        if (clickAction != ClickAction.PRIMARY || other.isEmpty() || !other.is(ModItemTagProvider.COINS)) {
            return false;
        }

        // If the slot the pouch is in doesn't allow modification return false
        if (!slot.allowModification(player)) {
            return false;
        }

        // Get the itemstack count
        int remaining = other.getCount();

        // First pass to try and merge into an existing matching stack
        for (int i = 0; i < items.size() && remaining > 0; i++) {
            ItemStack slotStack = items.get(i);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, other)
                    && slotStack.getCount() < PouchContents.MAX_STACK_SIZE) {
                int move = Math.min(PouchContents.MAX_STACK_SIZE - slotStack.getCount(), remaining);
                slotStack.grow(move);
                remaining -= move;
            }
        }

        // Second pass if first pass found nothing find the next available empty slot
        // and place a fresh 1 count copy of the coin
        for (int i = 0; i < items.size() && remaining > 0; i++) {
            if (items.get(i).isEmpty()) {
                int move = Math.min(remaining, PouchContents.MAX_STACK_SIZE);
                items.set(i, other.copyWithCount(move));
                remaining -= move;
            }
        }

        // Get the total amount of coins we inserted by
        // getting the current count and subtracting the remaining count
        int inserted = other.getCount() - remaining;

        // If we inserted at least one write the updated contents
        // back to the pouch and shrink the cursor itemstack
        if (inserted > 0) {
            self.set(ModComponents.POUCH_CONTENTS, PouchContents.of(items));
            other.shrink(inserted);

            // Set cursor stack to empty if remaining is empty or the updated count and play sound effect
            carriedItem.set(other.isEmpty() ? ItemStack.EMPTY : other);
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        } else {
            // Play bundle insert fail sound effect if we failed to insert any
            player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
        }

        // Broadcast the change to reopen and resync pouch container menu
        succorstadiums$broadcastChangesOnContainerMenu(player);
        return true;
    }

    // Helper method to broadcast changes to the pouch container menu
    private void succorstadiums$broadcastChangesOnContainerMenu(Player player) {
        AbstractContainerMenu containerMenu = player.containerMenu;
        containerMenu.slotsChanged(player.getInventory());
    }

    @Override
    public @NonNull InteractionResult use(Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Only do something if level is server sided
        if (!level.isClientSide()) {

            // Get the pouches current contents and create a new pouch container with the contents
            PouchContents contents = stack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
            PouchContainer container = new PouchContainer(contents, updated -> stack.set(ModComponents.POUCH_CONTENTS, updated));

            // Open the pouch
            player.openMenu(new SimpleMenuProvider(
                    (syncId, inventory, p) -> ChestMenu.threeRows(syncId, inventory, container),
                    Component.translatable("container.succorstadiums.plains_coin_pouch")
            ));

            // Play a sound effect and return a interaction result success
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }
}