package me.fy2ne.muto.menu;

import me.fy2ne.muto.api.MutoApi;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.util.concurrent.TimeUnit;

final class ModsWatcher {
	private static volatile boolean on = true;

	static boolean toggle() {
		return on = !on;
	}

	static void start() {
		Path dir = FabricLoader.getInstance().getGameDir().resolve("mods");
		if (!Files.isDirectory(dir)) return;
		var t = new Thread(() -> run(dir), "muto-menu-watch");
		t.setDaemon(true);
		t.start();
	}

	private static void run(Path dir) {
		try (var ws = dir.getFileSystem().newWatchService()) {
			dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
			while (true) {
				WatchKey k = ws.take();
				k.pollEvents();
				k.reset();
				// let copies finish, then swallow the burst
				Thread.sleep(800);
				WatchKey more;
				while ((more = ws.poll(300, TimeUnit.MILLISECONDS)) != null) {
					more.pollEvents();
					more.reset();
				}
				if (on && MutoApi.isSafeToReload()) {
					MenuMod.LOG.info("mods dir changed, reloading");
					MutoApi.reloadAsync();
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (java.io.IOException e) {
			MenuMod.LOG.warn("watch died: {}", e.getMessage());
		}
	}
}
