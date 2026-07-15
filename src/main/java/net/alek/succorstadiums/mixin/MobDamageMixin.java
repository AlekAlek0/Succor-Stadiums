package net.alek.succorstadiums.mixin;

import net.alek.succorstadiums.SuccorStadiumsConstants; // Import the new constants class
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class MobDamageMixin {

    // Removed the local MOB_DAMAGE map and its static initializer

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setBaseDamage(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;

        // Use the shared MOB_DAMAGE_OVERRIDES map from SuccorStadiumsConstants
        Double damage = SuccorStadiumsConstants.MOB_DAMAGE_OVERRIDES.get(self.getType());

        if (damage == null) return;

        AttributeInstance atk = self.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(damage);
    }
}
