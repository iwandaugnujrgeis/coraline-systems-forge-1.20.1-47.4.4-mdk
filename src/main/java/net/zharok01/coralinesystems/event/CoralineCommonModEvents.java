package net.zharok01.coralinesystems.event;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityCockroach;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.legacy.rediscovered.registry.RediscoveredEntityTypes;
import com.teamabnormals.upgrade_aquatic.core.registry.UAEntityTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.*;
import net.zharok01.coralinesystems.registry.CoralineTags;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoralineCommonModEvents {

    private static final int CAMEL_MIN_SPAWN_LIGHT = 8;

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(IsotopicEntities.ORB.get(), OrbEntity.createAttributes().build());
        event.put(IsotopicEntities.BRUME.get(), BrumeEntity.createAttributes().build());
        event.put(IsotopicEntities.HELPER.get(), HelperEntity.createAttributes().build());
        event.put(IsotopicEntities.MONSTER.get(), MonsterEntity.createAttributes().build());
        event.put(IsotopicEntities.CRYPTID.get(), CryptidEntity.createAttributes().build());
        event.put(IsotopicEntities.STAR.get(), StarEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {

        event.register(
                RediscoveredEntityTypes.ZOMBIE_PIGMAN,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> ZombifiedPiglin.checkZombifiedPiglinSpawnRules(
                        EntityType.ZOMBIFIED_PIGLIN, level, spawnType, pos, random
                ),
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                AMEntityRegistry.COCKROACH.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> {
                    BlockState below = level.getBlockState(pos.below());
                    if (below.is(Blocks.NETHERRACK)) {
                        return spawnType == MobSpawnType.SPAWNER || below.isValidSpawn(level, pos.below(), entityType);
                    }
                    return EntityCockroach.canCockroachSpawn(entityType, level, spawnType, pos, random);
                },
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                EntityType.VEX,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                UAEntityTypes.GOOSE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                EntityType.CAMEL,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> {
                    boolean onSand = level.getBlockState(pos.below()).is(CoralineTags.CAMEL_SPAWNABLE_ON);
                    boolean brightEnough = level.getRawBrightness(pos, 0) > CAMEL_MIN_SPAWN_LIGHT;
                    return onSand && brightEnough;
                },
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                EntityType.PANDA,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) ->
                        level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON),
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                EntityType.AXOLOTL,
                SpawnPlacements.Type.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> level.getFluidState(pos).is(FluidTags.WATER),
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                AMEntityRegistry.GORILLA.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> {
                    BlockState state = level.getBlockState(pos.below());
                    return state.is(AMTagRegistry.GORILLA_SPAWNS) || state.is(Blocks.GRASS_BLOCK);
                },
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                AMEntityRegistry.TASMANIAN_DEVIL.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON),
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                IsotopicEntities.ORB.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                OrbEntity::checkOrbSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                IsotopicEntities.STAR.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                StarEntity::checkStarSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

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
                IsotopicEntities.BRUME.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BrumeEntity::checkBrumeSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                IsotopicEntities.CRYPTID.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CryptidEntity::checkCryptidSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                EntityType.MOOSHROOM,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) ->
                        level.getBlockState(pos.below()).is(BlockTags.MOOSHROOMS_SPAWNABLE_ON),
                SpawnPlacementRegisterEvent.Operation.REPLACE
        ); // TODO: I don't think this is working
    }
}