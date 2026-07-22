package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.registry.CoralineEffects;

/**
 * Bottle-form Tea. Applies Instant Stamina on drink, with amplifier derived
 * from the brew's strength level (1–5):
 *
 *   Strength 1 → amplifier 0 →  2 stamina points
 *   Strength 2 → amplifier 1 →  4 stamina points
 *   Strength 3 → amplifier 2 →  6 stamina points
 *   Strength 4 → amplifier 3 →  8 stamina points
 *   Strength 5 → amplifier 4 → 10 stamina points
 */
public class TeaBottleItem extends AbstractCoralineDrinkItem {

    public TeaBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE,
                "item.coraline_systems.tea_bottle.strength", ChatFormatting.GREEN);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;

        int strength = CoralineFluidUtils.getStrength(stack);
        int amplifier = strength - 1; // strength 1–5 → amplifier 0–4

        // Duration 1 tick — InstantStaminaEffect.isInstantenous() returns true,
        // so the effect fires applyInstantenousEffect immediately and is never
        // stored as a lingering effect on the player.
        player.addEffect(new MobEffectInstance(
                CoralineEffects.INSTANT_STAMINA.get(),
                1,
                amplifier,
                false,
                false,  // no particles for an instant effect
                false
        ));
    }
}
