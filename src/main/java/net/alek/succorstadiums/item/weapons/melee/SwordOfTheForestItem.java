package net.alek.succorstadiums.item.weapons.melee;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class SwordOfTheForestItem extends Item {
    public SwordOfTheForestItem(Properties properties) {
        super(properties.delayedComponent(DataComponents.ENCHANTMENTS, (context) -> {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(context.getOrThrow(Enchantments.SWEEPING_EDGE), 1);
            return mutable.toImmutable();
        }));
    }
}