package net.alek.succorstadiums.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Mob.class)
public class MobDamageMixin {

    @Unique
    private static final Map<Class<?>, Double> MOB_DAMAGE = new HashMap<>();

    static {
        MOB_DAMAGE.put(Zombie.class,   0.5);
        MOB_DAMAGE.put(Spider.class,   0.5);
        MOB_DAMAGE.put(Skeleton.class, 0.5);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setBaseDamage(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;

        Double damage = MOB_DAMAGE.get(self.getClass());

        if (damage == null) return;

        AttributeInstance atk = self.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(damage);
    }
}