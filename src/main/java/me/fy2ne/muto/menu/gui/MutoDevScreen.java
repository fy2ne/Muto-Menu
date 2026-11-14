package me.fy2ne.muto.menu.gui;

import me.fy2ne.muto.api.MutoApi;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MutoDevScreen extends Screen {
    private final Screen parent;
    private final List<String> consoleLines = new ArrayList<>();
    private int scrollOffset = 0;

    public MutoDevScreen(Screen parent) {
        super(Component.literal("Muto Dev Diagnostics & Simulator"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int btnH = 20;
        int topY = 24;

        // Dev Mode Toggle Button
        boolean devMode = MutoApi.isDevMode();
        addRenderableWidget(Button.builder(
                Component.literal("DevMode: " + (devMode ? "§aACTIVE" : "§cOFF")),
                btn -> {
                    boolean next = !MutoApi.isDevMode();
                    MutoApi.setDevMode(next);
                    btn.setMessage(Component.literal("DevMode: " + (next ? "§aACTIVE" : "§cOFF")));
                })
                .bounds(20, topY, 110, btnH)
                .build());

        // Simulation Button
        addRenderableWidget(Button.builder(
                Component.literal("Simulate Reload"),
                btn -> {
                    String report = MutoApi.runReloadSimulation();
                    setConsoleText(report);
                })
                .bounds(136, topY, 120, btnH)
                .build());

        // Prune Ghost Mods Button
        addRenderableWidget(Button.builder(
                Component.literal("Prune Ghost Mods"),
                btn -> {
                    List<String> pruned = MutoApi.prunePhantomDisabledMods();
                    if (pruned.isEmpty()) {
                        setConsoleText("No phantom/ghost disabled mods detected. Registry is healthy.");
                    } else {
                        setConsoleText("Pruned " + pruned.size() + " ghost mod(s) from disabled registry:\n  " + String.join(", ", pruned));
                    }
                })
                .bounds(262, topY, 130, btnH)
                .build());

        // Refresh Logs Button
        addRenderableWidget(Button.builder(
                Component.literal("View Dev Logs"),
                btn -> {
                    List<String> logs = MutoApi.getDevLogs();
                    if (logs.isEmpty()) {
                        setConsoleText("No logs in muto-dev.log yet.");
                    } else {
                        Collections.reverse(logs);
                        setConsoleText(String.join("\n", logs));
                    }
                })
                .bounds(398, topY, 110, btnH)
                .build());

        // Screen History Button
        addRenderableWidget(Button.builder(
                Component.literal("Screen History"),
                btn -> {
                    List<String> hist = MutoApi.getScreenHistory();
                    if (hist.isEmpty()) {
                        setConsoleText("No screen transitions recorded yet.");
                    } else {
                        Collections.reverse(hist);
                        setConsoleText("=== RECENT SCREEN TRANSITIONS ===\n" + String.join("\n", hist));
                    }
                })
                .bounds(514, topY, 110, btnH)
                .build());

        // Bottom action buttons
        int bY = this.height - 28;
        addRenderableWidget(Button.builder(Component.literal("Clear Logs"), btn -> {
            MutoApi.clearDevLogs();
            setConsoleText("Dev logs cleared.");
        }).bounds(20, bY, 100, btnH).build());

        addRenderableWidget(Button.builder(Component.literal("Test Reload Flow"), btn -> {
            MutoApi.reloadWithBoundary(this);
        }).bounds(126, bY, 130, btnH).build());

        addRenderableWidget(Button.builder(Component.literal("Back"), btn -> onClose())
                .bounds(this.width - 110, bY, 90, btnH).build());

        if (consoleLines.isEmpty()) {
            List<String> hist = MutoApi.getScreenHistory();
            if (!hist.isEmpty()) {
                Collections.reverse(hist);
                setConsoleText("=== RECENT SCREEN FLOW ===\n" + String.join("\n", hist.subList(0, Math.min(10, hist.size()))));
            } else {
                setConsoleText("Muto Dev Diagnostics ready. Click 'Simulate Reload' to test dry-run diffs.");
            }
        }
    }

    private void setConsoleText(String text) {
        consoleLines.clear();
        if (text != null) {
            String[] split = text.split("\n");
            for (String s : split) {
                while (s.length() > 95) {
                    consoleLines.add(s.substring(0, 95));
                    s = "  " + s.substring(95);
                }
                consoleLines.add(s);
            }
        }
        scrollOffset = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) {
            scrollOffset = Math.max(0, scrollOffset - 2);
        } else if (verticalAmount < 0) {
            scrollOffset = Math.min(Math.max(0, consoleLines.size() - 10), scrollOffset + 2);
        }
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(this.font, Component.literal("Muto Dev Diagnostics & Telemetry Dashboard"), 20, 8, 0xFF38BDF8);

        int boxX = 20;
        int boxY = 50;
        int boxW = this.width - 40;
        int boxH = this.height - 84;

        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xDD0B0F19);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + 1, 0x6038BDF8);
        graphics.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, 0x6038BDF8);
        graphics.fill(boxX, boxY, boxX + 1, boxY + boxH, 0x6038BDF8);
        graphics.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, 0x6038BDF8);

        int visibleRows = (boxH - 10) / 11;
        int lineY = boxY + 6;
        for (int i = 0; i < visibleRows && (scrollOffset + i) < consoleLines.size(); i++) {
            String line = consoleLines.get(scrollOffset + i);
            int color = 0xFFE2E8F0;
            if (line.contains("ALERT") || line.contains("ERROR") || line.contains("FAIL") || line.contains("WARNING") || line.contains("!")) {
                color = 0xFFFF6B6B;
            } else if (line.contains("OK") || line.contains("ACTIVE") || line.contains("Cleaned") || line.contains("Pruned")) {
                color = 0xFF51CF66;
            } else if (line.contains("===") || line.contains("---")) {
                color = 0xFF38BDF8;
            }
            graphics.text(this.font, Component.literal(line), boxX + 8, lineY, color);
            lineY += 11;
        }

        if (consoleLines.size() > visibleRows) {
            String scrollText = String.format("[%d/%d] (Scroll to view)", scrollOffset + 1, consoleLines.size());
            graphics.text(this.font, Component.literal(scrollText), boxX + boxW - 140, boxY + 6, 0xFF64748B);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }
}
