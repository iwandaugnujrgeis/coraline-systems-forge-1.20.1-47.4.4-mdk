package net.zharok01.coralinesystems.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MulberryJuiceBottleItem extends AbstractCoralineDrinkItem {

    public MulberryJuiceBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE);
    }

    @Override
    protected int getDrinkColor(ItemStack stack) {
        return 0;
    }

    @Override
    protected boolean shouldSpawnDrinkParticles() {
        return false;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {}
}