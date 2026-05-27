package net.alek.succorstadiums.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireStaffItem extends Item {

    private static final int COOLDOWN_TICKS = 100;

    public FireStaffItem(Properties properties) {
        super(properties);

    }


    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {

        // Ensure we don't spawn the fireball only on the client this is to prevent desync.
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        // Get the direction the player is looking as a vector.
        Vec3 lookVec = user.getLookAngle();

        // Spawn the dragon fireball slightly in front of the player to avoid self-collision.
        Vec3 spawnPos = user.getEyePosition().add(lookVec.scale(2));

        DragonFireball fireball = new DragonFireball(level, user, lookVec);
        fireball.setPos(spawnPos);
        level.addFreshEntity(fireball);

        // Give a cooldown on the item and return a success
        user.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }
}