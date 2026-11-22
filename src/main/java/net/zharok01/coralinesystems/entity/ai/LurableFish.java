package net.zharok01.coralinesystems.entity.ai;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;

public interface LurableFish {
    @Nullable
    FishingHook coraline$getLureHook();

    void coraline$setLureHook(@Nullable FishingHook hook);

    void coraline$setExtraSwimAcceleration(double acceleration);

    void coraline$startReel(Entity target);

    boolean coraline$isReeling();
}