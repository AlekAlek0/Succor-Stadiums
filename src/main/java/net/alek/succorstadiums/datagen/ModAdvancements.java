package net.alek.succorstadiums.datagen;

import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;

import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NonNull;
import java.util.function.Consumer;
import java.util.Optional;

import net.alek.succorstadiums.advancement.criterion.BackpackOpenCriterion;
import net.alek.succorstadiums.advancement.criterion.PlayerDeathCriterion;
import net.alek.succorstadiums.advancement.ModCriteria;
import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.SuccorStadiums;

// ModAdvancements class
public class ModAdvancements extends FabricAdvancementProvider {

    // Public constructor
    public ModAdvancements(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    // Override method to generate advancements
    @Override
    public void generateAdvancement(HolderLookup.@NonNull Provider wrapperLookup, @NonNull Consumer<AdvancementHolder> consumer) {

        // Root advancement
        AdvancementHolder ROOT = Advancement.Builder.advancement()
                .display(
                        ModItems.BRENNON_ORE,
                        Component.literal("Succor Stadiums"),
                        Component.literal(""),
                        Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false
                )
                .addCriterion("root", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemPredicate[0]))
                .save(consumer, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "root"));

        // Create vincible advancement
        AdvancementHolder VINCIBLE = Advancement.Builder.advancement()
                .parent(ROOT)
                .display(
                        Items.TOTEM_OF_UNDYING,
                        Component.literal("Vincible"),
                        Component.literal("Die for the first time"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("player_death", ModCriteria.PLAYER_DEATH.createCriterion(new PlayerDeathCriterion.Conditions(Optional.empty())))
                .save(consumer, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "vincible"));

        // Create baby's first arena advancement
        AdvancementHolder BABYS_FIRST_ARENA = Advancement.Builder.advancement()
                .parent(ROOT)
                .display(
                        Items.IRON_SWORD,
                        Component.literal("Baby's First Arena"),
                        Component.literal("Complete the first arena"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                .save(consumer, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "babys_first_arena"));

        // Create off to the big city advancement
        AdvancementHolder OFF_TO_THE_BIG_CITY = Advancement.Builder.advancement()
                .parent(ROOT)
                .display(
                        Items.OAK_SAPLING,
                        Component.literal("Off to the Big City"),
                        Component.literal("Complete the plains arena"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                .save(consumer, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "off_to_the_big_city"));

        // Create pocket dimension advancement
        AdvancementHolder POCKET_DIMENSION = Advancement.Builder.advancement()
                .parent(ROOT)
                .display(
                        Items.ENDER_CHEST,
                        Component.literal("Pocket Dimension"),
                        Component.literal("Open your backpack for the first time"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("backpack_open", ModCriteria.BACKPACK_OPEN.createCriterion(new BackpackOpenCriterion.Conditions(Optional.empty())))
                .save(consumer, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "pocket_dimension"));
    }
}
