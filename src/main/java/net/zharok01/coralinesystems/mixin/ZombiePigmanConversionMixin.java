package net.zharok01.coralinesystems.mixin;

import com.legacy.rediscovered.entity.ZombiePigmanEntity;
import com.legacy.rediscovered.entity.util.IPigman.Type;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Mixin to fix the AI "Zombie Stare" after curing a Zombie Pigman.
 * Targets the Rediscovered mod's ZombiePigmanEntity.
 */
@Mixin(value = ZombiePigmanEntity.class, remap = false)
public abstract class ZombiePigmanConversionMixin {

    /**
     * Injects into the finishCure method right before the Forge event is fired.
     * We capture the local 'pigman' variable to wake up its AI.
     */
    @Inject(
            method = "finishCure(Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/ForgeEventFactory;onLivingConvert(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void coraline$wakeUpCuredPigman(ServerLevel level, CallbackInfo ci, byte typeId, Type wow, Mob pigman) {
        // Resetting noActionTime is the technical equivalent of "punching" the mob to wake it up.
        // This forces the Minecraft GoalSelector to re-scan for tasks (like finding a job).
        pigman.setNoActionTime(0);

        // Clear any leftover pathfinding from the zombie state.
        pigman.getNavigation().stop();

        // Note: As seen in the source, PigmanData and XP are already
        // transferred by the mod before this point.
    }
}