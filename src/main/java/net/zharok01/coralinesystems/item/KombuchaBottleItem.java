package net.zharok01.coralinesystems.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.client.color.CoralineItemColors;

public class KombuchaBottleItem extends AbstractCoralineDrinkItem {

    public KombuchaBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE);
    }

    @Override
    protected int getDrinkColor(ItemStack stack) {
        return CoralineItemColors.KOMBUCHA_COLOR;
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof ServerPlayer serverPlayer)) return;

        serverPlayer.setRespawnPosition(
                level.dimension(),
                serverPlayer.blockPosition(),
                serverPlayer.getYRot(),
                true,
                false
        );
    }
}