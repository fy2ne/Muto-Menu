package me.fy2ne.muto.menu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import me.fy2ne.muto.mods.ModStateManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.gui.widget.entries.ModListEntry")
public abstract class ModListEntryMixin extends ObjectSelectionList.Entry<ModListEntry> {

    @Shadow public abstract Mod getMod();
    @Shadow public abstract int getXOffset();
    @Shadow public abstract int getYOffset();

    @WrapOperation(
            method = "extractContent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;white(F)I"),
            require = 0
    )
    private int muto$dimIconAlpha(float alpha, Operation<Integer> orig) {
        Mod mod = getMod();
        if (mod != null && mod.getId() != null && ModStateManager.INSTANCE.isModDisabled(mod.getId())) {
            return orig.call(0.38F);
        }
        return orig.call(alpha);
    }

    @Inject(method = "extractContent", at = @At("RETURN"), require = 0)
    private void muto$dimDisabled(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovered, float delta, CallbackInfo ci) {
        Mod mod = getMod();
        if (mod == null) return;
        String modId = mod.getId();
        if (modId == null) return;
        if (!ModStateManager.INSTANCE.isModDisabled(modId)) return;

        int x = this.getX() + getXOffset();
        int y = this.getContentY() + getYOffset();
        int w = this.getContentWidth();
        int h = Math.max(32, this.getContentHeight());

        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x780A0A0E);
    }
}
