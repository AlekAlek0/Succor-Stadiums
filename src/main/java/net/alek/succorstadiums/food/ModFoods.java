package net.alek.succorstadiums.food;

import net.alek.succorstadiums.effect.ModEffects;
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
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 600, 0), 0.1f))

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.REGENERATION, 300, 0), 0.75f))

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.SLOWNESS, 280, 3), 0.30f))

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.WEAKNESS, 240, 0), 0.65f))

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(ModEffects.PARALYSIS, 160, 0), 0.08f))
            .build();

    // Create a new food called creeper salve with the following nutrition and saturation
    public static final FoodProperties CREEPER_SALVE = new FoodProperties.Builder()
            .nutrition(0)
            .saturationModifier(0)
            .build();

    // Create the consumable for the creeper salve with the consume duration and heal consume effect
    public static final Consumable CREEPER_SALVE_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(0.4f)

            .onConsume(new HealConsumeEffect(3.0f))

            .build();

    // Create a new food called plant powder with the following nutrition and saturation and always edible
    public static final FoodProperties PLANT_POWDER = new FoodProperties.Builder()
            .nutrition(0)
            .saturationModifier(0)
            .alwaysEdible()
            .build();

    // Create the consumable for the plant_powder with the consume duration and explode consume effect
    public static final Consumable PLANT_POWDER_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(0.8f)

            .onConsume(new ExplodeConsumeEffect())

            .build();

    // Create a new food called beef stew with the following nutrition and saturation
    public static final FoodProperties BEEF_STEW = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(0.24f)
            .build();

    // Create the consumable for the beef stew with the consume duration
    public static final Consumable BEEF_STEW_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(2.2f)
            .build();

    // Create a new food called chicken stew with the following nutrition and saturation
    public static final FoodProperties CHICKEN_STEW = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.3f)
            .build();

    // Create the consumable for the chicken stew with the consume duration and status effect
    public static final Consumable CHICKEN_STEW_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(1.8f)

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.HUNGER, 400, 0), 0.1f))
            .build();

    // Create a new food called pork stew with the following nutrition and saturation
    public static final FoodProperties PORK_STEW = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.4f)
            .build();

    // Create the consumable for the pork stew with the consume duration
    public static final Consumable PORK_STEW_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(2.0f)
            .build();

    // Create a new food called mutton stew with the following nutrition and saturation
    public static final FoodProperties MUTTON_STEW = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.6f)
            .build();

    // Create the consumable for the mutton stew with the consume duration
    public static final Consumable MUTTON_STEW_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(1.6f)
            .build();

    // Create a new food called rabbit stew with the following nutrition and saturation
    public static final FoodProperties RABBIT_STEW = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.225f)
            .build();

    // Create the consumable for the rabbit stew with the consume duration
    public static final Consumable RABBIT_STEW_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(1.4f)
            .build();

    // Create a new food called rotten stew with the following nutrition and saturation
    public static final FoodProperties ROTTEN_STEW = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(5f / 6f)
            .build();

    // Create the consumable for the rotten stew with the consume duration and status effect
    public static final Consumable ROTTEN_STEW_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(2.2f)

            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.HUNGER, 400, 1), 0.3f))
            .build();
}