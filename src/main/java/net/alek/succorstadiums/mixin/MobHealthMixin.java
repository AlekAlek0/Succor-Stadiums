package net.alek.succorstadiums.mixin;

import net.alek.succorstadiums.SuccorStadiumsConstants;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class MobHealthMixin {

    // Removed the local MOB_HEALTH map and its static initializer

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setBaseDamage(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;

        // Use the shared MOB_HEALTH_OVERRIDES map from SuccorStadiumsConstants
        Double health = SuccorStadiumsConstants.MOB_HEALTH_OVERRIDES.get(self.getType());
        if (health == null) return;

        AttributeInstance atk = self.getAttribute(Attributes.MAX_HEALTH);
        if (atk != null) atk.setBaseValue(health);
    }
}
