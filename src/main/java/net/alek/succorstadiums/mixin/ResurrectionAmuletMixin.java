package net.alek.succorstadiums.mixin;

import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.network.ResurrectionAmuletPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ResurrectionAmuletMixin {

    @Shadow
    public abstract ServerLevel level();

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        ItemStack mainhand = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();

        ItemStack totem = null;
        if (mainhand.is(ModItems.RESURRECTION_AMULET)) totem = mainhand;
        else if (offhand.is(ModItems.RESURRECTION_AMULET)) totem = offhand;

        if (totem != null) {
            ci.cancel();

            player.setHealth(player.getMaxHealth());
            player.removeAllEffects();

            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1));
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0));

            // Send packet to client to show animation with correct item
            ServerPlayNetworking.send(player, new ResurrectionAmuletPayload(player.getId()));

            // Play sound effects and spawn particles
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, net.minecraft.sounds.SoundSource.PLAYERS, 500.0F, 0.8f);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE, net.minecraft.sounds.SoundSource.PLAYERS, 2000.0F, 1.0f);



            // Delete totem
            totem.shrink(1);
        }



    }
}