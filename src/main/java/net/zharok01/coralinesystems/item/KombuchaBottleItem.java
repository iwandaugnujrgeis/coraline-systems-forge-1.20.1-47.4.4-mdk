package net.zharok01.coralinesystems.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bottle-form Kombucha, on {@link AbstractCoralineDrinkItem}. Single-strength
 * per the design doc — passes no strength translation key to the base
 * class, so no strength tooltip line is added and
 * {@link CoralineFluidUtils} is never invoked for this item.
 * <p>
 * The respawn-point-setting drink effect (design doc Section 4,
 * "Collection") is Session 4's job — see {@link #applyDrinkEffect}.
 */
public class KombuchaBottleItem extends AbstractCoralineDrinkItem {

    public KombuchaBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 4): set the player's respawn point at their current
        // position on drink, per design doc Section 4 ("Effect: sets the
        // player's respawn point at the location where the Kombucha was
        // drunk"). Fallback-on-obstruction behavior inherits vanilla
        // bed/anchor logic wholesale per the design doc.
    }
}
