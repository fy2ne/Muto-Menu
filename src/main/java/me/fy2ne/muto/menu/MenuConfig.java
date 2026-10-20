package me.fy2ne.muto.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.fy2ne.muto.api.MutoApi;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class MenuConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static MenuConfig inst;

	public boolean autoWatchFolder = true;
	public boolean autoReloadResources = true;
	public boolean warnDependencies = true;

	public static MenuConfig get() {
		if (inst == null) load();
		return inst;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("muto-menu.json");
	}

	public static void load() {
		Path f = file();
		if (Files.exists(f)) {
			try (Reader r = Files.newBufferedReader(f)) {
				MenuConfig loaded = GSON.fromJson(r, MenuConfig.class);
				if (loaded != null) {
					inst = loaded;
					syncToRuntime();
					return;
				}
			} catch (Exception e) {
				MenuMod.LOG.warn("bad muto-menu.json, resetting: {}", e.getMessage());
			}
		}
		inst = new MenuConfig();
		save();
		syncToRuntime();
	}

	public static void save() {
		if (inst == null) return;
		try {
			Path f = file();
			if (f.getParent() != null) Files.createDirectories(f.getParent());
			Path tmp = f.resolveSibling("muto-menu.json.tmp");
			try (Writer w = Files.newBufferedWriter(tmp)) {
				GSON.toJson(inst, w);
			}
			Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			MenuMod.LOG.warn("failed saving muto-menu.json: {}", e.getMessage());
		}
	}

	public static void syncToRuntime() {
		if (inst == null) return;
		MutoApi.setFolderWatcherEnabled(inst.autoWatchFolder);
		MutoApi.setAutoReloadResources(inst.autoReloadResources);
	}
}
