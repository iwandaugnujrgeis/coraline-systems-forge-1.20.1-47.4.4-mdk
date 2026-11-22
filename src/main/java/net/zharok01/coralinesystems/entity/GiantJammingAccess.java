package net.zharok01.coralinesystems.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.monster.Giant;
import org.jetbrains.annotations.NotNull;

public interface GiantJammingAccess {
    @NotNull EntityDataAccessor<Boolean> coraline$IS_JAMMING =
            SynchedEntityData.defineId(Giant.class, EntityDataSerializers.BOOLEAN);

    boolean coraline$isJamming();

    void coraline$setDancingDuration(int duration);

    void coraline$tickMusicJamming();
}
