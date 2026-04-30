package net.zharok01.coralinesystems.event;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.entity.custom.MonsterEntity;
import net.zharok01.coralinesystems.registry.IsotopicEntities;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(IsotopicEntities.HELPER.get(), HelperEntity.createAttributes().build());
        event.put(IsotopicEntities.MONSTER.get(), MonsterEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawns(SpawnPlacementRegisterEvent event) {
        event.register(
                IsotopicEntities.HELPER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HelperEntity::checkHelperSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
                IsotopicEntities.MONSTER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MonsterEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
                EntityType.CAMEL,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> {
                    // Custom logic: Camel spawns if brightness > 8
                    // and block below is in your custom tag
                    boolean isBrightEnough = level.getRawBrightness(pos, 0) > 8;
                    return level.getBlockState(pos.below()).is(BlockTags.SAND) && isBrightEnough;
                },
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }
}