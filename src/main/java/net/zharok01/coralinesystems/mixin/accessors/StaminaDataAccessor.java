package net.zharok01.coralinesystems.mixin.accessors;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the private fields of NT's {@link StaminaData} so that our drink
 * effects can read and write stamina state without NT needing to expose a
 * public API surface.
 *
 * BLIND SPOT: These field names match the decompiled IntelliJ view of the NT
 * jar. If NT updates and renames its fields this accessor will break at
 * runtime with a MixinException. Check NT's changelog on any update.
 */
@Mixin(value = StaminaData.class, remap = false)
public interface StaminaDataAccessor {

    // ── Read ─────────────────────────────────────────────────────────────────

    @Accessor("tickTimer")
    int coraline$getTickTimer();

    @Accessor("durationInTicks")
    int coraline$getDurationInTicks();

    @Accessor("rechargeInTicks")
    int coraline$getRechargeInTicks();

    @Accessor("isExhausted")
    boolean coraline$getIsExhausted();

    // ── Write ────────────────────────────────────────────────────────────────

    @Accessor("tickTimer")
    void coraline$setTickTimer(int value);

    @Accessor("isExhausted")
    void coraline$setIsExhausted(boolean value);
}
