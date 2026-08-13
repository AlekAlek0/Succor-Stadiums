package net.alek.succorstadiums.datagen;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.ApplyMobEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModEnchantmentGenerator extends FabricDynamicRegistryProvider {

    public ModEnchantmentGenerator(FabricPackOutput output,
                                   CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.ENCHANTMENT));
    }

    @Override
    public String getName() {
        return "Enchantments";
    }

    public static void bootstrap(BootstrapContext<Enchantment> context) {

        HolderGetter<Item> itemLookup = context.lookup(Registries.ITEM);

        register(context, ModEnchantments.VIPERS_BITE,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemLookup.getOrThrow(ModItemTagProvider.BONE_DAGGERS),
                                5, // Weight
                                5, // Max Level
                                Enchantment.dynamicCost(1, 2), // Min cost configuration
                                Enchantment.dynamicCost(1, 2), // Max cost configuration
                                0, // Anvil cost
                                EquipmentSlotGroup.HAND
                        )
                ).withEffect(
                        EnchantmentEffectComponents.POST_ATTACK,
                        EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM,
                        new ApplyMobEffect(
                                HolderSet.direct(MobEffects.POISON),

                                // Duration: L1=2s, L2=3s, L3=5s, L4=1s, L5=2s
                                LevelBasedValue.lookup(
                                        List.of(2F, 3F, 5F, 1F, 2F),
                                        LevelBasedValue.constant(2) // Fallback
                                ),

                                // Amplifier: L1=Poison I, L2=Poison I, L3=Poison I, L4=Poison II, L5=Poison II
                                LevelBasedValue.lookup(
                                        List.of(0F, 0F, 0F, 1F, 1F),
                                        LevelBasedValue.constant(0) // Fallback
                                ),

                                // Delay
                                LevelBasedValue.lookup(
                                        List.of(0F, 0F, 0F, 0F, 0F),
                                        LevelBasedValue.constant(0) // Fallback
                                ),

                                // Extra duration/randomness
                                LevelBasedValue.lookup(
                                        List.of(0F, 0F, 0F, 0F, 0F),
                                        LevelBasedValue.constant(0) // Fallback
                                )
                        )
                )
        );

        register(context, ModEnchantments.ROSE_THORN,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemLookup.getOrThrow(ModItemTagProvider.BONE_DAGGERS),
                                5, // Weight
                                3, // Max Level
                                Enchantment.dynamicCost(1, 2), // Min cost configuration
                                Enchantment.dynamicCost(1, 2), // Max cost configuration
                                0, // Anvil cost
                                EquipmentSlotGroup.HAND
                        )
                ).withEffect(
                        EnchantmentEffectComponents.POST_ATTACK,
                        EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM,
                        new ApplyMobEffect(
                                HolderSet.direct(MobEffects.REGENERATION),

                                // Duration: L1 = 2s, L2 = 3s, L3 = 1s
                                LevelBasedValue.lookup(
                                        List.of(2F, 3F, 1F),
                                        LevelBasedValue.constant(2) // Fallback
                                ),

                                // Amplifier: L1 = Regen I, L2 = Regen I, L3 = Regen II
                                LevelBasedValue.lookup(
                                        List.of(0F, 0F, 1F),
                                        LevelBasedValue.constant(0) // Fallback
                                ),

                                // Delay
                                LevelBasedValue.lookup(
                                        List.of(0F, 0F, 0F),
                                        LevelBasedValue.constant(0) // Fallback
                                ),

                                // Extra duration/randomness
                                LevelBasedValue.lookup(
                                        List.of(0F, 0F, 0F),
                                        LevelBasedValue.constant(0) // Fallback
                                )
                        )
                )
        );

        register(context, ModEnchantments.SWIFTNESS,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemLookup.getOrThrow(ModItemTagProvider.BONE_DAGGERS),
                                5,  // Weight
                                3,  // Max Level
                                Enchantment.dynamicCost(1, 2),
                                Enchantment.dynamicCost(1, 2),
                                0,  // Anvil cost
                                EquipmentSlotGroup.HAND
                        )
                ).withEffect(
                        EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "swiftness_speed"),
                                Attributes.MOVEMENT_SPEED,
                                LevelBasedValue.perLevel(0.15F, 0.10F), // L1: 0.15, L2: 0.25, L3: 0.35
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        )
                )
        );
    }

    private static void register(BootstrapContext<Enchantment> context,
                                 ResourceKey<Enchantment> key,
                                 Enchantment.Builder builder) {
        context.register(key, builder.build(key.identifier()));
    }
}