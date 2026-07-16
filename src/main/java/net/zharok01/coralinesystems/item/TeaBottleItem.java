package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bottle-form Tea, on {@link AbstractCoralineDrinkItem}. Tea Leaves use the
 * same 1-5 level-tracking pattern as Mulberries/Wine (design doc Section 4,
 * step 2), so strength is carried the same way.
 * <p>
 * Per Session 1.7, Tea's *recipe* (Water + Tea Leaves, no culture step) is
 * finalized and collection is wired in {@code BrewingCauldronInteractions}.
 * Tea's drink *effect* remains undesigned (design doc Section 6) — see
 * {@link #applyDrinkEffect}.
 */
public class TeaBottleItem extends AbstractCoralineDrinkItem {

    public TeaBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE,
                "item.coraline_systems.tea_bottle.strength", ChatFormatting.GREEN);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 5+): Tea's drink effect is not yet designed at all
        // (design doc Section 6). No placeholder guessed here.
    }
}
