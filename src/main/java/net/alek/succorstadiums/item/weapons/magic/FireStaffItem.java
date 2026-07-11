package net.alek.succorstadiums.item.weapons.magic;

import net.alek.succorstadiums.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireStaffItem extends Item {

    private static final int COOLDOWN_TICKS = 100;

    public FireStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {

        // Check to see if level is client sided if so return a pass value for the interaction result
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        // Get user look vector and spawn position for fireball
        Vec3 lookVec = user.getLookAngle();
        Vec3 spawnPos = user.getEyePosition().add(lookVec.scale(2));

        LargeFireball fireball = new LargeFireball(level, user, lookVec, 0);
        fireball.setPos(spawnPos);
        level.addFreshEntity(fireball);

        // Get the item in user hand as itemStack
        ItemStack itemStack = user.getItemInHand(hand);

        // Damage the item by 1 durability if damageable
        if (itemStack.isDamageableItem()) {
            itemStack.hurtAndBreak(1, user, hand);
        }

        // Set Cooldown, play a sound effect and return a success value for the interaction result
        user.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);

        level.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.FIRE_STAFF_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }
}