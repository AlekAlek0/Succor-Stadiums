package net.alek.succorstadiums.effect;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class ParalysisEffect extends MobEffect {

    private static final Identifier MOVEMENT_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "paralysis_movement_speed");
    private static final Identifier JUMP_STRENGTH_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "paralysis_jump_strength");

    public ParalysisEffect() {
        super(MobEffectCategory.HARMFUL, 0x707070);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER_ID, -1.0, Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, JUMP_STRENGTH_MODIFIER_ID, -1.0, Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(final int duration, final int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(final @NonNull ServerLevel level, final @NonNull LivingEntity entity, final int amplifier) {
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setJumping(false);
        entity.setSprinting(false);
        return super.applyEffectTick(level, entity, amplifier);
    }
}