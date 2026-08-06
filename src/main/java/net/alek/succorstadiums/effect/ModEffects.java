package net.alek.succorstadiums.effect;

import net.alek.succorstadiums.SuccorStadiums;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;

public class ModEffects {

    public static final Holder<MobEffect> PARALYSIS = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "paralysis"),
            new ParalysisEffect()
    );

    public static void register() {
        SuccorStadiums.LOGGER.info("Registering Mod Effects for " + SuccorStadiums.MOD_ID);

        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (player.hasEffect(PARALYSIS)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.hasEffect(PARALYSIS)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }
}