package net.alek.succorstadiums.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerAttributesMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setMaxHealth(CallbackInfo ci) {
        Player self = (Player) (Object) this;

        AttributeInstance maxHealth = self.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(6.0);
        }

        if (self.getHealth() > 6.0f) {
            self.setHealth(6.0f);
        }
    }
}
