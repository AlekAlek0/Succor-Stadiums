package net.alek.succorstadiums.mixin.item.armor;

import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Unit;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import net.alek.succorstadiums.item.armor.BaleArmorItem;
import net.alek.succorstadiums.item.ModItems;

@Mixin(ProjectileWeaponItem.class)
public abstract class BaleArmorSaveAmmoMixin {

    @Unique
    private static final float BALE_SET_SAVE_CHANCE = 0.03F;

    @Inject(method = "useAmmo", at = @At("HEAD"), cancellable = true)
    private static void succorstadiums$baleSetSaveArrow(ItemStack weapon, ItemStack projectile, LivingEntity holder, boolean forceInfinite, CallbackInfoReturnable<ItemStack> cir
    ) {
        if (forceInfinite || !(holder.level() instanceof ServerLevel)) {
            return;
        }
        if (weapon.getItem() instanceof CrossbowItem) {
            return;
        }
        if (!projectile.is(ModItems.BALE_ARROW)) {
            return;
        }
        if (!BaleArmorItem.isWearingFullBaleSet(holder)) {
            return;
        }
        if (holder.getRandom().nextFloat() >= BALE_SET_SAVE_CHANCE) {
            return;
        }

        ItemStack saved = projectile.copyWithCount(1);
        saved.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        cir.setReturnValue(saved);
    }
}