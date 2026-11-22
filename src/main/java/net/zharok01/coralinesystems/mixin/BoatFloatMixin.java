package net.zharok01.coralinesystems.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.ObsidianBoatEntity;
import net.zharok01.coralinesystems.mixin.accessor.BoatAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Boat.class)
public abstract class BoatFloatMixin {

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;floatBoat()V")
    )
    private void coraline$wrapFloatBoat(Boat instance, Operation<Void> original) {
        if (instance instanceof ObsidianBoatEntity obsidianBoat) {
            coraline$lavaFloatBoat(obsidianBoat);
        } else {
            original.call(instance);
        }
    }

    @Unique
    private static void coraline$lavaFloatBoat(ObsidianBoatEntity boat) {
        BoatAccessor acc = (BoatAccessor) boat;
        double gravity = boat.isNoGravity() ? 0.0D : -0.04D;
        double lift = 0.0D;
        acc.coraline$setInvFriction(0.05F);

        Boat.Status status = acc.coraline$getStatus();
        Boat.Status oldStatus = acc.coraline$getOldStatus();

        if (oldStatus == Boat.Status.IN_AIR && status != Boat.Status.IN_AIR && status != Boat.Status.ON_LAND) {
            acc.coraline$setWaterLevel(boat.getY(1.0D));
            boat.setPos(boat.getX(), (double) (boat.getWaterLevelAbove() - boat.getBbHeight()) + 0.101D, boat.getZ());
            boat.setDeltaMovement(boat.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
            acc.coraline$setLastYd(0.0D);
            acc.coraline$setStatus(Boat.Status.IN_WATER);
        } else {
            if (status == Boat.Status.IN_WATER) {
                lift = (acc.coraline$getWaterLevel() - boat.getY()) / (double) boat.getBbHeight()
                        + (coraline$hasEntityStandingOnTop(boat) ? 0.0D : 0.35D);
                acc.coraline$setInvFriction(0.9F);
            } else if (status == Boat.Status.UNDER_FLOWING_WATER) {
                gravity = -7.0E-4D;
                acc.coraline$setInvFriction(0.9F);
            } else if (status == Boat.Status.UNDER_WATER) {
                lift = 0.01D;
                acc.coraline$setInvFriction(0.45F);
            } else if (status == Boat.Status.IN_AIR) {
                acc.coraline$setInvFriction(0.9F);
            } else if (status == Boat.Status.ON_LAND) {
                acc.coraline$setInvFriction(acc.coraline$getLandFriction());
                if (boat.getControllingPassenger() instanceof Player) {
                    acc.coraline$setLandFriction(acc.coraline$getLandFriction() / 2.0F);
                }
            }

            Vec3 movement = boat.getDeltaMovement();
            float invFriction = acc.coraline$getInvFriction();
            boat.setDeltaMovement(movement.x * invFriction, movement.y + gravity, movement.z * invFriction);
            acc.coraline$setDeltaRotation(acc.coraline$getDeltaRotation() * invFriction);
            if (lift > 0.0D) {
                Vec3 lifted = boat.getDeltaMovement();
                boat.setDeltaMovement(lifted.x, (lifted.y + lift * 0.06153846016296973D) * 0.75D, lifted.z);
            }
        }
    }

    @Unique
    private static boolean coraline$hasEntityStandingOnTop(Boat boat) {
        double boatTop = boat.getBoundingBox().maxY;
        return !boat.level().getEntities(boat, boat.getBoundingBox().inflate(0.0D, 0.2D, 0.0D),
                        entity -> entity instanceof LivingEntity
                                && entity.getBoundingBox().minY >= boatTop - 1.0E-3D
                                && entity.getBoundingBox().minY <= boatTop + 0.2D)
                .isEmpty();
    }
}