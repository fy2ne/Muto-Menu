package me.fy2ne.muto.menu;

import com.mojang.blaze3d.platform.InputConstants;
import me.fy2ne.muto.api.MutoEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MenuMod implements ClientModInitializer {
	static final Logger LOG = LoggerFactory.getLogger("muto-menu");

	@Override
	public void onInitializeClient() {
		MenuConfig.load();

		var open = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.muto_menu.open", InputConstants.Type.KEYBOARD, InputConstants.KEY_O, KeyMapping.Category.MISC));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (open.consumeClick()) {
				mc.setScreenAndShow(new ModsScreen(mc.gui != null ? mc.gui.screen() : null));
			}
		});

		ScreenEvents.AFTER_INIT.register((client, screen, sw, sh) -> {
			if (screen instanceof TitleScreen || screen instanceof PauseScreen) {
				Screens.getWidgets(screen).add(
					Button.builder(Component.literal("Muto"), b -> client.setScreenAndShow(new ModsScreen(screen)))
						.bounds(sw - 56, 6, 50, 20).build()
				);
			}
		});

		MutoEvents.RELOAD_FINISH.register((end, diff, ok, ms, err) ->
			LOG.info("reload {} in {}ms", ok ? "ok" : "rolled back", ms));

		LOG.info("muto menu ready");
	}
}
