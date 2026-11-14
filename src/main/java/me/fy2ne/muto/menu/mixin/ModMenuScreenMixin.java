package me.fy2ne.muto.menu.mixin;

import me.fy2ne.muto.api.MutoApi;
import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.menu.gui.ModMenuDiskAdapter;
import me.fy2ne.muto.menu.gui.MutoModChangesScreen;
import me.fy2ne.muto.menu.gui.MutoModFilter;
import me.fy2ne.muto.engine.ModDependencyAnalyzer;
import me.fy2ne.muto.mods.DiskMod;
import me.fy2ne.muto.mods.ModStateManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.gui.ModsScreen")
public abstract class ModMenuScreenMixin extends Screen {
    protected ModMenuScreenMixin(Component title) {
        super(title);
    }

    private static final Identifier SPRITE_DISABLE = Identifier.fromNamespaceAndPath("muto_menu", "icon/disable");
    private static final Identifier SPRITE_ENABLE = Identifier.fromNamespaceAndPath("muto_menu", "icon/enable");
    private static final Identifier SPRITE_LOAD = Identifier.fromNamespaceAndPath("muto_menu", "icon/load");
    private static final Identifier SPRITE_REVERT = Identifier.fromNamespaceAndPath("muto_menu", "icon/revert");

    private static final Identifier SPRITE_FILTER_ALL = Identifier.fromNamespaceAndPath("muto_menu", "icon/filter_all");
    private static final Identifier SPRITE_FILTER_ACTIVE = Identifier.fromNamespaceAndPath("muto_menu", "icon/filter_active");
    private static final Identifier SPRITE_FILTER_DISABLED = Identifier.fromNamespaceAndPath("muto_menu", "icon/filter_disabled");
    private static final Identifier SPRITE_FILTER_UNLOADED = Identifier.fromNamespaceAndPath("muto_menu", "icon/filter_unloaded");

    @Shadow
    private com.terraformersmc.modmenu.gui.widget.entries.ModListEntry selected;

    @Shadow
    private com.terraformersmc.modmenu.gui.widget.ModListWidget modList;

    @Shadow
    private EditBox searchBox;

    @Shadow
    private SpriteIconButton filtersButton;

    @Shadow
    private SpriteIconButton configureButton;

    @Shadow
    protected Screen previousScreen;

    @Shadow
    public abstract boolean getModHasConfigScreen(String modId);

    // Action buttons (native SpriteIconButton)
    private SpriteIconButton muto$actionDisableBtn;
    private SpriteIconButton muto$actionEnableBtn;
    private SpriteIconButton muto$actionLoadBtn;
    private SpriteIconButton muto$actionRevertBtn;

    // Filter buttons (native SpriteIconButton)
    private SpriteIconButton muto$filterAllBtn;
    private SpriteIconButton muto$filterActiveBtn;
    private SpriteIconButton muto$filterDisabledBtn;
    private SpriteIconButton muto$filterUnloadedBtn;

