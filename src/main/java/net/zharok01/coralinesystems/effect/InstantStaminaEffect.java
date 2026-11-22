package net.zharok01.coralinesystems.effect;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.mixin.accessor.StaminaDataAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        applyStamina(livingEntity, amplifier);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        applyStamina(livingEntity, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration >= 1;
    }

    private void applyStamina(LivingEntity livingEntity, int amplifier) {
        if (!(livingEntity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        StaminaDataAccessor accessor = (StaminaDataAccessor) StaminaHelper.get(player);

        int durationInTicks = accessor.coraline$getDurationInTicks();
        int rechargeInTicks = accessor.coraline$getRechargeInTicks();
        if (durationInTicks <= 0) return;

        boolean isExhausted = accessor.coraline$getIsExhausted();
        int currentTicks = accessor.coraline$getTickTimer();
        int newTicks;

        if (isExhausted) {
            if (rechargeInTicks <= 0) return;
            newTicks = Math.min(currentTicks + amplifier * 4 * rechargeInTicks / 20, rechargeInTicks);
            accessor.coraline$setTickTimer(newTicks);

            if (newTicks >= rechargeInTicks) {
                accessor.coraline$setIsExhausted(false);
                accessor.coraline$setTickTimer(durationInTicks);
            }
        } else {
            newTicks = Math.min(currentTicks + amplifier * 4 * durationInTicks / 20, durationInTicks);
            accessor.coraline$setTickTimer(newTicks);
        }
    }
}