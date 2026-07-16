package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bottle-form Mulberry Juice — the unfermented, pre-Yeast collectible for
 * the Wine branch (mirrors Tea's pre-culture collectibility on the
 * Kombucha branch, per Session 1.7's precedent). Strength (1-5) is carried
 * via {@link CoralineFluidUtils}, same as Wine/Tea.
 * <p>
 * Drink effect undesigned — parked alongside Tea's, no real fermentation
 * or culture step applies to this drink at all (drawing it early, before
 * Yeast, is what keeps it "juice" rather than "wine").
 */
public class MulberryJuiceBottleItem extends AbstractCoralineDrinkItem {

    public MulberryJuiceBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE,
                "item.coraline_systems.mulberry_juice_bottle.strength", ChatFormatting.RED);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 5+): Mulberry Juice's drink effect is not yet
        // designed at all.
    }
}
