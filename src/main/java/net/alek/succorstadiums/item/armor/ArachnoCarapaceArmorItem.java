package net.alek.succorstadiums.item.armor;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class ArachnoCarapaceArmorItem extends Item {

    private static final Identifier KNOCKBACK_RESISTANCE_ID =
            Identifier.withDefaultNamespace("arachno_knockback_resistance");

    private static final Identifier SAFE_FALL_DISTANCE_ID =
            Identifier.withDefaultNamespace("arachno_safe_fall_distance");

    private static final Identifier STEP_HEIGHT_ID =
            Identifier.withDefaultNamespace("arachno_step_height");

    public ArachnoCarapaceArmorItem(Item.Properties properties, ArmorType armorType) {
        super(properties.component(
                DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                        .add(
                                Attributes.KNOCKBACK_RESISTANCE,
                                new AttributeModifier(
                                        KNOCKBACK_RESISTANCE_ID,
                                        getKnockbackResistance(armorType),
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                getSlotGroup(armorType)
                        )
                        .add(
                                Attributes.SAFE_FALL_DISTANCE,
                                new AttributeModifier(
                                        SAFE_FALL_DISTANCE_ID,
                                        1,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.ARMOR
                        )
                        .add(
                                Attributes.STEP_HEIGHT,
                                new AttributeModifier(
                                        STEP_HEIGHT_ID,
                                        0.9,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.ARMOR
                        )
                        .add(
                                Attributes.ARMOR,
                                new AttributeModifier(
                                        Identifier.withDefaultNamespace(
                                                "arachno_armor_" + armorType.getName()
                                        ),
                                        getArmorPoints(armorType),
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                getSlotGroup(armorType)
                        ).build()
        ));
    }

    // Helper method to get armor points required for each armor type
    private static double getArmorPoints(ArmorType armorType) {
        return switch (armorType) {
            case HELMET -> 2;
            case CHESTPLATE -> 3;
            case LEGGINGS -> 2;
            case BOOTS -> 1;
            default -> 0;
        };
    }

    // Helper method to get knockback resistance required for each armor type
    private static double getKnockbackResistance(ArmorType armorType) {
        return switch (armorType) {
            case HELMET -> 0.2;
            case CHESTPLATE -> 0.5;
            case LEGGINGS -> 0.4;
            case BOOTS -> 0.1;
            default -> 0;
        };
    }

    // Helper method to get the slot the armor is in
    private static EquipmentSlotGroup getSlotGroup(ArmorType armorType) {
        return switch (armorType) {
            case HELMET -> EquipmentSlotGroup.HEAD;
            case CHESTPLATE -> EquipmentSlotGroup.CHEST;
            case LEGGINGS -> EquipmentSlotGroup.LEGS;
            case BOOTS -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ARMOR;
        };
    }
}