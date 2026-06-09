package net.alek.succorstadiums.mixin;

import net.alek.succorstadiums.item.armor.NannerWaterWadersItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class NannerWaterWadersMixin {

    @Unique
    private boolean isWearingWaders(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof NannerWaterWadersItem;
    }

    @Unique
    private Block getGroundBlock(Player player) {
        BlockPos pos = player.blockPosition();
        BlockPos below = pos.below();

        Block feetBlock = player.level().getBlockState(pos).getBlock();
        Block belowBlock = player.level().getBlockState(below).getBlock();

        if (feetBlock == Blocks.SOUL_SAND || feetBlock == Blocks.SOUL_SOIL || feetBlock == Blocks.MUD
                || feetBlock == Blocks.ICE || feetBlock == Blocks.PACKED_ICE || feetBlock == Blocks.BLUE_ICE) {
            return feetBlock;
        }

        return belowBlock;
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void onTravel(Vec3 input, CallbackInfo ci) {
        Player self = (Player)(Object)this;

        if (!isWearingWaders(self)) return;

        if (self.isUnderWater()) {
            Vec3 vel = self.getDeltaMovement();
            self.setDeltaMovement(vel.x * 1.05, vel.y, vel.z * 1.05);
            return;
        }

        // If self is not on the ground do nothing
        if (!self.onGround()) return;

        Block block = getGroundBlock(self);
        Vec3 vel = self.getDeltaMovement();

        if (block == Blocks.SOUL_SAND) {
            self.setDeltaMovement(vel.x * 3.5, vel.y, vel.z * 3.5);
        }
        else if (block == Blocks.SOUL_SOIL || block == Blocks.MUD) {
            self.setDeltaMovement(vel.x * 1.5, vel.y, vel.z * 1.5);
        }
    }
}