package net.alek.succorstadiums.item.weapons.melee;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.NonNull;

public class SwordOfTheForestItem extends Item {
    public SwordOfTheForestItem(Properties properties) {
        super(properties.delayedComponent(DataComponents.ENCHANTMENTS, (context) -> {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(context.getOrThrow(Enchantments.SWEEPING_EDGE), 1);
            return mutable.toImmutable();
        }));
    }

    // Remove enchantment glint from item
    @Override
    public boolean isFoil(@NonNull ItemStack itemStack) {
        return false;
    }
}