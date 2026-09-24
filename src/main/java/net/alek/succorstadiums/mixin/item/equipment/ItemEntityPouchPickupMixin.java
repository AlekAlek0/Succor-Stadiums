package net.alek.succorstadiums.mixin.item.equipment;

import net.alek.succorstadiums.datagen.ModItemTagProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.NonNullList;
import net.minecraft.stats.Stats;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import net.alek.succorstadiums.item.equipment.pouch.PouchSettingsHelper;
import net.alek.succorstadiums.item.equipment.pouch.PouchContents;
import net.alek.succorstadiums.component.ModComponents;
import net.alek.succorstadiums.item.ModItems;

// ItemEntityPouchPickupMixin class
@Mixin(ItemEntity.class)
public abstract class ItemEntityPouchPickupMixin {

    @Unique
    private static final int OWN_DROP_GRACE_TICKS = 40; // 2 seconds

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void succorstadiums$insertCoinsIntoPouch(Player player, CallbackInfo ci) {

        // Get self entity, owner, ground stack, pouch stack location, and pouchTypeId
        ItemEntity self = (ItemEntity) (Object) this;
        Entity owner = self.getOwner();
        ItemStack groundStack = self.getItem();
        ItemStack pouchStack = findPouch(player);
        Identifier pouchTypeId = BuiltInRegistries.ITEM.getKey(pouchStack.getItem());

        // Get pouch current contents component and unpack it into a mutable working list
        PouchContents contents = pouchStack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
        NonNullList<ItemStack> items = contents.copyItems();

        // If self is client side or pouch stack is empty return
        if (self.level().isClientSide()) {
            return;
        }
        if (pouchStack.isEmpty()) {
            return;
        }

        // If ground stack is not a coin then return and vanilla pickup
        if (!groundStack.is(ModItemTagProvider.COINS)) {
            return;
        }

        // If the player has auto deposit disabled return
        if (!PouchSettingsHelper.isAutoDepositEnabled(player, pouchTypeId)) {
            return;
        }

        // If I dropped this coin, and it is younger than 2 seconds return
        if (owner != null && owner.getUUID().equals(player.getUUID()) && self.getAge() < OWN_DROP_GRACE_TICKS) {
            return;
        }

        // Track how many coins from this ground stack still need a hom
        int orgCount = groundStack.getCount();
        int remaining = orgCount;

        // First pass fill existing matching stacks up to 99 potentially across multiple slots if one slot alone can't absorb everything
        for (int slot = 0; slot < items.size() && remaining > 0; slot++) {
            ItemStack slotStack = items.get(slot);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, groundStack)
                    && slotStack.getCount() < PouchContents.MAX_STACK_SIZE) {
                int move = Math.min(PouchContents.MAX_STACK_SIZE - slotStack.getCount(), remaining);
                slotStack.grow(move);
                remaining -= move;
            }
        }

        // Second pass whatever's left after merging spills into empty slots again potentially across several if the amount exceeds one slot's cap
        for (int slot = 0; slot < items.size() && remaining > 0; slot++) {
            if (items.get(slot).isEmpty()) {
                int move = Math.min(remaining, PouchContents.MAX_STACK_SIZE);
                items.set(slot, groundStack.copyWithCount(move));
                remaining -= move;
            }
        }

        // Get how many we actually inserted by subtracting the original count we picked up by how many we have left if its the same just return and let vanilla playerTouch run unmodified
        int inserted = orgCount - remaining;
        if (inserted <= 0) {
            return;
        }

        // Write the whole updated contents back onto the pouch component and award a stat to the player for picking up an item
        pouchStack.set(ModComponents.POUCH_CONTENTS, PouchContents.of(items));
        player.awardStat(Stats.ITEM_PICKED_UP.get(groundStack.getItem()), inserted);

        // If everything fit then replicate the parts of vanillas own pickup system like the pickup animation, particle effect, and removing item from the world etc
        if (remaining <= 0) {
            player.take(self, orgCount);
            self.discard();
            player.onItemPickup(self);
            ci.cancel();

        // Else if only a some coins fit shrink the ground stack to just what's and let vanillas playerTouch run
        } else {
            groundStack.setCount(remaining);
            self.setItem(groundStack);
        }
    }

    // Iterate through the inventory and find what stack contains the plains coin pouch and return it else return empty
    @Unique
    private static ItemStack findPouch(Player player) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.PLAINS_COIN_POUCH)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}