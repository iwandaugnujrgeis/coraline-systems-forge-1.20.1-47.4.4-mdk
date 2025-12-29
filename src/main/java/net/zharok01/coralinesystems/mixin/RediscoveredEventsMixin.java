package net.zharok01.coralinesystems.mixin;

import com.legacy.rediscovered.event.RediscoveredEvents;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RediscoveredEvents.class, remap = false)
public abstract class RediscoveredEventsMixin {

    @Inject(
            method = "Lcom/legacy/rediscovered/event/RediscoveredEvents;onPlayerInteract(Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$RightClickBlock;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void allowSkylandsBeds(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        if (event.getClass().getSimpleName().contains("RightClickBlock")) {
            ci.cancel();
        }
    }
}