package net.alek.succorstadiums.item.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;

import net.minecraft.world.item.component.ItemAttributeModifiers;

// Arachno carapace armor class
public class ArachnoCarapaceArmorItem extends Item {

    // Define the step height, jump strength, and movement ids
    private static final Identifier STEP_HEIGHT_ID = Identifier.withDefaultNamespace("arachno_step_height");
    private static final Identifier SAFE_FALL_DISTANCE_ID = Identifier.withDefaultNamespace("arachno_safe_fall_distance");


    public ArachnoCarapaceArmorItem(Item.Properties properties) {
        super(properties.component(
                DataComponents.ATTRIBUTE_MODIFIERS,

                // Modify the item attributes for step height by the added values given
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
                                Attributes.SAFE_FALL_DISTANCE,
                                new AttributeModifier(
                                        SAFE_FALL_DISTANCE_ID,
                                        1,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.ARMOR
                        ).build()
        ));
    }
}
