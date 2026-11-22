package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreakDoorGoal.class)
public class BreakDoorGoalMixin {


    @Inject(method = "isValidDifficulty", at = @At("HEAD"), cancellable = true)
    private void coraline$alwaysValidDifficulty(Difficulty difficulty, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}