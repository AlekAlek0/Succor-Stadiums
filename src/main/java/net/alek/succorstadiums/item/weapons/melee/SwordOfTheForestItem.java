package net.alek.succorstadiums.item.weapons.melee;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class SwordOfTheForestItem extends Item {
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
}