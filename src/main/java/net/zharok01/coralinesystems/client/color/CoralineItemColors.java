package net.zharok01.coralinesystems.client.color;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.item.CoralineFluidUtils;

public final class CoralineItemColors {

    // Same gradients as CoralineBlockColors -- kept as separate constants
    // (not shared statics) since the block/item tint layers are
    // conceptually independent registrations, even though the numbers
    // happen to match today. See CoralineBlockColors' own javadoc for the
    // design-conversation source of these values.
    private static final int MULBERRY_JUICE_START = 0xe597e8;
    private static final int MULBERRY_JUICE_END = 0xcb67cf;

    private static final int WINE_START = 0xb93dbf;
    private static final int WINE_END = 0x7a1f80;

    private static final int TEA_START = 0xffb866;
    private static final int TEA_END = 0xed8f21;

    private static final int KOMBUCHA_COLOR = 0xffcc7a;
    private static final int DREGS_COLOR = 0xe3ed87;

    private static final int NO_TINT = -1;

    private CoralineItemColors() {
    }

    private static ItemColor strengthGradient(int startRgb, int endRgb) {
        return (ItemStack stack, int tintIndex) -> {
            if (tintIndex != 0) {
                return NO_TINT;
            }
            int strength = CoralineFluidUtils.getStrength(stack);
            return CoralineTintUtils.lerpStrength(startRgb, endRgb, strength);
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