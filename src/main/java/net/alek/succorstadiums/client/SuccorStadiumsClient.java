package net.alek.succorstadiums.client;

import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.api.ClientModInitializer;

import static net.alek.succorstadiums.item.armor.ArachnoCarapaceArmorItem.isWearingFullArachnoSet;
import net.alek.succorstadiums.client.render.item.CardboardShieldSpecialRenderer;
import net.alek.succorstadiums.network.item.trinkets.ResurrectionAmuletPayload;
import net.alek.succorstadiums.client.render.item.CardboardShieldLayers;
import net.alek.succorstadiums.client.render.arena.ArenaRadiusRenderer;
import net.alek.succorstadiums.network.arena.AdvancementListPayload;
import net.alek.succorstadiums.client.particle.ForestAuraParticle;
import net.alek.succorstadiums.network.arena.OpenMobArenaPayload;
import net.alek.succorstadiums.network.arena.AdvancementIdCache;
import net.alek.succorstadiums.client.render.entity.monsters.*;
import net.alek.succorstadiums.client.render.entity.items.*;
import net.alek.succorstadiums.client.tooltip.ItemLore;
import net.alek.succorstadiums.particle.ModParticles;
import net.alek.succorstadiums.entity.ModEntityTypes;
import net.alek.succorstadiums.screen.mobarenagui.*;
import net.alek.succorstadiums.network.item.armor.*;
import net.alek.succorstadiums.item.ModItems;

// SuccorStadiumsClient class
public class SuccorStadiumsClient implements ClientModInitializer {
    private static boolean canDoubleJump;
    private static boolean hasReleasedJumpKey;

    @Override
    public void onInitializeClient() {

        ModKeyBindings.registerKeyBindings();
        ScreenCloseHandler.register();
        KeyInputHandler.register();

        EntityRenderers.register(ModEntityTypes.RAZOR_THORN, RazorThornEntityRenderer::new);
        EntityRenderers.register(ModEntityTypes.BALE_ARROW, BaleArrowEntityRenderer::new);

        EntityRenderers.register(ModEntityTypes.BANANA_SLIME, BananaSlimeRenderer::new);
        EntityRenderers.register(ModEntityTypes.FARMBIE, FarmbieRenderer::new);
        EntityRenderers.register(ModEntityTypes.FARMBIE_BLUE, FarmbieBlueRenderer::new);
        EntityRenderers.register(ModEntityTypes.GRASS_CREEPER, GrassCreeperRenderer::new);
        EntityRenderers.register(ModEntityTypes.SKELCROW, SkelcrowRenderer::new);
        EntityRenderers.register(ModEntityTypes.GRAY_SPIDER, GraySpiderRenderer::new);

        SpecialModelRenderers.ID_MAPPER.put(Identifier.fromNamespaceAndPath("succorstadiums", "cardboard_shield"), CardboardShieldSpecialRenderer.Unbaked.MAP_CODEC);
        ModelLayerRegistry.registerModelLayer(CardboardShieldLayers.CARDBOARD_SHIELD, ShieldModel::createLayer);

        ParticleProviderRegistry.getInstance().register(ModParticles.FOREST_ANGRY, sprites -> new ForestAuraParticle.Provider(sprites, false));
        ParticleProviderRegistry.getInstance().register(ModParticles.FOREST_ANGRY_SMALL, sprites -> new ForestAuraParticle.Provider(sprites, true));

        ClientTickEvents.END_CLIENT_TICK.register(SuccorStadiumsClient::handleArachnoDoubleJumpInput);

        MobArenaScreenHandler.register();
        ArenaRadiusRenderer.register();
        ItemLore.register();

        ClientPlayNetworking.registerGlobalReceiver(ResurrectionAmuletPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            ItemStack stack = new ItemStack(ModItems.RESURRECTION_AMULET);
            context.client().gameRenderer.displayItemActivation(stack);

            var level = context.client().level;
            var player = context.client().player;
            if (level == null || player == null) return;

            for (int i = 0; i < 100; i++) {
                level.addParticle(
                        new DustParticleOptions(0xC1B9AE, 2.0f),
                        player.getX() + (Math.random() - 0.5) * 2,
                        player.getY() + Math.random() * 2,
                        player.getZ() + (Math.random() - 0.5) * 2,
                        (Math.random() - 0.5) * 0.5,
                        Math.random() * 0.5,
                        (Math.random() - 0.5) * 0.5
                );
            }
        }));

        ClientPlayNetworking.registerGlobalReceiver(OpenMobArenaPayload.TYPE, (payload, context) -> context.client().execute(() ->
                Minecraft.getInstance().gui.setScreen(new MobArenaScreen(Component.literal("Mob Arena Manager")))));

        ClientPlayNetworking.registerGlobalReceiver(AdvancementListPayload.TYPE,
                (payload, context) -> AdvancementIdCache.set(payload.ids()));

        ClientPlayNetworking.registerGlobalReceiver(ArachnoDoubleJumpResultPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            if (!payload.success()) return;

            LocalPlayer player = context.client().player;
            if (player == null) return;

            Vec3 velocity = player.getDeltaMovement();
            double jumpBoost = 0.55D - velocity.y;
            player.push(0.0D, jumpBoost, 0.0D);
            player.fallDistance = 0.0F;
            spawnArachnoDoubleJumpClouds(player);
        }));
    }

    private static void handleArachnoDoubleJumpInput(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        if (isDoubleJumpBlocked(player)) {
            canDoubleJump = false;
            hasReleasedJumpKey = false;
            return;
        }

        if (player.onGround()) {
            hasReleasedJumpKey = false;
            canDoubleJump = true;
            return;
        }

        boolean jumpPressed = client.options.keyJump.isDown();
        if (!jumpPressed) {
            hasReleasedJumpKey = true;
            return;
        }

        if (player.getAbilities().flying || !canDoubleJump || !hasReleasedJumpKey) return;
        if (!isWearingFullArachnoSet(player)) return;

        canDoubleJump = false;
        ClientPlayNetworking.send(new ArachnoDoubleJumpPayload());
    }

    private static boolean isDoubleJumpBlocked(LocalPlayer player) {
        return player.isSwimming()
                || player.isInWater()
                || player.isInLava()
                || player.onClimbable()
                || player.getAbilities().flying;
    }

    private static void spawnArachnoDoubleJumpClouds(LocalPlayer player) {
        for (int i = 0; i < 18; i++) {
            double angle = Math.random() * Math.PI * 2.0D;
            double radius = 0.15D + Math.random() * 0.45D;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;

            player.level().addParticle(
                    ParticleTypes.CLOUD,
                    player.getX() + xOffset,
                    player.getY() + 0.1D + Math.random() * 0.25D,
                    player.getZ() + zOffset,
                    xOffset * 0.08D,
                    -0.02D + Math.random() * 0.04D,
                    zOffset * 0.08D
            );
        }
    }
}