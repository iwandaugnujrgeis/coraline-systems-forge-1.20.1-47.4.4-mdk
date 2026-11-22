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
import org.jetbrains.annotations.NotNull;

public class TeaBottleItem extends AbstractCoralineDrinkItem {

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V"};

    public TeaBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE, stack -> {
            int strength = CoralineFluidUtils.getStrength(stack);
            int clamped = Math.max(1, Math.min(5, strength));
            return blue(Component.translatable(
                    "tooltip.coraline_systems.tea_bottle",
                    Component.translatable("effect.coraline_systems.instant_stamina"),
                    ROMAN[clamped - 1]
            ));
        });
    }

    @Override
    protected int getDrinkColor(ItemStack stack) {
        return CoralineTintUtils.lerpStrength(CoralineItemColors.TEA_START, CoralineItemColors.TEA_END, CoralineFluidUtils.getStrength(stack));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;

        int strength = CoralineFluidUtils.getStrength(stack);
        player.addEffect(new MobEffectInstance(
                CoralineEffects.INSTANT_STAMINA.get(),
                1,
                strength,
                false,
                false,
                false
        ));
    }
}