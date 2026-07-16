package net.zharok01.coralinesystems.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Strength-level NBT plumbing for Wine and Tea bottle/bucket items.
 * Mirrors {@code PotionUtils}'s {@code TAG_POTION} shape (a single NBT tag
 * read/written via small static helpers) but simpler — a plain int level
 * rather than a registry-backed {@code Potion} reference, since Wine/Tea
 * strength is just the 1-5 solid-ingredient level carried forward from
 * brewing, not a distinct effect type.
 * <p>
 * Only ever called with a real level (1-5) for Wine/Tea ItemStacks at the
 * moment of collection — that's Session 4's job. Session 1.6 only builds
 * this utility and the items that read it. Kombucha and Dregs items never
 * call {@link #setStrength}, and will always read back {@link #DEFAULT_STRENGTH}
 * if queried, which is intentionally inert for those two substances.
 */
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
