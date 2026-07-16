package net.zharok01.coralinesystems.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bucket-form Kombucha, on {@link AbstractCoralineDrinkItem}. Single-strength,
 * no fluid capability attached — see {@link WineBucketItem}'s javadoc for
 * the capability rationale.
 */
public class KombuchaBucketItem extends AbstractCoralineDrinkItem {

    public KombuchaBucketItem(Properties properties) {
        super(properties, Items.BUCKET);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 4): respawn-point-set drink effect — see
        // KombuchaBottleItem's javadoc for the same TODO.
    }
}
