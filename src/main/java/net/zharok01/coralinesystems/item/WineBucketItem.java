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
 * Bucket-form Wine. Identical Persistence effect to {@link WineBottleItem}.
 * See that class for duration formula details.
 */
public class WineBucketItem extends AbstractCoralineDrinkItem {

    private static final int TICKS_PER_STRENGTH = 60 * 20;

    public WineBucketItem(Properties properties) {
        super(properties, Items.BUCKET,
                "item.coraline_systems.wine_bucket.strength", ChatFormatting.DARK_PURPLE);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;

        int strength = CoralineFluidUtils.getStrength(stack);
        int duration = strength * TICKS_PER_STRENGTH;

        player.addEffect(new MobEffectInstance(
                CoralineEffects.PERSISTENCE.get(),
                duration,
                0,
                false,
                true,
                true
        ));
    }
}
