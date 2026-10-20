package me.fy2ne.muto.menu.plugin;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.fy2ne.muto.menu.gui.MutoConfigScreen;

public final class MutoModMenuPlugin implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MutoConfigScreen::new;
    }
}
