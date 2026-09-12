package net.alek.succorstadiums.item.weapons.ranged;

import net.alek.succorstadiums.entity.projectile.BaleArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BaleArrowItem extends ArrowItem {
    public BaleArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull AbstractArrow createArrow(final @NonNull Level level, final ItemStack itemStack, final @NonNull LivingEntity owner, final @Nullable ItemStack firedFromWeapon) {
        assert firedFromWeapon != null;
        return new BaleArrowEntity(level, owner, itemStack.copyWithCount(1), firedFromWeapon);
    }
}