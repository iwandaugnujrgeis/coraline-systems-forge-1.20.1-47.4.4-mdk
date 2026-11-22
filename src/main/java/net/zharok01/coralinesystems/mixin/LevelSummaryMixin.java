package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelSettings;
import net.zharok01.coralinesystems.util.CoralineWorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(value = LevelSummary.class, priority = 1001)
public class LevelSummaryMixin {

    @Shadow @Final private LevelSettings settings;
    @Shadow @Final private Path icon;

    @Shadow
    public boolean isLocked() {
        return false;
    }

    @Shadow
    public boolean requiresManualConversion() {
        return false;
    }

    @Shadow
    public boolean isCompatible() {
        return false;
    }

    @Unique private ResourceLocation coraline$worldType;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void coraline$loadWorldType(CallbackInfo ci) {
        this.coraline$worldType = CoralineWorldInfo.coraline$readWorldType(this.icon.resolveSibling("."));
    }

    @Inject(method = "createInfo", at = @At("HEAD"), cancellable = true)
    private void coraline$createInfo(CallbackInfoReturnable<Component> cir) {
        if (this.isLocked() || this.requiresManualConversion() || !this.isCompatible()) {
            return;
        }

        MutableComponent info = Component.empty();
        if (this.coraline$worldType != null) {
            info.append(Component.translatable(this.coraline$worldType.toLanguageKey("generator"))).append(", ");
        }
        info.append(this.settings.difficulty().getDisplayName());
        cir.setReturnValue(info);
    }

    @Inject(method = "isExperimental", at = @At("RETURN"), cancellable = true)
    private void coraline$hideExperimentalWarning(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isLifecycleExperimental", at = @At("RETURN"), cancellable = true, remap = false)
    private void coraline$hideLifecycleExperimentalWarning(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}