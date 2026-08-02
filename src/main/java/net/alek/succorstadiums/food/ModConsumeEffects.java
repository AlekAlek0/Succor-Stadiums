package net.alek.succorstadiums.food;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.alek.succorstadiums.SuccorStadiums;

public class ModConsumeEffects {

    public static final ConsumeEffect.Type<HealConsumeEffect> HEAL_TYPE =
            new ConsumeEffect.Type<>(HealConsumeEffect.CODEC, HealConsumeEffect.STREAM_CODEC);

    public static void register() {
        Registry.register(
                BuiltInRegistries.CONSUME_EFFECT_TYPE,
                Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "heal"),
                HEAL_TYPE
        );
    }
}