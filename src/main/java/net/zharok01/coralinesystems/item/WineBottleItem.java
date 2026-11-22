package net.zharok01.coralinesystems.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.client.color.CoralineItemColors;
import net.zharok01.coralinesystems.registry.CoralineEffects;
import net.zharok01.coralinesystems.util.CoralineTintUtils;

public class WineBottleItem extends AbstractCoralineDrinkItem {

    private static final int TICKS_PER_STRENGTH = 20 * 20;

    public WineBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE, stack -> {
            int strength = CoralineFluidUtils.getStrength(stack);
            return blue(Component.translatable(
                    "tooltip.coraline_systems.wine_bottle",
                    Component.translatable("effect.coraline_systems.persistence"),
                    formatDuration(strength * TICKS_PER_STRENGTH)
            ));
        });
    }

    @Override
    protected int getDrinkColor(ItemStack stack) {
        return CoralineTintUtils.lerpStrength(CoralineItemColors.WINE_START, CoralineItemColors.WINE_END, CoralineFluidUtils.getStrength(stack));
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;

        int strength = CoralineFluidUtils.getStrength(stack);
        player.addEffect(new MobEffectInstance(
                CoralineEffects.PERSISTENCE.get(),
                strength * TICKS_PER_STRENGTH,
                0,
                false,
                true,
                true
        ));
    }
}