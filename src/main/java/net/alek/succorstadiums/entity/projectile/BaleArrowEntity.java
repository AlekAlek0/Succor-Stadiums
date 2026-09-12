package net.alek.succorstadiums.entity.projectile;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.NullMarked;

import net.alek.succorstadiums.entity.ModEntityTypes;
import net.alek.succorstadiums.item.ModItems;

@NullMarked
public class BaleArrowEntity extends AbstractArrow {

    private static final double BALE_ARROW_KNOCKBACK_IMPULSE = 0.3D;

    // Spawn constructor used when the entity type is created generically like with summon command
    public BaleArrowEntity(EntityType<? extends BaleArrowEntity> type, Level level) {
        super(type, level);
    }

    // Constructor used when fired from a bow or crossbow by a living entity shooter
    public BaleArrowEntity(Level level, LivingEntity shooter, ItemStack pickupStack, ItemStack firedFromWeapon) {
        super(ModEntityTypes.BALE_ARROW, shooter, level, pickupStack, firedFromWeapon);
    }

    // Constructor used for dispenser firing
    public BaleArrowEntity(Level level, double x, double y, double z, ItemStack pickupStack, ItemStack firedFromWeapon) {
        super(ModEntityTypes.BALE_ARROW, x, y, z, level, pickupStack, firedFromWeapon);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Vec3 arrowMovement = this.getDeltaMovement();

        super.onHitEntity(result);

        if (!this.level().isClientSide()) {
            if (result.getEntity() instanceof LivingEntity livingTarget) {
                double resistance = Math.max(0.0D, 1.0D - livingTarget.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                Vec3 horizontalDirection = arrowMovement.multiply(1.0D, 0.0D, 1.0D);

                if (horizontalDirection.lengthSqr() > 1.0E-7D && resistance > 0.0D) {
                    Vec3 push = horizontalDirection.normalize().scale(BALE_ARROW_KNOCKBACK_IMPULSE * resistance);
                    livingTarget.push(push.x, 0.1D, push.z);
                    livingTarget.hurtMarked = true;
                }
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ModItems.BALE_ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.BALE_ARROW);
    }
}