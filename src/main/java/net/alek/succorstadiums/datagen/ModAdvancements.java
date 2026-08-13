package net.alek.succorstadiums.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

import net.alek.succorstadiums.advancement.PlayerDeathCriterion;
import net.alek.succorstadiums.advancement.ModCriteria;
import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.SuccorStadiums;

import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NonNull;
import java.util.function.Consumer;
import java.util.Optional;

public class ModAdvancements extends FabricAdvancementProvider {

    public ModAdvancements(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

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
                .addCriterion("root", InventoryChangeTrigger.TriggerInstance.hasItems((ItemPredicate[]) new ItemPredicate[0]))
                .save(consumer, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "root"));

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
    }
}
