package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelSummary.class, priority = 1001)
public class LevelSummaryMixin {

    /**
     * @author Coraline Systems
     * @reason Overrides the experimental flag to false, hiding the "Experimental" world warning banner.
     */
    @Inject(method = "isExperimental", at = @At("RETURN"), cancellable = true)
    private void coraline_systems$hideExperimentalWarning(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}