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

public class ModsScreen extends Screen {
	private static final int ROW_H = 22;
	private final Screen parent;
	private final ModStateManager state = ModStateManager.INSTANCE;
	private List<DiskMod> mods = List.of();
	private int top = 0;
	private Component status = Component.empty();

	public ModsScreen(Screen parent) {
		super(Component.literal("Muto Menu"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		mods = state.scanDiskMods().stream()
			.sorted(Comparator.comparing((DiskMod m) -> m.isCore()).thenComparing(m -> m.name().toLowerCase()))
			.toList();

		int w = Math.min(width - 40, 320);
		int x = (width - w) / 2;
		int rows = Math.max(1, (height - 90) / ROW_H);
		top = Math.max(0, Math.min(top, Math.max(0, mods.size() - rows)));

		addRenderableWidget(new StringWidget(x, 12, w, 12, title, font));
		addRenderableWidget(new StringWidget(x, 28, w, 12, status, font));

		for (int i = 0; i < rows && top + i < mods.size(); i++) {
			var m = mods.get(top + i);
			boolean on = state.getStagedState(m.id());
			boolean staged = state.isStaged(m.id());
			var label = Component.literal((on ? "[on]  " : "[off] ") + m.name() + " " + m.version() + (staged ? "  *" : ""));
			var btn = Button.builder(label, b -> {
				state.stageToggle(m.id());
				rebuildWidgets();
			}).bounds(x, 46 + i * ROW_H, w, 20).build();
			btn.active = !m.isCore();
			addRenderableWidget(btn);
		}

		int by = height - 30;
		int third = (w - 8) / 3;
		var apply = Button.builder(Component.literal("Apply (" + state.getStagedCount() + ")"), b -> apply())
			.bounds(x, by, third, 20).build();
		apply.active = state.hasStagedChanges() && !MutoApi.isReloading();
		addRenderableWidget(apply);
		addRenderableWidget(Button.builder(Component.literal("Reload"), b -> reload())
			.bounds(x + third + 4, by, third, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
			.bounds(x + (third + 4) * 2, by, third, 20).build());
	}

	private void apply() {
		var log = state.applyFileRenames();
		status = Component.literal(log.size() + " change(s) written");
		reload();
	}

	private void reload() {
		if (!MutoApi.isSafeToReload()) {
			status = Component.literal("leave the world first");
			rebuildWidgets();
			return;
		}
		status = Component.literal("reloading");
		rebuildWidgets();
		MutoApi.reloadAsync().thenAccept(res -> Minecraft.getInstance().execute(() -> {
			status = Component.literal(res.success() ? "done in " + res.durationMs() + "ms" : "failed: " + res.error());
			if (Minecraft.getInstance().gui.screen() == this) rebuildWidgets();
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

