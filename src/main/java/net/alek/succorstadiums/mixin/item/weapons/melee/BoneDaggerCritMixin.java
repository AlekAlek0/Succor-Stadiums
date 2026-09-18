package net.alek.succorstadiums.mixin.item.weapons.melee;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.Mixin;

import net.alek.succorstadiums.item.weapons.melee.BoneDaggerItem;

// BoneDaggerCritMixin class
@Mixin(Player.class)
public class BoneDaggerCritMixin {

    // Inject code at the attack method of the player class and return a crit damage of 2 instead of the vanilla 1.5
    @ModifyConstant(method = "attack", constant = @Constant(floatValue = 1.5F))
    private float boneDaggerCritMultiplier(float original) {
        Player self = (Player) (Object) this;
        ItemStack mainHand = self.getMainHandItem();

        if (mainHand.getItem() instanceof BoneDaggerItem) {
            return 2.0F;
        }

        return original;
    }
}