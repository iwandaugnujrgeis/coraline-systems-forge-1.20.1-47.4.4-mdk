package net.zharok01.coralinesystems.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bottle-form Dregs — the collectible product of a spoiled Wine batch
 * (design doc Section 3) and the required culture input for Kombucha
 * (design doc Section 4, step 3). Single-strength, no
 * {@link CoralineFluidUtils} usage.
 * <p>
 * Drinkability itself is unresolved per the roadmap ("Dregs presumably
 * non-drinkable or a joke/negative effect") — {@link #applyDrinkEffect} is
 * a no-op for now so the item is functional/consistent during playtesting;
 * Session 4 decides whether Dregs should be drinkable at all, or whether
 * {@code use()} should be blocked/repurposed instead.
 */
public class DregsBottleItem extends AbstractCoralineDrinkItem {

    public DregsBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        // TODO (Session 4): decide + implement Dregs' drink effect (or lack
        // thereof) — see class javadoc. Not guessed here.
    }
}
