package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.zharok01.coralinesystems.entity.ObsidianBoatEntity;
import net.zharok01.coralinesystems.mixin.accessor.BoatAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ObsidianBoatPassengerMixin {

    @Inject(method = "lavaHurt", at = @At("HEAD"), cancellable = true)
    private void coraline$cancelLavaHurtInBoat(CallbackInfo ci) {
        if (coraline$isSafeBoatPassenger()) {
            ci.cancel();
        }
    }

    @Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
    private void coraline$suppressFireOverlayInBoat(CallbackInfoReturnable<Boolean> cir) {
        if (coraline$isSafeBoatPassenger()) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean coraline$isSafeBoatPassenger() {
        Entity self = (Entity) (Object) this;
        if (!(self.getVehicle() instanceof ObsidianBoatEntity boat)) {
            return false;
        }
        Boat.Status status = ((BoatAccessor) boat).coraline$getStatus();
        return status != Boat.Status.UNDER_WATER && status != Boat.Status.UNDER_FLOWING_WATER;
    }
}