    @Inject(method = "safelyOpenConfigScreen", at = @At("HEAD"), cancellable = true, require = 0)
    private void muto$blockConfigForDisabled(String modId, CallbackInfo ci) {
        if (modId != null && !ModStateManager.INSTANCE.isModLoaded(modId) && ModStateManager.INSTANCE.isModDisabled(modId)) {
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("HEAD"), require = 0)
    private void muto$syncDiskModsBeforeInit(CallbackInfo ci) {
        ModMenuDiskAdapter.syncDiskMods();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void muto$injectModMenuUi(CallbackInfo ci) {
        // 1. Filter toggle SpriteIconButtons placed next to ModMenu's filter funnel
        int fx = (filtersButton != null) ? filtersButton.getX() - 22 : 12;
        int fy = (filtersButton != null) ? filtersButton.getY() : 10;
        if (searchBox != null && filtersButton != null) {
            searchBox.setWidth(Math.max(40, searchBox.getWidth() - 22));
        }

        muto$filterAllBtn = SpriteIconButton.builder(Component.literal("Filter: All"), btn -> muto$cycleFilter(), true)
                .size(20, 20)
                .sprite(SPRITE_FILTER_ALL, 16, 16)
                .tooltip(Component.literal("Filter: All Mods (Click to cycle)"))
                .build();
        muto$filterAllBtn.setPosition(fx, fy);

        muto$filterActiveBtn = SpriteIconButton.builder(Component.literal("Filter: Active"), btn -> muto$cycleFilter(), true)
                .size(20, 20)
                .sprite(SPRITE_FILTER_ACTIVE, 16, 16)
                .tooltip(Component.literal("Filter: Active Mods (Click to cycle)"))
                .build();
        muto$filterActiveBtn.setPosition(fx, fy);

        muto$filterDisabledBtn = SpriteIconButton.builder(Component.literal("Filter: Disabled"), btn -> muto$cycleFilter(), true)
                .size(20, 20)
                .sprite(SPRITE_FILTER_DISABLED, 16, 16)
                .tooltip(Component.literal("Filter: Disabled Mods (Click to cycle)"))
                .build();
        muto$filterDisabledBtn.setPosition(fx, fy);

        muto$filterUnloadedBtn = SpriteIconButton.builder(Component.literal("Filter: Unloaded"), btn -> muto$cycleFilter(), true)
                .size(20, 20)
                .sprite(SPRITE_FILTER_UNLOADED, 16, 16)
                .tooltip(Component.literal("Filter: Unloaded Mods (Click to cycle)"))
                .build();
        muto$filterUnloadedBtn.setPosition(fx, fy);

        this.addRenderableWidget(muto$filterAllBtn);
        this.addRenderableWidget(muto$filterActiveBtn);
        this.addRenderableWidget(muto$filterDisabledBtn);
        this.addRenderableWidget(muto$filterUnloadedBtn);
        muto$updateFilterButtons();

        // 2. Action SpriteIconButtons (Disable, Enable, Load, Revert)
        int rightEdgeX = (configureButton != null) ? configureButton.getX() : (this.width - 24);
        int ay = (configureButton != null) ? configureButton.getY() : 48;

        muto$actionDisableBtn = SpriteIconButton.builder(Component.literal("Disable Mod"), btn -> muto$triggerDisable(), true)
                .size(20, 20)
                .sprite(SPRITE_DISABLE, 16, 16)
                .tooltip(Component.literal("Disable Mod"))
                .build();
        muto$actionDisableBtn.setPosition(rightEdgeX - 22, ay);

        muto$actionEnableBtn = SpriteIconButton.builder(Component.literal("Enable Mod"), btn -> muto$triggerEnable(), true)
                .size(20, 20)
                .sprite(SPRITE_ENABLE, 16, 16)
                .tooltip(Component.literal("Enable Mod"))
                .build();
        muto$actionEnableBtn.setPosition(rightEdgeX - 22, ay);

        muto$actionLoadBtn = SpriteIconButton.builder(Component.literal("Load Mod"), btn -> muto$triggerEnable(), true)
                .size(20, 20)
                .sprite(SPRITE_LOAD, 16, 16)
                .tooltip(Component.literal("Load Mod"))
                .build();
        muto$actionLoadBtn.setPosition(rightEdgeX, ay);

        muto$actionRevertBtn = SpriteIconButton.builder(Component.literal("Revert"), btn -> muto$triggerRevert(), true)
                .size(20, 20)
                .sprite(SPRITE_REVERT, 16, 16)
                .tooltip(Component.literal("Pending Change (Click to revert)"))
                .build();
        muto$actionRevertBtn.setPosition(rightEdgeX - 22, ay);

        muto$actionDisableBtn.visible = false;
        muto$actionEnableBtn.visible = false;
        muto$actionLoadBtn.visible = false;
        muto$actionRevertBtn.visible = false;

        this.addRenderableWidget(muto$actionDisableBtn);
        this.addRenderableWidget(muto$actionEnableBtn);
        this.addRenderableWidget(muto$actionLoadBtn);
        this.addRenderableWidget(muto$actionRevertBtn);

        // Intercept ModMenu's Done button directly on the widget
        for (var child : this.children()) {
            if (child instanceof Button b) {
                if (b.getY() >= this.height - 40 && b.getX() >= this.width / 2) {
                    try {
                        Field f = Button.class.getDeclaredField("onPress");
                        f.setAccessible(true);
                        Button.OnPress orig = (Button.OnPress) f.get(b);
                        Button.OnPress wrapped = btn -> {
                            if (ModStateManager.INSTANCE.hasStagedChanges()) {
                                if (this.minecraft != null) {
                                    this.minecraft.setScreenAndShow(new MutoModChangesScreen(this, () -> {
                                        if (this.minecraft != null) {
                                            MutoApi.reloadWithBoundary(new com.terraformersmc.modmenu.gui.ModsScreen(this.previousScreen));
                                        }
                                    }));
                                }
                            } else if (orig != null) {
                                orig.onPress(btn);
                            } else if (this.minecraft != null) {
                                this.minecraft.setScreenAndShow(this.previousScreen);
                            }
                        };

                        try {
                            Field uField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                            uField.setAccessible(true);
                            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uField.get(null);
                            long offset = unsafe.objectFieldOffset(f);
                            unsafe.putObject(b, offset, wrapped);
                        } catch (Throwable ignored) {
                            f.set(b, wrapped);
                        }
                    } catch (Throwable t) {
                        MutoLog.warn("error wrapping ModMenu Done button: {}", t.getMessage());
                    }
                    break;
                }
            }
        }

        muto$updateActionButtons();
    }

    @Inject(method = "updateSelectedEntry", at = @At("RETURN"), require = 0)
    private void muto$onSelectedEntryChanged(com.terraformersmc.modmenu.gui.widget.entries.ModListEntry entry, CallbackInfo ci) {
        muto$updateActionButtons();
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true, require = 0)
    private void muto$onClose(CallbackInfo ci) {
        if (ModStateManager.INSTANCE.hasStagedChanges()) {
            ci.cancel();
            if (this.minecraft != null) {
                this.minecraft.setScreenAndShow(new MutoModChangesScreen(this, () -> {
                    if (this.minecraft != null) {
                        MutoApi.reloadWithBoundary(new com.terraformersmc.modmenu.gui.ModsScreen(this.previousScreen));
                    }
                }));
            }
        }
    }

    private void muto$cycleFilter() {
        MutoModFilter.CURRENT = MutoModFilter.CURRENT.next();
        muto$updateFilterButtons();
        if (modList != null) {
            ModMenuDiskAdapter.syncDiskMods();
            modList.reloadFilters();
        }
    }

    private void muto$triggerDisable() {
        if (this.selected != null && this.selected.getMod() != null) {
            String modId = this.selected.getMod().getId();
            if (!ModStateManager.INSTANCE.isProtectedMod(modId)) {
                DiskMod dm = ModStateManager.INSTANCE.findDiskMod(modId);
                if (dm == null) return;
                ModStateManager.INSTANCE.stageDisable(modId);
                muto$updateActionButtons();
                if (modList != null) {
                    modList.reloadFilters();
                }
            }
        }
    }

    private void muto$triggerEnable() {
        if (this.selected != null && this.selected.getMod() != null) {
            String modId = this.selected.getMod().getId();
            if (!ModStateManager.INSTANCE.isProtectedMod(modId)) {
                DiskMod dm = ModStateManager.INSTANCE.findDiskMod(modId);
                if (dm == null) return;
                ModStateManager.INSTANCE.stageEnable(modId);
                muto$updateActionButtons();
                if (modList != null) {
                    modList.reloadFilters();
                }
            }
        }
    }

    private void muto$triggerRevert() {
        if (this.selected != null && this.selected.getMod() != null) {
            String modId = this.selected.getMod().getId();
            ModStateManager.INSTANCE.revertStaging(modId);
            muto$updateActionButtons();
            if (modList != null) {
                modList.reloadFilters();
            }
        }
    }

    private void muto$updateFilterButtons() {
        if (muto$filterAllBtn == null) return;
        MutoModFilter f = MutoModFilter.CURRENT;
        muto$filterAllBtn.visible = (f == MutoModFilter.ALL);
        muto$filterActiveBtn.visible = (f == MutoModFilter.ACTIVE);
        muto$filterDisabledBtn.visible = (f == MutoModFilter.DISABLED);
        muto$filterUnloadedBtn.visible = (f == MutoModFilter.UNLOADED);
    }

    private void muto$updateActionButtons() {
        if (muto$actionDisableBtn == null || muto$actionEnableBtn == null || muto$actionLoadBtn == null || muto$actionRevertBtn == null) return;

        if (this.selected == null || this.selected.getMod() == null) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = false;
            return;
        }

        String modId = this.selected.getMod().getId();
        if (modId == null) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = false;
            return;
        }

        boolean isProtected = ModStateManager.INSTANCE.isProtectedMod(modId);
        boolean isLoaded = ModStateManager.INSTANCE.isModLoaded(modId);
        boolean isDisabled = ModStateManager.INSTANCE.isModDisabled(modId);
        boolean isUnloaded = !isLoaded && !isDisabled;
        boolean isStaged = ModStateManager.INSTANCE.isStaged(modId);

        // Handle configure button for disabled mods: keep visible if mod supports config, but disable it with tooltip
        boolean supportsConfig = this.getModHasConfigScreen(modId);
        if (this.configureButton != null) {
            if (isUnloaded) {
                this.configureButton.visible = false;
            } else if (isDisabled || (isStaged && !ModStateManager.INSTANCE.getStagedState(modId))) {
                if (supportsConfig) {
                    this.configureButton.visible = true;
                    this.configureButton.active = false;
                    this.configureButton.setTooltip(Tooltip.create(Component.literal("Cannot configure disabled mod. Enable the mod first.")));
                } else {
                    this.configureButton.visible = false;
                }
            } else {
                if (supportsConfig) {
                    this.configureButton.visible = true;
                    this.configureButton.active = true;
                    this.configureButton.setTooltip(null);
                } else {
                    this.configureButton.visible = false;
                }
            }
        }

        // Positioning: if configureButton is visible, position action button to its left (-22px)
        // If configureButton is NOT visible, place action button right on the edge in its place!
        int rightEdgeX = (this.configureButton != null) ? this.configureButton.getX() : (this.width - 24);
        int ay = (this.configureButton != null) ? this.configureButton.getY() : 48;
        boolean configShown = (this.configureButton != null && this.configureButton.visible);
        int ax = configShown ? (rightEdgeX - 22) : rightEdgeX;

        muto$actionDisableBtn.setPosition(ax, ay);
        muto$actionEnableBtn.setPosition(ax, ay);
        muto$actionLoadBtn.setPosition(ax, ay);
        muto$actionRevertBtn.setPosition(ax, ay);

        List<String> missing = ModDependencyAnalyzer.getMissing(modId);
        String missingNote = !missing.isEmpty() ? " [Missing: " + String.join(", ", missing) + "]" : "";

        if (isStaged) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = true;
            muto$actionRevertBtn.active = true;

            if (isUnloaded) {
                muto$actionRevertBtn.setTooltip(Tooltip.create(Component.literal("Pending: Load (Click to revert)" + missingNote)));
            } else {
                boolean stagedState = ModStateManager.INSTANCE.getStagedState(modId);
                muto$actionRevertBtn.setTooltip(Tooltip.create(Component.literal(
                        (stagedState ? "Pending: Enable (Click to revert)" : "Pending: Disable (Click to revert)") + missingNote
                )));
            }
        } else if (isUnloaded) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = true;
            muto$actionRevertBtn.visible = false;
            muto$actionLoadBtn.active = true;
            muto$actionLoadBtn.setTooltip(Tooltip.create(Component.literal("Load Mod (Inject into client)" + missingNote)));
        } else if (isDisabled) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = true;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = false;
            muto$actionEnableBtn.active = true;
            muto$actionEnableBtn.setTooltip(Tooltip.create(Component.literal("Enable Mod" + missingNote)));
        } else {
            DiskMod dm = ModStateManager.INSTANCE.findDiskMod(modId);
            boolean isEmbedded = (dm == null);

            muto$actionDisableBtn.visible = true;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = false;

            if (isProtected) {
                muto$actionDisableBtn.active = false;
                muto$actionDisableBtn.setTooltip(Tooltip.create(Component.literal("Protected Mod (Cannot be disabled)")));
            } else if (isEmbedded) {
                muto$actionDisableBtn.active = false;
                muto$actionDisableBtn.setTooltip(Tooltip.create(Component.literal("Embedded Library (Packaged inside another mod). Install standalone jar in /mods to toggle independently.")));
            } else {
                muto$actionDisableBtn.active = true;
                muto$actionDisableBtn.setTooltip(Tooltip.create(Component.literal("Disable Mod")));
            }
        }
    }
}
