package me.fy2ne.muto.menu;

import me.fy2ne.muto.api.MutoEvents;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MenuMod implements ClientModInitializer {
	public static final Logger LOG = LoggerFactory.getLogger("muto-menu");

	@Override
	public void onInitializeClient() {
		MenuConfig.load();

		MutoEvents.RELOAD_FINISH.register((end, diff, ok, ms, err) ->
			LOG.info("reload {} in {}ms", ok ? "ok" : "rolled back", ms));

		LOG.info("muto menu initialized (modmenu native integration)");
	}
}
