package net.alek.succorstadiums.item.trinkets;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.LinkedHashSet;
import java.util.Set;

public class FlintCharmItem extends Item {
    private static final Identifier ATTACK_DAMAGE_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("succorstadiums", "flint_charm_attack_damage");

    public FlintCharmItem(Properties properties) {
        super(properties
                .component(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        ItemAttributeModifiers.builder()
                                .add(
                                        Attributes.ATTACK_DAMAGE,
                                        new AttributeModifier(
                                                ATTACK_DAMAGE_MODIFIER_ID,
                                                1,
                                                AttributeModifier.Operation.ADD_VALUE
                                        ),
                                        EquipmentSlotGroup.OFFHAND
                                )
                                .build()
                )

                // Suppress the default tooltip created
                .component(
                        DataComponents.TOOLTIP_DISPLAY,
                        new TooltipDisplay(false, new LinkedHashSet<>(Set.of(DataComponents.ATTRIBUTE_MODIFIERS)))
                )
        );
    }
}