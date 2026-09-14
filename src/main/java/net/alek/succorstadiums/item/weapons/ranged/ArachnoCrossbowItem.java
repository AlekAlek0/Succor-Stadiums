package net.alek.succorstadiums.item.weapons.ranged;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.NonNull;

// ArachnoCrossbowItem class
public class ArachnoCrossbowItem extends CrossbowItem {
    public ArachnoCrossbowItem(Properties properties) {
        super(properties.delayedComponent(DataComponents.ENCHANTMENTS, (context) -> {

            // Enchant item with piercing 1
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(context.getOrThrow(Enchantments.PIERCING), 1);
            return mutable.toImmutable();
        }));
    }

    // Remove enchantment glint from item
    @Override
    public boolean isFoil(@NonNull ItemStack itemStack) {
        return false;
    }
}
