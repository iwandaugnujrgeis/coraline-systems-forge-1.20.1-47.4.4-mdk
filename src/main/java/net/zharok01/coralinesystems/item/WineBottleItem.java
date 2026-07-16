package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bottle-form Wine, on {@link AbstractCoralineDrinkItem}. Strength (1-5,
 * the solid-ingredient level at time of collection) is carried via
 * {@link CoralineFluidUtils} and surfaced automatically in the tooltip by
 * the base class.
 * <p>
 * Drink *effects* are still explicitly out of scope — {@link #applyDrinkEffect}
 * is a TODO for Session 4, keyed off {@code CoralineFluidUtils.getStrength(stack)}.
 * Effect design isn't finalized (see design doc Section 6, "Wine drink
 * effects... parked for a future brainstorm session").
 */
public class WineBottleItem extends AbstractCoralineDrinkItem {

    public WineBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE,
                "item.coraline_systems.wine_bottle.strength", ChatFormatting.DARK_PURPLE);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 4): dispatch Wine's level-tied drink effect here,
        // keyed off CoralineFluidUtils.getStrength(stack). Deliberately not
        // guessed yet — effect design isn't finalized.
    }
}
