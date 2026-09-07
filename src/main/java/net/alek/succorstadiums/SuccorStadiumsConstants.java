package net.alek.succorstadiums;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.Identifier;

import java.util.Map;

// SuccorStadiumsConstants class
public class SuccorStadiumsConstants {

    // Initialize maps for mob health and mob damages overrides
    public static final Map<EntityType<?>, Double> MOB_HEALTH_OVERRIDES;
    public static final Map<EntityType<?>, Double> MOB_DAMAGE_OVERRIDES;

    // Initialize double for player health and first range modifier
    public static final double PLAYER_MAX_HEALTH = 10.0;
    public static final double PLAYER_FIST_RANGE_MODIFIER = -0.6;

    // Initialize a identifier namespace id for player fist range
    public static final Identifier PLAYER_FIST_RANGE_ID = Identifier.withDefaultNamespace("player_fist_range");

    // Static initialize block for the two maps
    static {
        MOB_HEALTH_OVERRIDES = Map.of(
                EntityTypes.SKELETON, 10.0,
                EntityTypes.CREEPER, 8.0,
                EntityTypes.ZOMBIE, 14.0,
                EntityTypes.ZOMBIE_VILLAGER, 25.5);

        MOB_DAMAGE_OVERRIDES = Map.of(
                EntityTypes.ZOMBIE, 2.0,
                EntityTypes.ZOMBIE_VILLAGER, 2.0,
                EntityTypes.SPIDER, 0.5,
                EntityTypes.SKELETON, 0.5);
    }
}
