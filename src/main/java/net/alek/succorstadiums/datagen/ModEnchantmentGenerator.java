package net.alek.succorstadiums.datagen;

import net.alek.succorstadiums.enchantment.ModEnchantments;
import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.ApplyMobEffect;

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
                                4, // Anvil cost
                                EquipmentSlotGroup.HAND
                        )
                ).withEffect(
                        EnchantmentEffectComponents.POST_ATTACK,
                        EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM,
                        new ApplyMobEffect(
                                HolderSet.direct(MobEffects.POISON),
                                LevelBasedValue.lookup(
                                        List.of(2F, 3F, 5F, 1F, 2F),
                                        LevelBasedValue.constant(2)
                                ),
                                LevelBasedValue.lookup(
                                        List.of(2F, 3F, 5F, 1F, 2F),
                                        LevelBasedValue.constant(2)
                                ),
                                LevelBasedValue.lookup(
                                        List.of(0F, 0F, 0F, 1F, 1F),
                                        LevelBasedValue.constant(0)
                                ),
                                LevelBasedValue.lookup(
                                        List.of(0F, 0F, 0F, 1F, 1F),
                                        LevelBasedValue.constant(0)
                                )
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