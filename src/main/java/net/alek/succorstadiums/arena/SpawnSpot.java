package net.alek.succorstadiums.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;

// The rule for where arena mobs can stand. ArenaSession uses it to place mobs, and the client-side invalid spawn
// indicator (ArenaRadiusRenderer) uses the same code, so what you see is exactly what the spawner checks.
public final class SpawnSpot {

    private SpawnSpot() {}

    // The cell a mob first tries to stand in for this column: the first cell above the highest solid block,
    // ignoring leaves, clamped to the world height.
    public static int surfaceY(LevelReader level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return Math.clamp(y, level.getMinY(), level.getMaxY());
    }

    // A mob can stand at pos when the block below is a full solid block and the cell it stands in
    // and the cell above it are not full solid blocks.
    public static boolean isValidStandingSpot(BlockGetter level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockPos above = pos.above();
        boolean groundSolid = level.getBlockState(below).isCollisionShapeFullBlock(level, below);
        boolean feetClear = !level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
        boolean headClear = !level.getBlockState(above).isCollisionShapeFullBlock(level, above);
        return groundSolid && feetClear && headClear;
    }
}