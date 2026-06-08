package net.alek.succorstadiums.mixin;

import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.network.ResurrectionAmuletPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ResurrectionAmuletMixin {

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

            totem.shrink(1);
        }
    }
}