package net.alek.succorstadiums.mixin;

import net.minecraft.resources.Identifier;
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

    private static final Identifier FIST_RANGE_ID = Identifier.withDefaultNamespace("player_fist_range");

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

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;

        AttributeInstance attackRange = self.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attackRange == null) return;

        boolean isHoldingFist = self.getMainHandItem().isEmpty();

        if (isHoldingFist) {
            if (attackRange.getModifier(FIST_RANGE_ID) == null) {
                attackRange.addPermanentModifier(
                        new AttributeModifier(
                                FIST_RANGE_ID,
                                -0.6,
                                AttributeModifier.Operation.ADD_VALUE
                        )
                );
            }
        } else {
            attackRange.removeModifier(FIST_RANGE_ID);
        }
    }
}