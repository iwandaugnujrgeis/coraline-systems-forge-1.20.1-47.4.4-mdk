package net.zharok01.coralinesystems.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.vehicle.Boat;
import net.zharok01.coralinesystems.mixin.accessor.BoatAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatMovementSoundMixin {

    @Unique
    private static final double coraline$MOVEMENT_SPEED_THRESHOLD_SQUARED = 0.0064D;

    @Unique
    private static final int coraline$SOUND_INTERVAL_TICKS = 20;

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$playSoftWaterMovementSound(CallbackInfo ci) {
        Boat boat = (Boat) (Object) this;
        if (boat.level().isClientSide || boat.isSilent() || boat.tickCount % coraline$SOUND_INTERVAL_TICKS != 0) {
            return;
        }

        Boat.Status status = ((BoatAccessor) boat).coraline$getStatus();
        if ((status != Boat.Status.IN_WATER && status != Boat.Status.UNDER_WATER
                && status != Boat.Status.UNDER_FLOWING_WATER)
                || boat.getDeltaMovement().horizontalDistanceSqr() <= coraline$MOVEMENT_SPEED_THRESHOLD_SQUARED) {
            return;
        }

        boat.level().playSound(null, boat, SoundEvents.GENERIC_SWIM, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }
}
