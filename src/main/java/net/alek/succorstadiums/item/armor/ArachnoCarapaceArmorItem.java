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

    private static final Identifier SAFE_FALL_DISTANCE_ID =
            Identifier.withDefaultNamespace("arachno_safe_fall_distance");

    private static final Identifier KNOCKBACK_RESISTANCE_ID =
            Identifier.withDefaultNamespace("arachno_knockback_resistance");

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
                                        0.01,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.ARMOR
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
                                        1,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                getSlotGroup(armorType)
                        )

                        .build()
        ));
    }

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