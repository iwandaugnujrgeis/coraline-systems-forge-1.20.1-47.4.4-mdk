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
 * Bucket-form Mulberry Juice. Identical Instant Stamina effect to
 * {@link MulberryJuiceBottleItem} — fixed 4 stamina points regardless of
 * strength.
 */
public class MulberryJuiceBucketItem extends AbstractCoralineDrinkItem {

    /** Restores 4 stamina points (2 full icons). Amplifier IS the point value. */
    private static final int FIXED_AMPLIFIER = 4;

    public MulberryJuiceBucketItem(Properties properties) {
        super(properties, Items.BUCKET,
                "item.coraline_systems.mulberry_juice_bucket.strength", ChatFormatting.RED);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;

        player.addEffect(new MobEffectInstance(
                CoralineEffects.INSTANT_STAMINA.get(),
                1,
                FIXED_AMPLIFIER,
                false,
                false,
                false
        ));
    }
}