package net.zharok01.coralinesystems.util.access;

public interface SealPickupAccess {

    float POSE_TICKS = 6.0F;

    void coraline$setEating(boolean eating);

    float coraline$getPickupProgress(float partialTick);
}