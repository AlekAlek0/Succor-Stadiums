package net.alek.succorstadiums.mixin;

import net.alek.succorstadiums.item.interfaces.FreeRepair;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin {

    @Final
    @Shadow
    private DataSlot cost;

    @Inject(method = "createResult", at = @At("TAIL"))
    private void onCreateResult(CallbackInfo ci) {
        AnvilMenu anvil = (AnvilMenu) (Object) this;
        ItemStack input = anvil.getSlot(0).getItem();
        ItemStack material = anvil.getSlot(1).getItem();
        ItemStack output = anvil.getSlot(2).getItem();

        if (output.isEmpty()) return;

        if (input.getItem() instanceof FreeRepair freeRepair
                && freeRepair.isFreeRepair(input, material)) {
            cost.set(0); // Show 0 to the player
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void onMayPickup(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        AnvilMenu anvil = (AnvilMenu) (Object) this;
        ItemStack input = anvil.getSlot(0).getItem();
        ItemStack material = anvil.getSlot(1).getItem();
        ItemStack output = anvil.getSlot(2).getItem();

        if (output.isEmpty()) return;

        if (input.getItem() instanceof FreeRepair freeRepair
                && freeRepair.isFreeRepair(input, material)) {
            cir.setReturnValue(true); // Allow pickup even at cost 0
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void onTakeResult(Player player, ItemStack stack, CallbackInfo ci) {
        AnvilMenu anvil = (AnvilMenu) (Object) this;
        ItemStack input = anvil.getSlot(0).getItem();
        ItemStack material = anvil.getSlot(1).getItem();

        if (input.getItem() instanceof FreeRepair freeRepair
                && freeRepair.isFreeRepair(input, material)) {
            cost.set(0); // Ensure nothing gets deducted
        }
    }
}