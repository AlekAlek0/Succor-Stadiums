package net.alek.succorstadiums.mixin;

import net.alek.succorstadiums.SuccorStadiumsConstants; // Import the new constants class
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerAttributesMixin {

    // Removed the local FIST_RANGE_ID, now using the one from constants

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setMaxHealth(CallbackInfo ci) {
        Player self = (Player) (Object) this;

        AttributeInstance maxHealth = self.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(SuccorStadiumsConstants.PLAYER_MAX_HEALTH); // Use constant
        }

        if (self.getHealth() > SuccorStadiumsConstants.PLAYER_MAX_HEALTH) { // Use constant
            self.setHealth((float) SuccorStadiumsConstants.PLAYER_MAX_HEALTH); // Use constant
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;

        AttributeInstance attackRange = self.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attackRange == null) return;

        boolean isHoldingFist = self.getMainHandItem().isEmpty();

        if (isHoldingFist) {
            if (attackRange.getModifier(SuccorStadiumsConstants.PLAYER_FIST_RANGE_ID) == null) { // Use constant
                attackRange.addPermanentModifier(
                        new AttributeModifier(
                                SuccorStadiumsConstants.PLAYER_FIST_RANGE_ID, // Use constant
                                SuccorStadiumsConstants.PLAYER_FIST_RANGE_MODIFIER, // Use constant
                                AttributeModifier.Operation.ADD_VALUE
                        )
                );
            }
        } else {
            attackRange.removeModifier(SuccorStadiumsConstants.PLAYER_FIST_RANGE_ID); // Use constant
        }
    }
}
