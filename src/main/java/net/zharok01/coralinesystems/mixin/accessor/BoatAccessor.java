package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Boat.class)
public interface BoatAccessor {

    @Accessor("status")
    Boat.Status coraline$getStatus();

    @Accessor("status")
    void coraline$setStatus(Boat.Status status);

    @Accessor("oldStatus")
    Boat.Status coraline$getOldStatus();

    @Accessor("waterLevel")
    double coraline$getWaterLevel();

    @Accessor("waterLevel")
    void coraline$setWaterLevel(double waterLevel);

    @Accessor("lastYd")
    void coraline$setLastYd(double lastYd);

    @Accessor("invFriction")
    float coraline$getInvFriction();

    @Accessor("invFriction")
    void coraline$setInvFriction(float invFriction);

    @Accessor("landFriction")
    float coraline$getLandFriction();

    @Accessor("landFriction")
    void coraline$setLandFriction(float landFriction);

    @Accessor("deltaRotation")
    float coraline$getDeltaRotation();

    @Accessor("deltaRotation")
    void coraline$setDeltaRotation(float deltaRotation);
}