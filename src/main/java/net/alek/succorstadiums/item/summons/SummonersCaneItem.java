package net.alek.succorstadiums.item.summons;

import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.NonNull;
import java.util.List;

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

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {

        // Check to see if level is client sided if so return a pass value for the interaction result
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        // Check if player has enough mana to cast cane
        if (!ManaHelper.consumeMana(player, MANA_COST)) {
            player.sendOverlayMessage(NOT_ENOUGH_MANA_MESSAGE);
            return InteractionResult.FAIL;
        }

        // Set cooldown and return a success value for the interaction result
        player.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FIRECHARGED_CANE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
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

}