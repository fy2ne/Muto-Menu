package me.fy2ne.muto.menu;

import com.mojang.blaze3d.platform.InputConstants;
import me.fy2ne.muto.api.MutoEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MenuMod implements ClientModInitializer {
	static final Logger LOG = LoggerFactory.getLogger("muto-menu");

	@Override
	public void onInitializeClient() {
		var cat = KeyMapping.Category.MISC;
		var open = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.muto_menu.open", InputConstants.Type.KEYBOARD, InputConstants.KEY_O, cat));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (open.consumeClick()) mc.setScreenAndShow(new ModsScreen(mc.gui.screen()));
		});

		MutoEvents.RELOAD_FINISH.register((end, diff, ok, ms, err) ->
			LOG.info("reload {} in {}ms", ok ? "ok" : "failed", ms));

		ModsWatcher.start();
	}
}

