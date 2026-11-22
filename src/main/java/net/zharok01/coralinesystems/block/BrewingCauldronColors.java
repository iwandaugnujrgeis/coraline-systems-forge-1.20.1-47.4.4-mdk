package net.zharok01.coralinesystems.block;

import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.util.CoralineTintUtils;

public final class BrewingCauldronColors {

    public static final int NO_TINT = -1;

    public static final int MULBERRY_JUICE_START = 0xe563c7;
    public static final int MULBERRY_JUICE_END = 0xc550b2;

    public static final int WINE_START = 0xc037c7;
    public static final int WINE_END = 0x9f2ea7;

    public static final int TEA_START = 0xff961d;
    public static final int TEA_END = 0xd47200;

    public static final int DREGS_COLOR = 0x402f39;

    private static final int KOMBUCHA_COLOR = 0xfd737e;
    private static final int FALLBACK_SPLASH_COLOR = 0xd070d0;

    private BrewingCauldronColors() {
    }

    public static int getContentColor(BlockState state, int tintIndex) {
        if (tintIndex != 1 && tintIndex != 2) return NO_TINT;

        BrewingFluid fluid = state.getValue(BrewingCauldronBlock.FLUID);
        if (fluid == BrewingFluid.NONE) return NO_TINT;

        int strength = state.getValue(BrewingCauldronBlock.STRENGTH);
        int baseColor = switch (fluid) {
            case JUICE -> CoralineTintUtils.lerpStrength(MULBERRY_JUICE_START, MULBERRY_JUICE_END, strength);
            case TEA -> CoralineTintUtils.lerpStrength(TEA_START, TEA_END, strength);
            case WINE -> CoralineTintUtils.lerpStrength(WINE_START, WINE_END, strength);
            case KOMBUCHA -> KOMBUCHA_COLOR;
            default -> DREGS_COLOR;
        };

        if (tintIndex == 1) return baseColor;
        return fluid.hasOpaqueOverlay() ? baseColor : NO_TINT;
    }

    public static int getSplashColor(BlockState state) {
        int color = getContentColor(state, 1);
        return color == NO_TINT ? FALLBACK_SPLASH_COLOR : color;
    }
}