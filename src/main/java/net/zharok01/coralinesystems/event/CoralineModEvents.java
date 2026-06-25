package net.zharok01.coralinesystems.event;

import com.legacy.rediscovered.registry.RediscoveredEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.OrbEntity;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoralineModEvents {

    @SubscribeEvent
    public static void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {

        // Patching Rediscovered: They forgot to register a placement for their Zombie Pigman,
        // causing it to spawn mid-air. We assign the Vanilla Zombified Piglin placement rules here.
        event.register(
                RediscoveredEntityTypes.ZOMBIE_PIGMAN,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                // Lambda wrapper to satisfy Java's strict generic types
                (type, level, spawnType, pos, random) -> ZombifiedPiglin.checkZombifiedPiglinSpawnRules(
                        EntityType.ZOMBIFIED_PIGLIN, level, spawnType, pos, random
                ),
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }

    // Registering Attributes
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(IsotopicEntities.ORB.get(), OrbEntity.createAttributes().build());
    }

    // Registering Dawn Spawning Predicate
    @SubscribeEvent
    public static void entitySpawnRestriction(SpawnPlacementRegisterEvent event) {
        event.register(IsotopicEntities.ORB.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS, // We use NO_RESTRICTIONS so it can spawn in the air!
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                OrbEntity::checkOrbSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}