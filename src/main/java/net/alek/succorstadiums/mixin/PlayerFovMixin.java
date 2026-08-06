package net.alek.succorstadiums.mixin;

import net.alek.succorstadiums.effect.ModEffects;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class PlayerFovMixin {

    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void succorstadiums$ignoreParalysisFov(final boolean firstPerson, final float effectScale, final CallbackInfoReturnable<Float> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer)(Object)this;
        if (self.hasEffect(ModEffects.PARALYSIS)) {
            cir.setReturnValue(1.0F);
        }
    }
}