package me.fy2ne.muto.menu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.util.mod.fabric.FabricMod")
public abstract class FabricModMixin {

    @Shadow
    protected ModContainer container;

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
}
