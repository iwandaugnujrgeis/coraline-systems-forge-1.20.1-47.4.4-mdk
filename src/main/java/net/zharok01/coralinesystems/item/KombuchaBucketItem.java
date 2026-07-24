package net.zharok01.coralinesystems.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bucket-form Kombucha. Identical respawn-point effect to
 * {@link KombuchaBottleItem} — see that class for details.
 * No fluid capability attached (see {@link WineBucketItem}'s javadoc).
 */
public class KombuchaBucketItem extends AbstractCoralineDrinkItem {

    public KombuchaBucketItem(Properties properties) {
        super(properties, Items.BUCKET);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof ServerPlayer serverPlayer)) return;

        serverPlayer.setRespawnPosition(
                level.dimension(),
                serverPlayer.blockPosition(),
                serverPlayer.getYRot(),
                true,
                true
        );
    }
}