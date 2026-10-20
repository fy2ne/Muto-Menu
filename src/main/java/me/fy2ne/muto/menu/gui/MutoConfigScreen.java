package me.fy2ne.muto.menu.gui;

import me.fy2ne.muto.api.MutoApi;
import me.fy2ne.muto.menu.MenuConfig;
import me.fy2ne.muto.menu.ReloadHistoryScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class MutoConfigScreen extends Screen {
    private final Screen parent;

    public MutoConfigScreen(Screen parent) {
        super(Component.literal("Muto Menu Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        MenuConfig cfg = MenuConfig.get();
        int btnW = Math.min(220, (this.width - 40) / 2 - 8);
        int btnH = 20;
        int leftX = this.width / 2 - btnW - 6;
        int rightX = this.width / 2 + 6;
        int startY = 56;
        int gapY = 26;

        // Option 1: Auto-Watch Mods Folder
        this.addRenderableWidget(Button.builder(
                getOptionText("Auto-Watch Mods Folder", cfg.autoWatchFolder),
                btn -> {
                    cfg.autoWatchFolder = !cfg.autoWatchFolder;
                    MenuConfig.save();
                    MenuConfig.syncToRuntime();
                    btn.setMessage(getOptionText("Auto-Watch Mods Folder", cfg.autoWatchFolder));
                })
                .bounds(leftX, startY, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Automatically trigger hot-reload when jars in mods folder are added or replaced")))
                .build());

        // Option 2: Auto-Reload Resources
        this.addRenderableWidget(Button.builder(
                getOptionText("Auto-Reload Resources", cfg.autoReloadResources),
                btn -> {
                    cfg.autoReloadResources = !cfg.autoReloadResources;
                    MenuConfig.save();
                    MenuConfig.syncToRuntime();
                    btn.setMessage(getOptionText("Auto-Reload Resources", cfg.autoReloadResources));
                })
                .bounds(rightX, startY, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Reload texture packs and client assets when mods are hot-swapped")))
                .build());

        // Option 3: Dependency Warnings
        this.addRenderableWidget(Button.builder(
                getOptionText("Missing Dep Warnings", cfg.warnDependencies),
                btn -> {
                    cfg.warnDependencies = !cfg.warnDependencies;
                    MenuConfig.save();
                    btn.setMessage(getOptionText("Missing Dep Warnings", cfg.warnDependencies));
                })
                .bounds(leftX, startY + gapY, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Highlight missing dependencies on mod badges in ModMenu")))
                .build());

        // Option 4: Reload History
        this.addRenderableWidget(Button.builder(
                Component.literal("View Reload History..."),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new ReloadHistoryScreen(this));
                    }
                })
                .bounds(rightX, startY + gapY, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Inspect previous reload logs, durations, and diffs")))
                .build());

        // Done Button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> {
            MenuConfig.save();
            if (this.minecraft != null) {
                this.minecraft.setScreenAndShow(this.parent);
            }
        })
                .bounds(this.width / 2 - 100, this.height - 36, 200, 20)
                .build());
    }

    private static Component getOptionText(String label, boolean state) {
        return Component.literal(label + ": " + (state ? "§aON" : "§cOFF"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        if (this.minecraft != null) {
            graphics.text(this.minecraft.font, this.title, this.width / 2 - this.minecraft.font.width(this.title) / 2, 22, 0xFFFFFFFF);
        }
    }

    @Override
    public void onClose() {
        MenuConfig.save();
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }
}
