package net.zharok01.coralinesystems.util;

import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public final class DyeableItemColorHelper {
    private DyeableItemColorHelper() {
    }

    public static void coraline$blendColors(ItemStack result, ItemStack first, ItemStack second) {
        if (!(result.getItem() instanceof DyeableLeatherItem dyeable)) {
            return;
        }

        boolean firstDyed = dyeable.hasCustomColor(first);
        boolean secondDyed = dyeable.hasCustomColor(second);
        if (firstDyed && secondDyed) {
            dyeable.setColor(result, coraline$averageColor(dyeable.getColor(first), dyeable.getColor(second)));
        } else if (secondDyed) {
            dyeable.setColor(result, dyeable.getColor(second));
        }
    }

    private static int coraline$averageColor(int first, int second) {
        int red = ((first >> 16 & 255) + (second >> 16 & 255)) / 2;
        int green = ((first >> 8 & 255) + (second >> 8 & 255)) / 2;
        int blue = ((first & 255) + (second & 255)) / 2;
        return (red << 16) | (green << 8) | blue;
    }
}
