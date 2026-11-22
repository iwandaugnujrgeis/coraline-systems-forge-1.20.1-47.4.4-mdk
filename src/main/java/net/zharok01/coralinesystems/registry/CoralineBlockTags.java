package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.zharok01.coralinesystems.CoralineSystems;

public class CoralineBlockTags {

    public static final TagKey<Block> BANANA_LEAVES =
            tag("banana_leaves");

    public static final TagKey<Block> MANGO_LEAVES =
            tag("mango_leaves");

    public static final TagKey<Block> WALNUT_LEAVES =
            tag("walnut_leaves");

    public static final TagKey<Block> APPLE_LEAVES =
            tag("apple_leaves");

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, CoralineSystems.of(path));
    }
}
