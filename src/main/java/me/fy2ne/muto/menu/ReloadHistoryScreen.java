package me.fy2ne.muto.menu;

import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.api.MutoApi;
import me.fy2ne.muto.engine.ReloadHistoryEntry;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReloadHistoryScreen extends Screen {
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
		.withZone(ZoneId.systemDefault());
	private static final int ROW_H = 22;

	private final Screen parent;
	private List<ReloadHistoryEntry> entries = List.of();
	private int top = 0;
	private ReloadHistoryEntry selected;

	public ReloadHistoryScreen(Screen parent) {
		super(Component.literal("Muto Reload History"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		List<ReloadHistoryEntry> raw = new ArrayList<>(MutoApi.getHistory());
		Collections.reverse(raw);
		entries = raw;

		int w = Math.min(width - 40, 380);
		int x = (width - w) / 2;
		int listBottom = selected != null ? height - 86 : height - 38;
		int rows = Math.max(1, (listBottom - 36) / ROW_H);
		top = Math.max(0, Math.min(top, Math.max(0, entries.size() - rows)));

		addRenderableWidget(new StringWidget(x, 12, w, 12,
			Component.literal("Muto Reload History (" + entries.size() + ")"), font));

		if (entries.isEmpty()) {
			addRenderableWidget(new StringWidget(x, 48, w, 12,
				Component.literal("No reloads recorded yet."), font));
		}

		for (int i = 0; i < rows && top + i < entries.size(); i++) {
			ReloadHistoryEntry e = entries.get(top + i);
			String ts = FMT.format(Instant.ofEpochMilli(e.timestamp()));
			String tag = e.success() ? "[OK]" : "[ROLLBACK]";
			ModDiff d = e.diff();
			String diffSummary = d == null ? "" : String.format("+%d -%d ~%d",
				d.added().size(), d.removed().size(), d.updated().size());
			String lbl = String.format("%s  %s  %dms  (%s)", ts, tag, e.durationMs(), diffSummary);

			addRenderableWidget(Button.builder(Component.literal(lbl), b -> {
				selected = (selected == e) ? null : e;
				rebuildWidgets();
			}).bounds(x, 34 + i * ROW_H, w, 20).build());
		}

		if (selected != null && selected.logs() != null && !selected.logs().isEmpty()) {
			List<String> logs = selected.logs();
			int start = Math.max(0, logs.size() - 3);
			int ly = height - 78;
			for (int i = start; i < logs.size(); i++) {
				String line = logs.get(i);
				if (line.length() > 68) line = line.substring(0, 65) + "...";
				addRenderableWidget(new StringWidget(x, ly, w, 10, Component.literal(line), font));
				ly += 12;
			}
		}

		addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
			.bounds((width - 120) / 2, height - 28, 120, 20).build());
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
