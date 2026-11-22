package net.zharok01.coralinesystems.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.monster.Giant;
import org.jetbrains.annotations.NotNull;

public interface GiantAttackCooldownAccess {
    @NotNull EntityDataAccessor<Boolean> coraline$KICK_ANIMATION_BLOCKED =
            SynchedEntityData.defineId(Giant.class, EntityDataSerializers.BOOLEAN);

    boolean coraline$hasAttackCooldown();

    boolean coraline$isKickAnimationBlocked();

    void coraline$startStompCooldown();

    void coraline$tickAttackCooldown();
}
