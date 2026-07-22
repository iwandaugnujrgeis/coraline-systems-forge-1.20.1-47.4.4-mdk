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
 * Bucket-form Tea. Identical Instant Stamina effect to {@link TeaBottleItem}.
 * See that class for amplifier/points mapping.
 */
public class TeaBucketItem extends AbstractCoralineDrinkItem {

    public TeaBucketItem(Properties properties) {
        super(properties, Items.BUCKET,
                "item.coraline_systems.tea_bucket.strength", ChatFormatting.GREEN);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;

        int strength = CoralineFluidUtils.getStrength(stack);
        int amplifier = strength - 1;

        player.addEffect(new MobEffectInstance(
                CoralineEffects.INSTANT_STAMINA.get(),
                1,
                amplifier,
                false,
                false,
                false
        ));
    }
}
