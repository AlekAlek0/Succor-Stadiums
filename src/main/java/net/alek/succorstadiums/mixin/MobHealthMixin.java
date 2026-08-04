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

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setBaseHealth(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;

        Double health =
                SuccorStadiumsConstants.MOB_HEALTH_OVERRIDES.get(self.getType());

        if (health == null) return;

        AttributeInstance maxHealth =
                self.getAttribute(Attributes.MAX_HEALTH);

        if (maxHealth != null) {
            maxHealth.setBaseValue(health);
            self.setHealth(health.floatValue());
        }
    }
}