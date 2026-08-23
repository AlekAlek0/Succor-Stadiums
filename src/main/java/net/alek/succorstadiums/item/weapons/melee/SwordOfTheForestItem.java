package net.alek.succorstadiums.item.weapons.melee;

import net.alek.succorstadiums.item.armor.ArmorOfTheForestItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class SwordOfTheForestItem extends Item {

    private static final float POISON_PROC_CHANCE = 0.25F; // 25%
    private static final int POISON_DURATION_TICKS = 4 * 20; // 0:04
    private static final int POISON_AMPLIFIER = 0; // Poison I

    public SwordOfTheForestItem(Properties properties) {
        super(properties.component(
                DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                        .add(
                                Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                        Item.BASE_ATTACK_DAMAGE_ID,
                                        2.5,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        )
                        .add(
                                Attributes.ATTACK_SPEED,
                                new AttributeModifier(
                                        Item.BASE_ATTACK_SPEED_ID,
                                        -3.2,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        )
                        .add(
                                Attributes.SWEEPING_DAMAGE_RATIO,
                                new AttributeModifier(
                                        Identifier.withDefaultNamespace("sword_of_the_forest_sweeping_damage_ratio"),
                                        0.5,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        ).build())
        );
    }

    // Override hurt enemy method
    @Override
    public void hurtEnemy(@NonNull ItemStack stack, @NonNull LivingEntity target, @NonNull LivingEntity attacker) {

        // 25% chance to give poison if wearing full armor of the forest set on hit
        if (attacker instanceof Player player
                && ArmorOfTheForestItem.isWearingFullForestSet(player)
                && attacker.getRandom().nextFloat() < POISON_PROC_CHANCE) {

            target.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    POISON_DURATION_TICKS,
                    POISON_AMPLIFIER,
                    false,
                    true
            ));
        }

        super.hurtEnemy(stack, target, attacker);
    }
}