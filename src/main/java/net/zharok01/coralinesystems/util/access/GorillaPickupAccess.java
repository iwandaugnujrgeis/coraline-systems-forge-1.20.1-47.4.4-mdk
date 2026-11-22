package net.zharok01.coralinesystems.util.access;

import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public interface GorillaPickupAccess {

    int POSE_TICKS = 5;

    EntityDataAccessor<Boolean> PICKING_UP = SynchedEntityData.defineId(EntityGorilla.class, EntityDataSerializers.BOOLEAN);

    boolean coraline$isPickingUp();

    void coraline$setPickingUp(boolean pickingUp);

    float coraline$getPickupProgress(float partialTick);
}