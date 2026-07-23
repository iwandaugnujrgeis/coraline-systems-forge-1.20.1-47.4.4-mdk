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
 * Bottle-form Wine. Applies the Persistence effect on drink, scaling
 * duration with the brew's strength level (1–5).
 *
 * Duration formula: strength * 60 seconds (in ticks: strength * 1200).
 *   Strength 1 →  60 s  (1 200 ticks)
 *   Strength 2 → 120 s  (2 400 ticks)
 *   Strength 3 → 180 s  (3 600 ticks)
 *   Strength 4 → 240 s  (4 800 ticks)
 *   Strength 5 → 300 s  (6 000 ticks)
 *
 * ASSUMPTION: Duration formula (60 s per strength level) is a reasonable
 * starting point—adjust the SECONDS_PER_STRENGTH constant to tune it.
 */
public class WineBottleItem extends AbstractCoralineDrinkItem {

    /** Ticks of Persistence granted per strength level. 20 ticks = 1 second. */
    private static final int TICKS_PER_STRENGTH = 20 * 20;

    public WineBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE,
                "item.coraline_systems.wine_bottle.strength", ChatFormatting.DARK_PURPLE);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;

        int strength = CoralineFluidUtils.getStrength(stack);
        int duration = strength * TICKS_PER_STRENGTH;

        player.addEffect(new MobEffectInstance(
                CoralineEffects.PERSISTENCE.get(),
                duration,
                0,          // amplifier — Persistence is single-tier
                false,      // not ambient (no reduced particles)
                true,       // show particles
                true        // show icon
        ));
    }
}
