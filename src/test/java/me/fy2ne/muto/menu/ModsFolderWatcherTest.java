package me.fy2ne.muto.menu;

import me.fy2ne.muto.menu.automation.ModsFolderWatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModsFolderWatcherTest {

    @Test
    void testRenameMutexActive() {
        assertFalse(ModsFolderWatcher.isCommittingRenames());
        try {
            ModsFolderWatcher.setCommittingRenames(true);
            assertTrue(ModsFolderWatcher.isCommittingRenames());
        } finally {
            ModsFolderWatcher.setCommittingRenames(false);
        }
        assertFalse(ModsFolderWatcher.isCommittingRenames());
    }

    @Test
    void testSuppressionWindow() {
        assertDoesNotThrow(() -> ModsFolderWatcher.suppressForMs(5000));
        assertDoesNotThrow(() -> ModsFolderWatcher.suppressForMs(-100));
    }

    @Test
    void testDrainPendingSafeWithoutWatchService() {
        assertDoesNotThrow(ModsFolderWatcher::drainPending);
    }

    @Test
    void testMenuConfigWatchBinding() {
        MenuConfig cfg = MenuConfig.get();
        boolean prev = cfg.autoWatchFolder;
        try {
            cfg.autoWatchFolder = true;
            MenuConfig.syncToRuntime();
            assertTrue(ModsFolderWatcher.isEnabled());

            cfg.autoWatchFolder = false;
            MenuConfig.syncToRuntime();
            assertFalse(ModsFolderWatcher.isEnabled());
        } finally {
            cfg.autoWatchFolder = prev;
            MenuConfig.syncToRuntime();
        }
    }
}
