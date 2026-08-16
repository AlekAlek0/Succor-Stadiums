package net.alek.succorstadiums.item.weapons.melee;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import net.alek.succorstadiums.effect.ModEffects;

import org.jspecify.annotations.NonNull;

public class SproutSickleItem extends Item {
    public SproutSickleItem(Properties properties) {
        super(properties.component(
                DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                        .add(
                                Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                        Item.BASE_ATTACK_DAMAGE_ID,
                                        1.5,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        )
                        .add(
                                Attributes.ATTACK_SPEED,
                                new AttributeModifier(
                                        Item.BASE_ATTACK_SPEED_ID,
                                        -2.4,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        )
                        .add(
                                Attributes.ATTACK_KNOCKBACK,
                                new AttributeModifier(
                                        Identifier.withDefaultNamespace("sprout_sickle_attack_knockback"),
                                        0.34,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        )
                        .add(
                                Attributes.SWEEPING_DAMAGE_RATIO,
                                new AttributeModifier(
                                        Identifier.withDefaultNamespace("sprout_sickle_sweeping_damage_ratio"),
                                        0.75,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        )
                        .add(
                                Attributes.ENTITY_INTERACTION_RANGE,
                                new AttributeModifier(
                                        Identifier.withDefaultNamespace("sprout_sickle_entity_interaction_range"),
                                        -0.125,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        ).build())
        );
    }

    @Override
    public void hurtEnemy(@NonNull ItemStack itemStack, @NonNull LivingEntity mob, LivingEntity attacker) {
        if (attacker.getRandom().nextFloat() < 0.08F) {
            mob.addEffect(new MobEffectInstance(ModEffects.PARALYSIS, 60, 0));
        }
    }
}
