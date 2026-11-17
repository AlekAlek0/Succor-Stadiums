package net.alek.succorstadiums.item.summons;

import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.*;

import org.jspecify.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.alek.succorstadiums.item.weapons.magic.MagicIndicator;
import net.alek.succorstadiums.mana.ManaHelper;
import net.alek.succorstadiums.sound.ModSounds;

// SummonersCaneItem class
public class SummonersCaneItem extends Item implements MagicIndicator {
    public SummonersCaneItem(Properties properties) {
        super(properties.component(
                DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                        .add(
                                Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                        Item.BASE_ATTACK_DAMAGE_ID,
                                        1.0,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        )
                        .add(
                                Attributes.ATTACK_SPEED,
                                new AttributeModifier(
                                        Item.BASE_ATTACK_SPEED_ID,
                                        -2.8,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND

                        )
                        .add(
                                Attributes.ATTACK_KNOCKBACK,
                                new AttributeModifier(
                                        Identifier.withDefaultNamespace("summoners_cane_attack_knockback"),
                                        2.0,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND
                        ).build()
        ));
    }

    private static final int COOLDOWN_TICKS = 20 * 10; // 10 Seconds
    private static final int MANA_COST = 6; // 3 Stars
    private static final double AOE_RADIUS = 2.5;

    private static final Component NOT_ENOUGH_MANA_MESSAGE =
            Component.translatable("message.succorstadiums.not_enough_mana");

    private static final Component NO_TARGET_MESSAGE =
            Component.translatable("message.succorstadiums.no_target_in_view");

    private static final Component NO_SUMMONS_MESSAGE =
            Component.translatable("message.succorstadiums.no_summons");

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {

        // Check to see if level is client sided if so return a pass value for the interaction result
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        // Find the mob the player is looking at, if there isn't one send warn message to the player and spend no mana
        LivingEntity lookedAt = getLookedAtMob(serverLevel, player);
        if (lookedAt == null) {
            level.playSound(null, player, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.UI, 1.0f, 1.0f);
            player.sendOverlayMessage(NO_TARGET_MESSAGE);
            return InteractionResult.FAIL;
        }

        // Collect the players living summons
        List<Wolf> summons = new ArrayList<>();
        for (UUID uuid : DogWhistleItem.SUMMONED_WOLVES.keySet()) {
            if (serverLevel.getEntity(uuid) instanceof Wolf wolf && wolf.isAlive() && wolf.isOwnedBy(player)) {
                summons.add(wolf);
            }
        }

        // If there are no summons send warn message to the player and return a success value for the interaction result
        if (summons.isEmpty()) {
            player.sendOverlayMessage(NO_SUMMONS_MESSAGE);
            return InteractionResult.FAIL;
        }

        // Check if player has enough mana to cast cane
        if (!ManaHelper.consumeMana(player, MANA_COST)) {
            level.playSound(null, player, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.UI, 1.0f, 0.5f);
            player.sendOverlayMessage(NOT_ENOUGH_MANA_MESSAGE);
            return InteractionResult.FAIL;
        }

        // Make every summon target the mob
        for (Wolf wolf : summons) {
            wolf.setTarget(lookedAt);
        }

        // Set cooldown and return a success value for the interaction result
        player.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        level.playSound(null, player, ModSounds.FIRECHARGED_CANE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void postHurtEnemy(@NonNull ItemStack stack, @NonNull LivingEntity target, @NonNull LivingEntity attacker) {

        // If the attacker is not a player and players level is not of server level return
        if (!(attacker instanceof Player player) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Get the damage source and damage and knockback from the item's attribute modifiers
        DamageSource source = serverLevel.damageSources().playerAttack(player);
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double knockback = player.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        double radiusSqr = AOE_RADIUS * AOE_RADIUS;

        // Create a list of living entities called targets for each entity in the aoe radius
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(AOE_RADIUS),
                other -> other != player
                        && other != target
                        && other.isAlive()
                        && !other.isSpectator()
                        && other.distanceToSqr(player) <= radiusSqr
        );

        // For each living entity target in targets give them knockback
        for (LivingEntity other : targets) {
            if (other.hurtServer(serverLevel, source, damage)) {
                other.knockback(knockback * 0.5, player.getX() - other.getX(), player.getZ() - other.getZ(), source, damage);
            }
        }
    }

    // Private helper method that returns the living mob the player is looking at within their entity interaction range, or null
    private static LivingEntity getLookedAtMob(ServerLevel level, Player player) {
        double range = player.entityInteractionRange();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, eye, end, searchBox,
                entity -> entity instanceof LivingEntity
                        && entity.isAlive()
                        && !entity.isSpectator()
                        && entity != player
                        && !(entity instanceof Wolf wolf && wolf.isOwnedBy(player)),
                range * range
        );
        if (hit == null) {
            return null;
        }

        // Don't target through walls
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS
                && blockHit.getLocation().distanceToSqr(eye) < hit.getLocation().distanceToSqr(eye)) {
            return null;
        }

        return (LivingEntity) hit.getEntity();
    }
}