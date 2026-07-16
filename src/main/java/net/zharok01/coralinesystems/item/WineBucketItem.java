package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bucket-form Wine, on {@link AbstractCoralineDrinkItem}. Deliberately no
 * {@code initCapabilities}/{@code FluidBucketWrapper} override — see the
 * roadmap handoff Section 1d.1/1d.1b for the full rationale (no backing
 * {@code Fluid} is registered for any of these four substances, so
 * attaching the capability would be a likely NPE/undefined-behavior
 * source, not a nice-to-have being skipped for convenience).
 */
public class WineBucketItem extends AbstractCoralineDrinkItem {

    public WineBucketItem(Properties properties) {
        super(properties, Items.BUCKET,
                "item.coraline_systems.wine_bucket.strength", ChatFormatting.DARK_PURPLE);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 4): same level-tied drink effect dispatch as
        // WineBottleItem — see that class's javadoc.
    }
}
