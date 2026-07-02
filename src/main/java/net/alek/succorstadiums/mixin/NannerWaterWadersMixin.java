package net.alek.succorstadiums.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import net.alek.succorstadiums.item.armor.NannerWaterWadersItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class NannerWaterWadersMixin {

    // Helper method to check if player is wearing nanner water waders
    @Unique
    private boolean isWearingWaders(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof NannerWaterWadersItem;
    }

    // Helper method to get the block under the player
    @Unique
    private Block getGroundBlock(Player player) {

        // Get the pos of the players feet and the pos below them
        BlockPos pos = player.blockPosition();
        BlockPos below = pos.below();

        // Get the block under the player and the block below them
        Block feetBlock = player.level().getBlockState(pos).getBlock();
        Block belowBlock = player.level().getBlockState(below).getBlock();

        // If feet block is soul sand, soil, or mud then return that block
        if (feetBlock == Blocks.SOUL_SAND || feetBlock == Blocks.SOUL_SOIL || feetBlock == Blocks.MUD) {
            return feetBlock;
        }

        // If feet block doesn't return just return the below block
        return belowBlock;
    }

    // Inject custom logic for nanner water waders when player is traveling
    @Inject(method = "travel", at = @At("TAIL"))
    private void onTravel(Vec3 input, CallbackInfo ci) {
        Player self = (Player)(Object)this;

        // If player is not wearing nanner water waders do nothing
        if (!isWearingWaders(self)) return;

        // If player is underwater increase velocity by 5%
        if (self.isUnderWater()) {
            Vec3 vel = self.getDeltaMovement();
            self.setDeltaMovement(vel.x * 1.05, vel.y, vel.z * 1.05);
            return;
        }

        // If self is not on the ground do nothing
        if (!self.onGround()) return;

        // Get players current velocity and the block their standing on
        Block block = getGroundBlock(self);
        Vec3 vel = self.getDeltaMovement();

        // if Block is soul sand under them multiply velocity by 330%
        if (block == Blocks.SOUL_SAND) {
            self.setDeltaMovement(vel.x * 3.3, vel.y, vel.z * 3.3);
        }
        // if Block is soul soil or mud under them multiply velocity by 30%
        else if (block == Blocks.SOUL_SOIL || block == Blocks.MUD) {
            self.setDeltaMovement(vel.x * 1.3, vel.y, vel.z * 1.3);
        }
    }
}