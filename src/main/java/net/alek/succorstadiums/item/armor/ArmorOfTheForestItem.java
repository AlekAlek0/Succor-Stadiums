package net.alek.succorstadiums.item.armor;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ArmorOfTheForestItem extends Item {

    private static final Identifier ARMOR_OF_THE_FOREST_KNOCKBACK_RESISTANCE_ID =
            Identifier.withDefaultNamespace("armor_of_the_forest_knockback_resistance");

    private static final Identifier ARMOR_OF_THE_FOREST_ATTACK_DAMAGE_ID =
            Identifier.withDefaultNamespace("armor_of_the_forest_attack_damage");

    private static final Identifier ARMOR_OF_THE_FOREST_ATTACK_SPEED_ID =
            Identifier.withDefaultNamespace("armor_of_the_forest_attack_speed");

    public ArmorOfTheForestItem(Item.Properties properties, ArmorType armorType) {
        super(properties.component(
                DataComponents.ATTRIBUTE_MODIFIERS,

                ItemAttributeModifiers.builder()

                        .add(
                                Attributes.ARMOR,
                                new AttributeModifier(
                                        Identifier.withDefaultNamespace(
                                                "forest_armor_" + armorType.getName()
                                        ),
                                        getArmorPoints(armorType),
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                getSlotGroup(armorType)
                        )

                        .build()
        ));
    }

    @Override
    public void inventoryTick(@NonNull ItemStack itemStack, @NonNull ServerLevel level, @NonNull Entity owner, @Nullable EquipmentSlot slot) {
        if (!(owner instanceof Player player) || slot != EquipmentSlot.CHEST) {
            return;
        }

        boolean fullSet = isWearingFullForestSet(player);

        applyOrRemove(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE),
                ARMOR_OF_THE_FOREST_KNOCKBACK_RESISTANCE_ID, -2.0, fullSet);

        applyOrRemove(player.getAttribute(Attributes.ATTACK_DAMAGE),
                ARMOR_OF_THE_FOREST_ATTACK_DAMAGE_ID, 1.0, fullSet);

        applyOrRemove(player.getAttribute(Attributes.ATTACK_SPEED),
                ARMOR_OF_THE_FOREST_ATTACK_SPEED_ID, 0.6, fullSet);
    }

    private static void applyOrRemove(AttributeInstance instance, Identifier id, double amount, boolean shouldHave) {
        if (instance == null) return;

        boolean has = instance.getModifier(id) != null;

        if (shouldHave && !has) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        } else if (!shouldHave && has) {
            instance.removeModifier(id);
        }
    }

    public static boolean isWearingFullForestSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArmorOfTheForestItem
                && player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorOfTheForestItem
                && player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmorOfTheForestItem
                && player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ArmorOfTheForestItem;
    }

    private static double getArmorPoints(ArmorType armorType) {
        return switch (armorType) {
            case HELMET -> 1.5;
            case CHESTPLATE -> 2.5;
            case LEGGINGS -> 2;
            case BOOTS -> 1;
            default -> 0;
        };
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