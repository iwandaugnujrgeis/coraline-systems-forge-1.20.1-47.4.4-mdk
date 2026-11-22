package net.zharok01.coralinesystems.client.color;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GrassColor;
import net.zharok01.coralinesystems.block.BrewingCauldronColors;
import net.zharok01.coralinesystems.item.CoralineFluidUtils;
import net.zharok01.coralinesystems.util.CoralineTintUtils;

public final class CoralineItemColors {

    public static final int MULBERRY_JUICE_START = BrewingCauldronColors.MULBERRY_JUICE_START;
    public static final int MULBERRY_JUICE_END = BrewingCauldronColors.MULBERRY_JUICE_END;

    public static final int WINE_START = BrewingCauldronColors.WINE_START;
    public static final int WINE_END = BrewingCauldronColors.WINE_END;

    public static final int TEA_START = BrewingCauldronColors.TEA_START;
    public static final int TEA_END = BrewingCauldronColors.TEA_END;

    public static final int KOMBUCHA_COLOR = 0xff3e55;

    public static final int DREGS_COLOR = BrewingCauldronColors.DREGS_COLOR;

    private static final int NO_TINT = -1;

    private CoralineItemColors() {
    }

    // public static final ItemColor WOOD_SORREL = (ItemStack stack, int tintIndex) -> GrassColor.get(0.5D, 1.0D);

    private static ItemColor strengthGradient(int startRgb, int endRgb) {
        return (ItemStack stack, int tintIndex) -> {
            if (tintIndex != 0) return NO_TINT;
            return CoralineTintUtils.lerpStrength(startRgb, endRgb, CoralineFluidUtils.getStrength(stack));
        };
    }

    private static ItemColor flatColor(int rgb) {
        return (ItemStack stack, int tintIndex) -> tintIndex != 0 ? NO_TINT : rgb;
    }

    public static final ItemColor MULBERRY_JUICE = strengthGradient(MULBERRY_JUICE_START, MULBERRY_JUICE_END);
    public static final ItemColor WINE = strengthGradient(WINE_START, WINE_END);
    public static final ItemColor TEA = strengthGradient(TEA_START, TEA_END);
    public static final ItemColor KOMBUCHA = flatColor(KOMBUCHA_COLOR);
    public static final ItemColor DREGS = flatColor(DREGS_COLOR);
}