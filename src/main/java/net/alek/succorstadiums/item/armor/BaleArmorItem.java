package net.alek.succorstadiums.item.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.ArmorType;

public class BaleArmorItem extends Item {
    public BaleArmorItem(ArmorMaterial material, ArmorType type, Properties properties) {
        super(applyBaleEnchants(type, properties.humanoidArmor(material, type)));
    }

    private static Properties applyBaleEnchants(ArmorType type, Properties properties) {
        return properties.delayedComponent(DataComponents.ENCHANTMENTS, (context) -> {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

            switch (type) {
                case HELMET, CHESTPLATE -> mutable.set(context.getOrThrow(Enchantments.PROJECTILE_PROTECTION), 2);
                case LEGGINGS -> {
                    mutable.set(context.getOrThrow(Enchantments.SWIFT_SNEAK), 1);
                    mutable.set(context.getOrThrow(Enchantments.PROJECTILE_PROTECTION), 1);
                }
                case BOOTS -> {
                    mutable.set(context.getOrThrow(Enchantments.FEATHER_FALLING), 2);
                    mutable.set(context.getOrThrow(Enchantments.PROJECTILE_PROTECTION), 1);
                }
                default -> {}
            }

            return mutable.toImmutable();
        });
    }
}