package net.zharok01.coralinesystems.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.client.color.CoralineItemColors;
import org.jetbrains.annotations.NotNull;

public class DregsBottleItem extends AbstractCoralineDrinkItem {

    public DregsBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE);
    }

    @Override
    protected int getDrinkColor(ItemStack stack) {
        return CoralineItemColors.DREGS_COLOR;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;

        player.addEffect(new MobEffectInstance(
                MobEffects.CONFUSION,
                200
        ));
    }
}