package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bucket-form Tea, on {@link AbstractCoralineDrinkItem}. Same shape as
 * {@link WineBucketItem} — no fluid capability attached, see that class's
 * javadoc for rationale.
 */
public class TeaBucketItem extends AbstractCoralineDrinkItem {

    public TeaBucketItem(Properties properties) {
        super(properties, Items.BUCKET,
                "item.coraline_systems.tea_bucket.strength", ChatFormatting.GREEN);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 5+): Tea's drink effect is not yet designed — see
        // TeaBottleItem's javadoc.
    }
}
