package me.fy2ne.muto.menu.gui;

import com.mojang.blaze3d.platform.NativeImage;
import me.fy2ne.muto.mods.DiskMod;
import me.fy2ne.muto.mods.ModStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ModMenuDiskAdapter {
    private ModMenuDiskAdapter() {}

    private static final Map<String, Object> SYNTHETIC_MODS = new HashMap<>();
    private static final Map<String, DiskMod> SYNTHETIC_MOD_DATA = new HashMap<>();
    private static final Map<String, DynamicTexture> ICON_CACHE = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static void syncDiskMods() {
        try {
            Class<?> modMenuCls = Class.forName("com.terraformersmc.modmenu.ModMenu");
            Field modsField = modMenuCls.getDeclaredField("MODS");
            modsField.setAccessible(true);
            Map<String, Object> mods = (Map<String, Object>) modsField.get(null);

            Field rootModsField = modMenuCls.getDeclaredField("ROOT_MODS");
            rootModsField.setAccessible(true);
            Map<String, Object> rootMods = (Map<String, Object>) rootModsField.get(null);

            if (mods == null || rootMods == null) return;

            List<DiskMod> diskMods = ModStateManager.INSTANCE.scanDiskMods();
            Class<?> modInterface = Class.forName("com.terraformersmc.modmenu.util.mod.Mod");

            for (DiskMod diskMod : diskMods) {
                Object existing = mods.get(diskMod.id());
                if (existing != null && !Proxy.isProxyClass(existing.getClass())) {
                    continue;
                }

                boolean aliasPresent = false;
                if (diskMod.aliases() != null) {
                    for (String alias : diskMod.aliases()) {
                        Object aliasObj = mods.get(alias);
                        if (aliasObj != null && !Proxy.isProxyClass(aliasObj.getClass())) {
                            aliasPresent = true;
                            break;
                        }
                    }
                }
                if (aliasPresent) continue;

                DiskMod cached = SYNTHETIC_MOD_DATA.get(diskMod.id());
                if (cached == null || !cached.version().equals(diskMod.version())
                        || !cached.filePath().equals(diskMod.filePath())
                        || !Objects.equals(cached.description(), diskMod.description())) {
                    SYNTHETIC_MODS.remove(diskMod.id());
                    ICON_CACHE.remove(diskMod.id());
                }
                SYNTHETIC_MOD_DATA.put(diskMod.id(), diskMod);

                Object proxy = SYNTHETIC_MODS.computeIfAbsent(diskMod.id(), id -> createModProxy(diskMod, modInterface));
                mods.put(diskMod.id(), proxy);
                rootMods.put(diskMod.id(), proxy);
            }
        } catch (Throwable ignored) {}
    }

    public static void clearModCountCache() {
        try {
            Class<?> modMenuCls = Class.forName("com.terraformersmc.modmenu.ModMenu");
            var m = modMenuCls.getDeclaredMethod("clearModCountCache");
            m.setAccessible(true);
            m.invoke(null);
        } catch (Throwable ignored) {}
    }

    @SuppressWarnings("unchecked")
    public static void applyDiff(me.fy2ne.muto.api.ModDiff diff) {
        if (diff == null) return;
        try {
            Class<?> modMenuCls = Class.forName("com.terraformersmc.modmenu.ModMenu");
            Field modsField = modMenuCls.getDeclaredField("MODS");
            modsField.setAccessible(true);
            Map<String, Object> mods = (Map<String, Object>) modsField.get(null);

            Field rootModsField = modMenuCls.getDeclaredField("ROOT_MODS");
            rootModsField.setAccessible(true);
            Map<String, Object> rootMods = (Map<String, Object>) rootModsField.get(null);

            Field factField = null;
            try {
                factField = modMenuCls.getDeclaredField("configScreenFactories");
                factField.setAccessible(true);
            } catch (Throwable ignored) {}
            Map<String, Object> factories = factField != null ? (Map<String, Object>) factField.get(null) : null;

            if (mods != null && rootMods != null) {
                for (String remId : diff.removed()) {
                    if (factories != null) factories.remove(remId);
                    if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(remId)) {
                        mods.remove(remId);
                        rootMods.remove(remId);
                        SYNTHETIC_MODS.remove(remId);
                        ICON_CACHE.remove(remId);
                    }
                }

                Set<String> readded = new LinkedHashSet<>(diff.added());
                readded.addAll(diff.updated());
                for (String addId : readded) {
                    SYNTHETIC_MODS.remove(addId);
                    ICON_CACHE.remove(addId);
                    var optCont = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(addId);
                    if (optCont.isPresent()) {
                        try {
                            Class<?> fmCls = Class.forName("com.terraformersmc.modmenu.util.mod.fabric.FabricMod");
                            var ctor = fmCls.getConstructor(net.fabricmc.loader.api.ModContainer.class, Set.class);
                            Object fm = ctor.newInstance(optCont.get(), Collections.emptySet());
                            mods.put(addId, fm);
                            rootMods.put(addId, fm);
                        } catch (Throwable t) {
                            mods.remove(addId);
                            rootMods.remove(addId);
                        }
                    }
                }
            }

            clearModCountCache();
            syncDiskMods();
        } catch (Throwable ignored) {}
    }

    public static void refreshModsScreen(net.minecraft.client.gui.screens.Screen screen) {
        if (screen == null) return;
        try {
            Field listField = null;
            for (Field f : screen.getClass().getDeclaredFields()) {
                if ("modList".equals(f.getName()) || f.getType().getName().endsWith("ModListWidget")) {
                    listField = f;
                    break;
                }
            }
            if (listField != null) {
                listField.setAccessible(true);
                Object list = listField.get(screen);
                if (list != null) {
                    var method = list.getClass().getMethod("reloadFilters");
                    method.setAccessible(true);
                    method.invoke(list);
                    return;
                }
            }
        } catch (Throwable ignored) {}

        try {
            screen.init(screen.width, screen.height);
        } catch (Throwable ignored) {}
    }

    private static Object createModProxy(DiskMod diskMod, Class<?> modInterface) {
        String desc = diskMod.description() != null && !diskMod.description().isBlank()
                ? diskMod.description()
                : "";
        Component descComp = Component.literal(desc);

        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            return switch (name) {
                case "getId" -> diskMod.id();
                case "getName", "getTranslatedName" -> diskMod.name();
                case "getVersion" -> diskMod.version();
                case "getPrefixedVersion" -> "v" + diskMod.version();
                case "getDescription", "getTranslatedDescription", "getSummary", "getTranslatedSummary" -> desc;
                case "getFormattedDescription" -> descComp;
                case "isHidden" -> Boolean.FALSE;
                case "isReal" -> Boolean.TRUE;
                case "allowsUpdateChecks", "hasUpdate", "getChildHasUpdate" -> Boolean.FALSE;
                case "getBadges" -> Collections.emptySet();
                case "getLicense" -> diskMod.license() != null ? diskMod.license() : Collections.emptySet();
                case "getAuthors" -> diskMod.authors() != null ? diskMod.authors() : Collections.emptyList();
                case "getContributors" -> Collections.emptyMap();
                case "getCredits" -> Collections.emptySortedMap();
                case "getLinks" -> diskMod.links() != null ? diskMod.links() : Collections.emptyMap();
                case "getWebsite" -> diskMod.links() != null ? diskMod.links().getOrDefault("homepage", "") : "";
                case "getIssueTracker" -> diskMod.links() != null ? diskMod.links().getOrDefault("issues", "") : "";
                case "getSource" -> diskMod.links() != null ? diskMod.links().getOrDefault("sources", "") : "";
                case "getIcon" -> loadModIcon(diskMod);
                case "getParent" -> null;
                case "equals" -> args != null && args.length == 1 && proxy == args[0];
                case "hashCode" -> diskMod.id().hashCode();
                case "toString" -> "DiskModProxy[" + diskMod.id() + "]";
                default -> {
                    Class<?> ret = method.getReturnType();
                    if (ret == boolean.class) yield Boolean.FALSE;
                    if (ret == int.class) yield 0;
                    if (ret == long.class) yield 0L;
                    if (ret == float.class) yield 0.0f;
                    if (ret == double.class) yield 0.0d;
                    if (Set.class.isAssignableFrom(ret)) yield Collections.emptySet();
                    if (List.class.isAssignableFrom(ret)) yield Collections.emptyList();
                    if (Map.class.isAssignableFrom(ret)) yield Collections.emptyMap();
                    yield null;
                }
            };
        };

        return Proxy.newProxyInstance(modInterface.getClassLoader(), new Class<?>[]{modInterface}, handler);
    }

    private static DynamicTexture loadModIcon(DiskMod diskMod) {
        String id = diskMod.id();
        if (ICON_CACHE.containsKey(id)) {
            return ICON_CACHE.get(id);
        }

        DynamicTexture tex = null;
        try {
            if (Files.exists(diskMod.filePath())) {
                try (JarFile jf = new JarFile(diskMod.filePath().toFile())) {
                    JarEntry iconEntry = null;
                    if (diskMod.iconPath() != null && !diskMod.iconPath().isBlank()) {
                        iconEntry = jf.getJarEntry(diskMod.iconPath());
                    }
                    if (iconEntry == null) iconEntry = jf.getJarEntry("assets/" + id + "/icon.png");
                    if (iconEntry == null) iconEntry = jf.getJarEntry("icon.png");

                    if (iconEntry != null) {
                        try (InputStream in = jf.getInputStream(iconEntry)) {
                            NativeImage img = NativeImage.read(in);
                            if (img != null) {
                                tex = new DynamicTexture(() -> "muto_icon_" + id, img);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        if (tex == null) {
            try {
                InputStream in = ModMenuDiskAdapter.class.getClassLoader().getResourceAsStream("assets/modmenu/unknown_icon.png");
                if (in != null) {
                    try (in) {
                        NativeImage img = NativeImage.read(in);
                        if (img != null) {
                            tex = new DynamicTexture(() -> "muto_icon_unknown_" + id, img);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        if (tex == null) {
            NativeImage blank = new NativeImage(16, 16, true);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    blank.setPixel(x, y, 0);
                }
            }
            tex = new DynamicTexture(() -> "muto_icon_blank_" + id, blank);
        }

        ICON_CACHE.put(id, tex);
        return tex;
    }
}
