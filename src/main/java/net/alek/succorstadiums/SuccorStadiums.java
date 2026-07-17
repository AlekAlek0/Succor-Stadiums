package net.alek.succorstadiums;

import net.alek.succorstadiums.arena.ArenaSessionManager;
import net.alek.succorstadiums.arena.MobArenaManager;
import net.alek.succorstadiums.command.ModCommands;
import net.alek.succorstadiums.creativemodetab.ModCreativeModeTabs;
import net.alek.succorstadiums.entity.ModEntityAttributes;
import net.alek.succorstadiums.entity.ModEntityTypes;
import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.item.trinkets.DogWhistleItem;
import net.alek.succorstadiums.network.*;
import net.alek.succorstadiums.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Mod initializer class
public class SuccorStadiums implements ModInitializer {

	// Define mod id and logger
	public static final String MOD_ID = "succorstadiums";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Initialize mod
	@Override
	public void onInitialize() {

		// Register mod content
		ModCreativeModeTabs.registerModCreativeModeTabs();
		ModCommands.registerModCommands();
		ModSounds.registerModSounds();
		ModItems.registerModItems();

		// Register mod entity types and attributes
		ModEntityTypes.registerModEntityTypes();
		ModEntityAttributes.register();

		// Register item network packets
		PayloadTypeRegistry.clientboundPlay().register(ResurrectionAmuletPayload.TYPE, ResurrectionAmuletPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ArachnoDoubleJumpPayload.TYPE, ArachnoDoubleJumpPayload.CODEC);
		ArachnoDoubleJumpHandler.register();

		// Register mob arena network packets
		PayloadTypeRegistry.clientboundPlay().register(OpenMobArenaPayload.TYPE, OpenMobArenaPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(OpenMobArenaRequestPayload.TYPE, OpenMobArenaRequestPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ArenaActionPayload.TYPE, ArenaActionPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ArenaDataPayload.TYPE, ArenaDataPayload.CODEC);
		ArenaPacketHandler.register();

		// Start Mob arenas on server start
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MobArenaManager.init(server);
			ArenaSessionManager.init();
		});

		// Wolf despawn tick
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerLevel serverLevel : server.getAllLevels()) {
				long currentTime = serverLevel.getGameTime();
				List<UUID> toRemove = new ArrayList<>();

				for (Map.Entry<UUID, Long> entry : DogWhistleItem.SUMMONED_WOLVES.entrySet()) {
					if (currentTime >= entry.getValue()) {
						Entity entity = serverLevel.getEntity(entry.getKey());
						if (entity instanceof Wolf wolf) {
							wolf.discard();
						}
						toRemove.add(entry.getKey());
					}
				}
				toRemove.forEach(DogWhistleItem.SUMMONED_WOLVES.keySet()::remove);
			}
		});
	}
}