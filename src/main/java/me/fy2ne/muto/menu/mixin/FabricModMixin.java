package me.fy2ne.muto.menu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fy2ne.muto.mods.DiskMod;
import me.fy2ne.muto.mods.ModStateManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.util.mod.fabric.FabricMod")
public abstract class FabricModMixin {

    @Shadow
    protected ModContainer container;

    @Shadow
    public abstract String getId();

    @WrapOperation(
            method = "getIcon",
            at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/api/FabricLoader;getModContainer(Ljava/lang/String;)Ljava/util/Optional;"),
            require = 0
    )
    private Optional<ModContainer> muto$safeGetModContainer(FabricLoader loader, String id, Operation<Optional<ModContainer>> original) {
        Optional<ModContainer> opt = original.call(loader, id);
        if (opt.isPresent()) {
            return opt;
        }
        if (this.container != null) {
            return Optional.of(this.container);
        }
        return loader.getModContainer("modmenu");
    }

    @Inject(method = {"getDescription", "getTranslatedDescription"}, at = @At("RETURN"), cancellable = true, require = 0)
    private void muto$ensureRealDescription(CallbackInfoReturnable<String> cir) {
        String val = cir.getReturnValue();
        if (val == null || val.isBlank() || val.startsWith("Disk mod:") || val.equals("Dynamically loaded by Muto")) {
            DiskMod dm = ModStateManager.INSTANCE.findDiskMod(getId());
            if (dm != null && dm.description() != null && !dm.description().isBlank()) {
                cir.setReturnValue(dm.description());
            } else if (val != null && (val.startsWith("Disk mod:") || val.equals("Dynamically loaded by Muto"))) {
                cir.setReturnValue("");
            }
        }
    }
}
