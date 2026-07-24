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
 * Bottle-form Tea. Applies Instant Stamina on drink, with stamina points
 * scaling with the brew's strength level (1–5). The amplifier passed to
 * {@link net.zharok01.coralinesystems.effect.InstantStaminaEffect} IS the
 * number of stamina points to restore directly.
 *
 *   Strength 1 (Bland)  → 2 stamina points  (1 full icon)
 *   Strength 2 (Mild)   → 3 stamina points
 *   Strength 3 (Decent) → 4 stamina points  (2 full icons)
 *   Strength 4 (Strong) → 5 stamina points
 *   Strength 5 (Fiery)  → 6 stamina points  (3 full icons)
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
        // strength 1–5 → stamina points 2–6 (one point added per tier above the base of 1)
        int staminaPoints = strength + 1;

        player.addEffect(new MobEffectInstance(
                CoralineEffects.INSTANT_STAMINA.get(),
                1,
                staminaPoints, // amplifier = stamina points (see InstantStaminaEffect)
                false,
                false,
                false
        ));
    }
}