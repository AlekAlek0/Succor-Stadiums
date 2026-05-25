package net.alek.succorstadiums;

import net.alek.succorstadiums.arena.ArenaSessionManager;
import net.alek.succorstadiums.arena.MobArenaManager;
import net.alek.succorstadiums.command.ModCommands;
import net.alek.succorstadiums.creativemodetab.ModCreativeModeTabs;
import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Mod Initializer
public class SuccorStadiums implements ModInitializer {
	public static final String MOD_ID = "succorstadiums";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModCreativeModeTabs.registerModCreativeModeTabs();
		ModItems.registerModItems();
		ModCommands.registerModCommands();

		ArenaSessionManager.init(); // registers the tick event
		ServerLifecycleEvents.SERVER_STARTED.register(MobArenaManager::init);
	}
}