package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.registry.CoralineEffects;

/**
 * Bottle-form Mulberry Juice. Applies Instant Stamina at a fixed amplifier
 * of 1 (→ 4 stamina points) regardless of brew strength. Strength is still
 * carried via {@link CoralineFluidUtils} and shown in the tooltip, but it
 * does not affect the stamina replenishment amount.
 */
public class MulberryJuiceBottleItem extends AbstractCoralineDrinkItem {

    /** Fixed amplifier: (1 + 1) * 2 = 4 stamina points restored. */
    private static final int FIXED_AMPLIFIER = 1;

    public MulberryJuiceBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE,
                "item.coraline_systems.mulberry_juice_bottle.strength", ChatFormatting.RED);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;

        player.addEffect(new MobEffectInstance(
                CoralineEffects.INSTANT_STAMINA.get(),
                1,
                FIXED_AMPLIFIER,
                false,
                false,
                false
        ));
    }
}
