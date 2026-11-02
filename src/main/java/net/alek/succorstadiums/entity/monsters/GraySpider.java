package net.alek.succorstadiums.entity.monsters;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// GraySpider class
public class GraySpider extends Spider {

    // Public constructor for gray spider entity
    public GraySpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }

    // Override the vanilla finalize spawn method for vanilla spiders to not have the chance of gray spider jockey's and spiders with mob effects
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(final @NonNull ServerLevelAccessor level, final @NonNull DifficultyInstance difficulty, final @NonNull EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        return groupData;
    }
}
