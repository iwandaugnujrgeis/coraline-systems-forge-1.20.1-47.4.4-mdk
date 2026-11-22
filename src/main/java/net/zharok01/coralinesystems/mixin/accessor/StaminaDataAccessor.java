package net.zharok01.coralinesystems.mixin.accessor;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StaminaData.class, remap = false)
public interface StaminaDataAccessor {

    @Accessor("tickTimer")
    int coraline$getTickTimer();

    @Accessor("durationInTicks")
    int coraline$getDurationInTicks();

    @Accessor("rechargeInTicks")
    int coraline$getRechargeInTicks();

    @Accessor("isExhausted")
    boolean coraline$getIsExhausted();

    @Accessor("tickTimer")
    void coraline$setTickTimer(int value);

    @Accessor("isExhausted")
    void coraline$setIsExhausted(boolean value);
}
