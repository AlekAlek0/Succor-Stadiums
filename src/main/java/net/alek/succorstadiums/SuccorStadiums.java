package net.alek.succorstadiums;

import net.alek.succorstadiums.arena.ArenaSessionManager;
import net.alek.succorstadiums.arena.MobArenaManager;
import net.alek.succorstadiums.command.ModCommands;
import net.alek.succorstadiums.creativemodetab.ModCreativeModeTabs;
import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.network.ResurrectionAmuletPayload;
import net.alek.succorstadiums.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Mod Initializer
public class SuccorStadiums implements ModInitializer {
	public static final String MOD_ID = "succorstadiums";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModCreativeModeTabs.registerModCreativeModeTabs();
		ModCommands.registerModCommands();
		ModItems.registerModItems();
		ModSounds.registerModSounds();

		PayloadTypeRegistry.clientboundPlay().register(ResurrectionAmuletPayload.TYPE, ResurrectionAmuletPayload.CODEC);

		// Register the tick event
		ArenaSessionManager.init();
		ServerLifecycleEvents.SERVER_STARTED.register(MobArenaManager::init);
	}
}