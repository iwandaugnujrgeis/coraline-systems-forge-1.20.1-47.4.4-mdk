package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.zharok01.coralinesystems.CoralineSystems;

public class CoralineTags {
    // This defines the location: data/coraline_systems/tags/blocks/helper_mineable.json
    public static final TagKey<Block> HELPER_MINEABLE = TagKey.create(Registries.BLOCK,
            new ResourceLocation(CoralineSystems.MOD_ID, "helper_mineable"));
}