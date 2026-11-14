package net.alek.succorstadiums.item.summons;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.NonNull;

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

}