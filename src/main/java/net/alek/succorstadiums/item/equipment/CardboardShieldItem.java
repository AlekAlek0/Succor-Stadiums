package net.alek.succorstadiums.item.equipment;

import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

// CardboardShieldItem class
public class CardboardShieldItem extends ShieldItem {
    public CardboardShieldItem(Item.Properties properties) {
        super(configure(properties));
    }

    private static Item.Properties configure(Item.Properties properties) {
        return properties.durability(64)
                .component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
                .equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(
                        0.25F, 1.0F,
                        List.of(new BlocksAttacks.DamageReduction(
                                90.0F,
                                Optional.of(context.getOrThrow(DamageTypeTags.IS_PROJECTILE)),
                                0.0F, 1.0F)),
                        new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                        Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                        Optional.of(SoundEvents.SHIELD_BLOCK),
                        Optional.of(SoundEvents.SHIELD_BREAK)))
                .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK);
    }
}