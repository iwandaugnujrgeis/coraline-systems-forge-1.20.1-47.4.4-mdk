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

public class InstantStaminaEffect extends MobEffect {

    public InstantStaminaEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4CAF50);
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    // ── CORALINE: Fixed Application Logic ────────────────────────────────────

    // Called when the effect is applied via splash potions / direct commands
    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource,
                                        @NotNull LivingEntity livingEntity, int amplifier, double health) {
        applyStamina(livingEntity, amplifier);
    }

    // Called when applied via FoodProperties (eating/drinking items)
    @Override
    public void applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        applyStamina(livingEntity, amplifier);
    }

    // Tells Minecraft that this effect should fire its logic immediately
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration >= 1;
    }

    private void applyStamina(LivingEntity livingEntity, int amplifier) {
        if (!(livingEntity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        StaminaData data = StaminaHelper.get(player);
        StaminaDataAccessor accessor = (StaminaDataAccessor) data;

        int durationInTicks = accessor.coraline$getDurationInTicks();
        int rechargeInTicks = accessor.coraline$getRechargeInTicks();
        if (durationInTicks <= 0) return;

        boolean isExhausted = accessor.coraline$getIsExhausted();
        int staminaPoints = (amplifier + 1) * 2;
        int currentTicks = accessor.coraline$getTickTimer();
        int newTicks;

        // If exhausted, tickTimer is counting up to rechargeInTicks
        if (isExhausted) {
            if (rechargeInTicks <= 0) return;
            int ticksToAdd = staminaPoints * rechargeInTicks / 20;
            newTicks = Math.min(currentTicks + ticksToAdd, rechargeInTicks);
            accessor.coraline$setTickTimer(newTicks);

            // If we pushed them past the recharge threshold, instantly clear exhaustion
            if (newTicks >= rechargeInTicks) {
                accessor.coraline$setIsExhausted(false);
                accessor.coraline$setTickTimer(durationInTicks); // Reset pool for sprinting
            }
        }
        // If normal, tickTimer is counting down from durationInTicks
        else {
            int ticksToAdd = staminaPoints * durationInTicks / 20;
            newTicks = Math.min(currentTicks + ticksToAdd, durationInTicks);
            accessor.coraline$setTickTimer(newTicks);
        }
    }
}