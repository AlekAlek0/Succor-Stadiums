package net.alek.succorstadiums.item.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;

import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ArachnoCarapaceArmorItem extends Item {

    private static final Identifier STEP_HEIGHT_ID = Identifier.withDefaultNamespace("arachno_step_height");
    private static final Identifier JUMP_STRENGTH = Identifier.withDefaultNamespace("arachno_jump_strength");
    private static final Identifier MOVEMENT_SPEED_ID = Identifier.withDefaultNamespace("arachno_movement_speed");


    public ArachnoCarapaceArmorItem(Item.Properties properties) {
        super(properties.component(
                DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
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
                                Attributes.MOVEMENT_SPEED,
                                new AttributeModifier(
                                        MOVEMENT_SPEED_ID,
                                        -0.04,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.ARMOR
                        )
                        .add(
                                Attributes.JUMP_STRENGTH,
                                new AttributeModifier(
                                        JUMP_STRENGTH,
                                        -0.10,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.ARMOR

                        ).build()
        ));
    }
}
