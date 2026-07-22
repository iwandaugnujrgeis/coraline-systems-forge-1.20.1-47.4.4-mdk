package net.zharok01.coralinesystems.effect;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaData;
import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.mixin.accessors.StaminaDataAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Instant Stamina — an instantaneous beneficial MobEffect applied when the
 * player drinks Tea or Mulberry Juice.
 *
 * Replenishment formula: {@code (amplifier + 1) * 2} stamina points.
 *   - Amplifier 0 →  2 points  (Mulberry Juice all strengths, Tea strength 1)
 *   - Amplifier 1 →  4 points  (Tea strength 2)
 *   - Amplifier 2 →  6 points  (Tea strength 3)
 *   - Amplifier 3 →  8 points  (Tea strength 4)
 *   - Amplifier 4 → 10 points  (Tea strength 5)
 *
 * Stamina points map 0–20, matching NT's staminaLevel scale.
 * Each "point" corresponds to tickTimer / durationInTicks * 20, so we
 * convert points back to ticks: ticksToAdd = points * durationInTicks / 20.
 *
 * Color: 0x4CAF50 — leafy green, matching the Tea aesthetic.
 */
public class InstantStaminaEffect extends MobEffect {

    public InstantStaminaEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4CAF50);
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource,
                                        @NotNull LivingEntity livingEntity, int amplifier, double health) {
        if (!(livingEntity instanceof Player player)) return;

        // NT stamina is only tracked server-side (or integrated server in SP).
        // Applying it client-side would be a no-op or double-apply, so guard here.
        if (player.level().isClientSide()) return;

        StaminaData data = StaminaHelper.get(player);
        StaminaDataAccessor accessor = (StaminaDataAccessor) data;

        int durationInTicks = accessor.coraline$getDurationInTicks();

        // Guard: if NT hasn't initialised duration yet (config not loaded),
        // durationInTicks would be 0 — skip to avoid divide-by-zero.
        if (durationInTicks <= 0) return;

        // Convert stamina points to ticks proportionally.
        int staminaPoints = (amplifier + 1) * 2;
        int ticksToAdd = staminaPoints * durationInTicks / 20;

        int currentTicks = accessor.coraline$getTickTimer();
        int newTicks = Math.min(currentTicks + ticksToAdd, durationInTicks);
        accessor.coraline$setTickTimer(newTicks);

        // If the player was exhausted and we've now pushed tickTimer above the
        // recharge threshold, clear exhaustion so sprinting becomes available again.
        if (accessor.coraline$getIsExhausted()
                && newTicks >= accessor.coraline$getRechargeInTicks()) {
            accessor.coraline$setIsExhausted(false);
        }
    }
}
