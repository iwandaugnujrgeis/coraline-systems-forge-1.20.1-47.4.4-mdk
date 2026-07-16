package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bucket-form Mulberry Juice, on {@link AbstractCoralineDrinkItem}. No
 * fluid capability attached — see {@link WineBucketItem}'s javadoc for
 * rationale.
 */
public class MulberryJuiceBucketItem extends AbstractCoralineDrinkItem {

    public MulberryJuiceBucketItem(Properties properties) {
        super(properties, Items.BUCKET,
                "item.coraline_systems.mulberry_juice_bucket.strength", ChatFormatting.RED);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 5+): Mulberry Juice's drink effect is not yet designed.
    }
}
