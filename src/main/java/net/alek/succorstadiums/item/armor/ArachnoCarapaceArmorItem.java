package net.alek.succorstadiums.item.armor;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
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

    private static final double SET_BONUS_AIR_DRAG = 0.87;

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

    // Call once from your mod initializer to enable the full set bonus
    public static void registerSetBonus() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                updateAirDragBonus(player);
            }
        });
    }

    private static void updateAirDragBonus(ServerPlayer player) {
        AttributeInstance airDrag = player.getAttribute(Attributes.AIR_DRAG_MODIFIER);
        if (airDrag == null) {
            return;
        }

        boolean hasBonus = Math.abs(airDrag.getBaseValue() - SET_BONUS_AIR_DRAG) < 1.0E-6;

        if (isWearingFullArachnoSet(player)) {
            if (!hasBonus) {
                airDrag.setBaseValue(SET_BONUS_AIR_DRAG);
            }
        } else if (hasBonus) {
            airDrag.setBaseValue(Attributes.AIR_DRAG_MODIFIER.value().getDefaultValue());
        }
    }

    // Accessor method to get if player is wearing full armor of the arachno set
    public static boolean isWearingFullArachnoSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArachnoCarapaceArmorItem
                && player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArachnoCarapaceArmorItem
                && player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArachnoCarapaceArmorItem
                && player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ArachnoCarapaceArmorItem;
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