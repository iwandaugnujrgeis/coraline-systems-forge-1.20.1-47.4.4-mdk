package net.zharok01.coralinesystems.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Make sure this is registered to the MOD bus!
@Mod.EventBusSubscriber(modid = "coraline_systems", bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoralineSpawnPlacements {

    // 1. Create your custom tag!
    public static final TagKey<Block> CAMELS_SPAWNABLE_ON = BlockTags.create(new ResourceLocation("coraline_systems", "camels_spawnable_on.json"));

    // 2. Register the placement rule using Forge's event
    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                EntityType.CAMEL,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> {
                    // 3. The exact logic you wanted! Check the tag, and ensure it has enough light to spawn (light > 8).
                    // (We write the light check manually because Animal.isBrightEnoughToSpawn is protected in Vanilla).
                    boolean isBrightEnough = level.getRawBrightness(pos, 0) > 8;

                    return level.getBlockState(pos.below()).is(CAMELS_SPAWNABLE_ON) && isBrightEnough;
                },
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }
}