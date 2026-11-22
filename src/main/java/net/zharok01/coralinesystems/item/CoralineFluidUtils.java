package net.zharok01.coralinesystems.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class CoralineFluidUtils {

    private static final String TAG_STRENGTH = "Strength";
    private static final int DEFAULT_STRENGTH = 1;

    private CoralineFluidUtils() {
    }

    public static ItemStack setStrength(ItemStack stack, int level) {
        stack.getOrCreateTag().putInt(TAG_STRENGTH, level);
        return stack;
    }

    public static int getStrength(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_STRENGTH, 99) ? tag.getInt(TAG_STRENGTH) : DEFAULT_STRENGTH;
    }
}
