package net.alek.succorstadiums.item.weapons;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import java.util.List;

public class BownanaItem extends BowItem {
    public BownanaItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40; // Short draw, fully charged at 20 ticks (1 second)
    }

    @Override
    protected void shoot(ServerLevel level, LivingEntity shooter, InteractionHand hand,
                         ItemStack bow, List<ItemStack> arrows, float velocity,
                         float inaccuracy, boolean isCrit, LivingEntity target) {
        super.shoot(level, shooter, hand, bow, arrows,
                Math.min(velocity, 1.0F),
                inaccuracy, isCrit, target);
    }
}