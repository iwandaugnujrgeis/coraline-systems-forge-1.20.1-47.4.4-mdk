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
 * Bucket-form Tea. Restores 3× the stamina points of {@link TeaBottleItem}
 * at the same strength level, since a bucket holds 3 bottles' worth of liquid.
 *
 *   Strength 1 (Bland)  →  6 stamina points
 *   Strength 2 (Mild)   →  9 stamina points
 *   Strength 3 (Decent) → 12 stamina points
 *   Strength 4 (Strong) → 15 stamina points
 *   Strength 5 (Fiery)  → 18 stamina points
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
        int staminaPoints = (strength + 1) * 3; // 3× the bottle value

        player.addEffect(new MobEffectInstance(
                CoralineEffects.INSTANT_STAMINA.get(),
                1,
                staminaPoints,
                false,
                false,
                false
        ));
    }
}