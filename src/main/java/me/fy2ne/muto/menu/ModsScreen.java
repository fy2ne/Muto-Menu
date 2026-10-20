package me.fy2ne.muto.menu;

import me.fy2ne.muto.api.MutoApi;
import me.fy2ne.muto.mods.DiskMod;
import me.fy2ne.muto.mods.ModStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModsScreen extends Screen {
	private static final int ROW_H = 22;
	private final Screen parent;
	private final ModStateManager state = ModStateManager.INSTANCE;
	private List<DiskMod> mods = List.of();
	private int top = 0;
	private Component status = Component.empty();
	private String lastWarnedModId = null;

	public ModsScreen(Screen parent) {
		super(Component.literal("Muto Menu"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		mods = state.scanDiskMods().stream()
			.sorted(Comparator.comparing(DiskMod::isCore).thenComparing(m -> m.name().toLowerCase()))
			.toList();

		int w = Math.min(width - 40, 340);
		int x = (width - w) / 2;
		int rows = Math.max(1, (height - 100) / ROW_H);
		top = Math.max(0, Math.min(top, Math.max(0, mods.size() - rows)));

		addRenderableWidget(new StringWidget(x, 10, 120, 14, title, font));

		MenuConfig cfg = MenuConfig.get();
		int topBtnW = 104;
		addRenderableWidget(Button.builder(
			Component.literal("Watch: " + (cfg.autoWatchFolder ? "ON" : "OFF")),
			b -> {
				cfg.autoWatchFolder = !cfg.autoWatchFolder;
				MenuConfig.save();
				MenuConfig.syncToRuntime();
				status = Component.literal("folder watcher " + (cfg.autoWatchFolder ? "enabled" : "paused"));
				rebuildWidgets();
			}).bounds(x + w - topBtnW * 2 - 4, 6, topBtnW, 18).build());

		addRenderableWidget(Button.builder(
			Component.literal("History (" + MutoApi.getHistory().size() + ")"),
			b -> minecraft.setScreenAndShow(new ReloadHistoryScreen(this))
		).bounds(x + w - topBtnW, 6, topBtnW, 18).build());

		addRenderableWidget(new StringWidget(x, 28, w, 12, status, font));

		for (int i = 0; i < rows && top + i < mods.size(); i++) {
			var m = mods.get(top + i);
			boolean on = state.getStagedState(m.id());
			boolean staged = state.isStaged(m.id());
			String prefix = m.isCore() ? "[core] " : (on ? "[on]   " : "[off]  ");
			var label = Component.literal(prefix + m.name() + " " + m.version() + (staged ? "  *" : ""));
			var btn = Button.builder(label, b -> onModClicked(m))
				.bounds(x, 44 + i * ROW_H, w, 20).build();
			btn.active = !m.isCore();
			addRenderableWidget(btn);
		}

		int by = height - 28;
		int third = (w - 8) / 3;
		boolean inWorld = !MutoApi.isSafeToReload() && !MutoApi.isReloading();

		var apply = Button.builder(Component.literal("Apply (" + state.getStagedCount() + ")"), b -> apply())
			.bounds(x, by, third, 20).build();
		apply.active = state.hasStagedChanges() && !MutoApi.isReloading();
		addRenderableWidget(apply);

		String reloadLabel = inWorld ? "Reload & Rejoin" : "Reload";
		var reloadBtn = Button.builder(Component.literal(reloadLabel), b -> reload())
			.bounds(x + third + 4, by, third, 20).build();
		reloadBtn.active = !MutoApi.isReloading();
		addRenderableWidget(reloadBtn);

		addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
			.bounds(x + (third + 4) * 2, by, third, 20).build());
	}

	private void onModClicked(DiskMod m) {
		boolean currentlyOn = state.getStagedState(m.id());
		if (MenuConfig.get().warnDependencies) {
			if (currentlyOn) {
				List<DiskMod> dependents = state.getDependents(m.id(), mods);
				if (!dependents.isEmpty() && !m.id().equals(lastWarnedModId)) {
					String names = dependents.stream().map(DiskMod::name).collect(Collectors.joining(", "));
					status = Component.literal("used by: " + names + " (click again to disable)");
					lastWarnedModId = m.id();
					rebuildWidgets();
					return;
				}
			} else {
				List<String> missing = state.getMissingDependencies(m, mods);
				if (!missing.isEmpty() && !m.id().equals(lastWarnedModId)) {
					status = Component.literal("needs: " + String.join(", ", missing) + " (click again to enable)");
					lastWarnedModId = m.id();
					rebuildWidgets();
					return;
				}
			}
		}

		lastWarnedModId = null;
		state.stageToggle(m.id());
		status = Component.empty();
		rebuildWidgets();
	}

	private void apply() {
		var log = state.applyFileRenames();
		status = Component.literal(log.size() + " change(s) written");
		reload();
	}

	private void reload() {
		if (!MutoApi.isSafeToReload()) {
			MutoApi.reloadWithBoundary(parent);
			return;
		}
		status = Component.literal("reloading...");
		rebuildWidgets();
		MutoApi.reloadAsync(stage -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc != null) {
				mc.execute(() -> {
					status = Component.literal(stage);
					if (mc.gui != null && mc.gui.screen() == this) rebuildWidgets();
				});
			}
		}).thenAccept(res -> Minecraft.getInstance().execute(() -> {
			status = Component.literal(res.success()
				? "done in " + res.durationMs() + "ms"
				: "rolled back: " + res.error());
			if (Minecraft.getInstance().gui != null && Minecraft.getInstance().gui.screen() == this) {
				rebuildWidgets();
			}
		}));
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double sx, double sy) {
		top = Math.max(0, top - (int) Math.signum(sy));
		rebuildWidgets();
		return true;
	}

	@Override
	public void onClose() {
		minecraft.setScreenAndShow(parent);
	}
}
