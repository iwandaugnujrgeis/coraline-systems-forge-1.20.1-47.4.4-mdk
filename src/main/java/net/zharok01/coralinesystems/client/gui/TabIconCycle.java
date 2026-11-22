package net.zharok01.coralinesystems.client.gui;

import com.google.common.collect.Iterables;
import java.util.Collection;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class TabIconCycle {
    private static final int MIN_SWAP_DELAY_MS = 1200;
    private static final int MAX_SWAP_DELAY_MS = 3500;
    private static final RandomSource RANDOM = RandomSource.create();

    private ItemStack shown = ItemStack.EMPTY;
    private long nextSwapMillis;

    public boolean isDue() {
        return Util.getMillis() >= this.nextSwapMillis;
    }

    public ItemStack shown() {
        return this.shown;
    }

    public void showOneOf(Collection<ItemStack> candidates) {
        this.nextSwapMillis = Util.getMillis() + RANDOM.nextIntBetweenInclusive(MIN_SWAP_DELAY_MS, MAX_SWAP_DELAY_MS);

        if (candidates.isEmpty()) {
            this.shown = ItemStack.EMPTY;
            return;
        }

        int index = RANDOM.nextInt(candidates.size());
        ItemStack picked = Iterables.get(candidates, index);
        if (candidates.size() > 1 && ItemStack.isSameItem(picked, this.shown)) {
            picked = Iterables.get(candidates, (index + 1) % candidates.size());
        }
        this.shown = picked;
    }
}