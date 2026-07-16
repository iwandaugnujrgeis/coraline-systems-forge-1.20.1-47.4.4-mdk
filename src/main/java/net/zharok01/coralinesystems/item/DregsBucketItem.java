package net.zharok01.coralinesystems.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bucket-form Dregs, on {@link AbstractCoralineDrinkItem}. Same shape as
 * {@link DregsBottleItem} — see that class's javadoc for the drinkability
 * open question. No fluid capability attached — see
 * {@link WineBucketItem}'s javadoc for that rationale.
 */
public class DregsBucketItem extends AbstractCoralineDrinkItem {

    public DregsBucketItem(Properties properties) {
        super(properties, Items.BUCKET);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 4): decide + implement Dregs' drink effect (or lack
        // thereof) — see DregsBottleItem's javadoc.
    }
}
