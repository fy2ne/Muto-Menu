package me.fy2ne.muto.menu.automation;

import me.fy2ne.muto.api.MutoApi;
import me.fy2ne.muto.api.MutoEvents;
import me.fy2ne.muto.menu.MenuMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ModsFolderWatcher {
    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);
    private static final AtomicBoolean COMMITTING_RENAMES = new AtomicBoolean(false);
    private static volatile long suppressUntilMs = 0L;

    private static WatchService watchService;
    private static Thread watcherThread;
    private static boolean eventsRegistered = false;

    private ModsFolderWatcher() {}

    public static synchronized void init() {
        if (!eventsRegistered) {
            eventsRegistered = true;
            MutoEvents.RELOAD_START.register((startMs, diff) -> {
                // keep disk watcher quiet while engine is working
                suppressForMs(10000);
            });
            MutoEvents.RELOAD_FINISH.register((endMs, diff, ok, ms, err) -> {
                // drain events generated during reload and allow fs to settle
                suppressForMs(1500);
                drainPending();
            });
        }
    }

    public static synchronized void setEnabled(boolean enabled) {
        ENABLED.set(enabled);
        if (enabled) {
            init();
            ensureRunning();
        }
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static void suppressForMs(long ms) {
        long target = System.currentTimeMillis() + ms;
        if (target > suppressUntilMs) {
            suppressUntilMs = target;
        }
    }

    public static void setCommittingRenames(boolean active) {
        COMMITTING_RENAMES.set(active);
    }

    public static boolean isCommittingRenames() {
        return COMMITTING_RENAMES.get();
    }

    public static void drainPending() {
        WatchService ws = watchService;
        if (ws == null) return;
        try {
            WatchKey k;
            while ((k = ws.poll()) != null) {
                k.pollEvents();
                k.reset();
            }
        } catch (Throwable ignored) {}
    }

    private static synchronized void ensureRunning() {
        if (watcherThread != null && watcherThread.isAlive()) return;

        try {
            Path gameDir;
            try {
                var loader = FabricLoader.getInstance();
                gameDir = (loader != null && loader.getGameDir() != null) ? loader.getGameDir() : Path.of(".");
            } catch (Throwable t) {
                gameDir = Path.of(".");
            }
            Path modsDir = gameDir.resolve("mods");
            if (!Files.exists(modsDir)) {
                Files.createDirectories(modsDir);
            }

            watchService = modsDir.getFileSystem().newWatchService();
            modsDir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );

            watcherThread = new Thread(ModsFolderWatcher::pollLoop, "muto-mods-watcher");
            watcherThread.setDaemon(true);
            watcherThread.start();
        } catch (IOException e) {
            MenuMod.LOG.warn("failed to bind mods folder watch service: {}", e.getMessage());
        }
    }

    private static void pollLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }

            if (key == null) continue;

            boolean relevant = false;
            for (WatchEvent<?> ev : key.pollEvents()) {
                Object ctx = ev.context();
                if (ctx instanceof Path p) {
                    String fn = p.getFileName().toString();
                    if (fn.endsWith(".jar") || fn.endsWith(".jar.disabled")) {
                        relevant = true;
                    }
                }
            }
            key.reset();

            if (!relevant || !ENABLED.get()) continue;

            // wait 750ms for multi-file bursts or slow copies to finish writing
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                break;
            }

            // drain leftover events from the burst
            try {
                WatchKey next;
                while ((next = watchService.poll(300, TimeUnit.MILLISECONDS)) != null) {
                    next.pollEvents();
                    next.reset();
                }
            } catch (InterruptedException e) {
                break;
            }

            if (!ENABLED.get()) continue;
            if (System.currentTimeMillis() < suppressUntilMs) continue;
            if (COMMITTING_RENAMES.get()) continue;
            if (MutoApi.isReloading()) continue;

            if (!MutoApi.isSafeToReload()) {
                MenuMod.LOG.info("mods dir altered, but client not in safe reload state");
                continue;
            }

            MenuMod.LOG.info("mods dir altered, launching auto-reload");
            MutoApi.reloadAsync();
        }
    }
}
