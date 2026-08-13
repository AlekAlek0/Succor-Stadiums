package net.alek.succorstadiums.advancement;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.alek.succorstadiums.SuccorStadiums;

public class ModCriteria {
    public static final PlayerDeathCriterion PLAYER_DEATH = register("player_death", new PlayerDeathCriterion());

    private static <T extends CriterionTrigger<?>> T register(final String name, final T criterion) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name), criterion);
    }

    public static void registerModCriteria() {

        SuccorStadiums.LOGGER.info("Registering Mod Criteria for " + SuccorStadiums.MOD_ID);
    }
}