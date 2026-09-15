package net.alek.succorstadiums.item.weapons.magic;

import net.alek.succorstadiums.mana.ManaHelper;
import net.alek.succorstadiums.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class FirechargedCaneItem extends Item implements MagicIndicator {

    private static final int COOLDOWN_TICKS = 30; // 1.5 seconds
    private static final int MANA_COST = 4; // 2 Stars

    private static final Component NOT_ENOUGH_MANA_MESSAGE =
            Component.translatable("message.succorstadiums.not_enough_mana");

    public FirechargedCaneItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(Level level, @NonNull Player player, @NonNull InteractionHand hand) {

        // Check to see if level is client sided if so return a pass value for the interaction result
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        // Check if player has enough mana to cast staff
        if (ManaHelper.consumeMana(player, MANA_COST)) {
            player.sendOverlayMessage(NOT_ENOUGH_MANA_MESSAGE);
            return InteractionResult.FAIL;
        }

        ItemStack itemStack = player.getItemInHand(hand);

        // Get player look vector and spawn position for fireball
        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(lookVec.scale(2));

        LargeFireball fireball = new LargeFireball(level, player, lookVec, 0);
        fireball.setPos(spawnPos);
        level.addFreshEntity(fireball);

        // Damage the item by 1 durability if damageable
        if (itemStack.isDamageableItem()) {
            itemStack.hurtAndBreak(1, player, hand);
        }

        // Set Cooldown, play a sound effect and return a success value for the interaction result
        player.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FIRECHARGED_CANE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }
}