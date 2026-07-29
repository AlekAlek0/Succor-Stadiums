package net.alek.succorstadiums.mixin;

import net.alek.succorstadiums.entity.monsters.GrassCreeper;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void succorstadiums$explodeCreeper(CallbackInfo ci) {
        if ((Object) this instanceof GrassCreeper grassCreeper) {
            grassCreeper.customExplode();
            ci.cancel();
        }
    }
}