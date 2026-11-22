package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import com.legacy.rediscovered.event.RediscoveredEvents;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RediscoveredEvents.class, remap = false)
public abstract class RediscoveredEventsMixin {

    @Inject(
            method = "onPlayerInteract(Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$RightClickBlock;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void allowSkylandsBeds(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "onEntityCheckSpawn",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void coraline$stopRediscoveredGiant(MobSpawnEvent.FinalizeSpawn event, CallbackInfo ci) {
        if (event.getEntity() instanceof Giant) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "onPlayerRightClick(Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$RightClickItem;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;m_36341_()Z", remap = false),
            remap = false
    )
    private static boolean coraline$neverOpenAttachedQuiver(Player player) {
        return false;
    }
}