package net.alek.succorstadiums;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SuccorStadiumsConstants {

    public static final Map<EntityType<?>, Double> MOB_HEALTH_OVERRIDES;
    public static final Map<EntityType<?>, Double> MOB_DAMAGE_OVERRIDES;

    public static final double PLAYER_MAX_HEALTH = 6.0;

    public static final double PLAYER_FIST_RANGE_MODIFIER = -0.6;
    public static final Identifier PLAYER_FIST_RANGE_ID = Identifier.withDefaultNamespace("player_fist_range");

    static {
        Map<EntityType<?>, Double> healthMap = new HashMap<>();
        healthMap.put(EntityTypes.SKELETON, 10.0);
        healthMap.put(EntityTypes.CREEPER, 8.0);
        healthMap.put(EntityTypes.ZOMBIE, 14.0);
        healthMap.put(EntityTypes.ZOMBIE_VILLAGER, 25.5);
        MOB_HEALTH_OVERRIDES = Collections.unmodifiableMap(healthMap);

        Map<EntityType<?>, Double> damageMap = new HashMap<>();
        damageMap.put(EntityTypes.ZOMBIE, 1.0);
        damageMap.put(EntityTypes.ZOMBIE_VILLAGER, 2.0);
        damageMap.put(EntityTypes.SPIDER, 0.5);
        damageMap.put(EntityTypes.SKELETON, 0.5);
        MOB_DAMAGE_OVERRIDES = Collections.unmodifiableMap(damageMap);
    }
}
