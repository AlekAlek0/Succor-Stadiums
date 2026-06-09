package net.alek.succorstadiums.item.interfaces;

import net.minecraft.world.item.ItemStack;

public interface FreeRepair {
    default boolean isFreeRepair(ItemStack input, ItemStack material) {
        return input.getItem() == material.getItem();
    }
}