package net.zharok01.coralinesystems.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.zharok01.coralinesystems.registry.CoralineItems;

public class OnyxTier {

    public static final Tier INSTANCE = new ForgeTier(
            3,
            1100,
            6.0F,
            3.0F,
            12,
            net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL,
            () -> Ingredient.of(CoralineItems.ONYX.get())
    );
}