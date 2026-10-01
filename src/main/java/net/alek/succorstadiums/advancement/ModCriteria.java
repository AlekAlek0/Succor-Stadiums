package net.alek.succorstadiums.advancement;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;

import net.alek.succorstadiums.advancement.criterion.summon.SummonItemUseCriterion;
import net.alek.succorstadiums.advancement.criterion.BackpackOpenCriterion;
import net.alek.succorstadiums.advancement.criterion.PlayerDeathCriterion;
import net.alek.succorstadiums.SuccorStadiums;

// ModCriteria class
public class ModCriteria {
    public static final PlayerDeathCriterion PLAYER_DEATH = register("player_death", new PlayerDeathCriterion());
    public static final BackpackOpenCriterion BACKPACK_OPEN = register("backpack_open", new BackpackOpenCriterion());
    public static final SummonItemUseCriterion SUMMON_ITEM_USE = register("summon_item_use", new SummonItemUseCriterion());

    private static <T extends CriterionTrigger<?>> T register(final String name, final T criterion) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name), criterion);
    }

    public static void registerModCriteria() {

        SuccorStadiums.LOGGER.info("Registering Mod Criteria for " + SuccorStadiums.MOD_ID);
    }
}