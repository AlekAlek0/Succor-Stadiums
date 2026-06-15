package net.alek.succorstadiums.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Mob.class)
public class MobHealthMixin {

    @Unique
    private static final Map<Class<?>, Double> MOB_HEALTH = new HashMap<>();

    static {
        MOB_HEALTH.put(Skeleton.class, 10.0);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setBaseDamage(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;

        Double health = MOB_HEALTH.get(self.getClass());
        if (health == null) return;

        AttributeInstance atk = self.getAttribute(Attributes.MAX_HEALTH);
        if (atk != null) atk.setBaseValue(health);
    }
}