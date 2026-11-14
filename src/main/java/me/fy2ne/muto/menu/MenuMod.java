package me.fy2ne.muto.menu;

import me.fy2ne.muto.api.MutoEvents;
import me.fy2ne.muto.menu.automation.ModsFolderWatcher;
import me.fy2ne.muto.menu.gui.ModMenuDiskAdapter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MenuMod implements ClientModInitializer {
	public static final Logger LOG = LoggerFactory.getLogger("muto-menu");

	@Override
	public void onInitializeClient() {
		MenuConfig.load();

		ModsFolderWatcher.init();
		if (MenuConfig.get().autoWatchFolder) {
			ModsFolderWatcher.setEnabled(true);
		}

		MutoEvents.RELOAD_FINISH.register((end, diff, ok, ms, err) -> {
			LOG.info("reload {} in {}ms", ok ? "ok" : "rolled back", ms);
			if (ok && diff != null) {
				Minecraft mc = Minecraft.getInstance();
				if (mc != null) {
					mc.execute(() -> {
						ModMenuDiskAdapter.syncDiskMods();
						ModMenuDiskAdapter.applyDiff(diff);
						ModMenuDiskAdapter.clearModCountCache();
						if (mc.gui != null && mc.gui.screen() != null && mc.gui.screen().getClass().getName().contains("ModsScreen")) {
							ModMenuDiskAdapter.refreshModsScreen(mc.gui.screen());
						}
					});
				}
			}
		});

		LOG.info("muto menu initialized (modmenu native integration)");
	}
}
