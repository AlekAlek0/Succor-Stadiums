package net.alek.succorstadiums.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

// Mod foods class
public class ModFoods {

    // Create a new food called ghramble bapple with the following nutrition and saturation
    public static final FoodProperties GHRAMBLE_BAPPLE = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.3f)
            .build();

    // Create the consumable for the ghramble bapple with the consume duration and status effects
    public static final Consumable GHRAMBLE_BAPPLE_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(1.6f)

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.RESISTANCE, 900, 1), 0.3f))

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.REGENERATION, 300, 0), 0.85f))

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 300, 0), 0.05f))

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.SLOWNESS, 200, 3), 0.35f))

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.WEAKNESS, 160, 0), 0.75f))
            .build();

    // Create a new food called rotten stew with the following nutrition and saturation
    public static final FoodProperties ROTTEN_STEW = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(1.2f)
            .build();

    // Create the consumable for the rotten stew with the consume duration and status effect
    public static final Consumable ROTTEN_STEW_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(2.2f)

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.HUNGER, 400, 1), 0.3f))
            .build();

}
