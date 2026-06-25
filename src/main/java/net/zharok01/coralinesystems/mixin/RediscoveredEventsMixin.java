package net.zharok01.coralinesystems.mixin;

import com.legacy.rediscovered.event.RediscoveredEvents;
import net.minecraft.world.entity.monster.Giant;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RediscoveredEvents.class, remap = false)
public abstract class RediscoveredEventsMixin {

    @Inject(
            method = "onPlayerInteract(Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$RightClickBlock;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void allowSkylandsBeds(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        if (event.getClass().getSimpleName().contains("RightClickBlock")) {
            ci.cancel();
        }
    }

    @Inject(
            method = "onEntityCheckSpawn",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void coraline$stopRediscoveredGiant(MobSpawnEvent.FinalizeSpawn event, CallbackInfo ci) {
        // If the spawning entity is a Giant, we cancel the execution of Rediscovered's event.
        // This stops them from adding the MeleeAttackGoal, while letting Coraline Systems handle the AI natively.
        if (event.getEntity() instanceof Giant) {
            ci.cancel();
        }
    }
}