package net.zharok01.coralinesystems.event;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.OrbEntity;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

// CRITICAL FIX: Added 'bus = Mod.EventBusSubscriber.Bus.MOD'
@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoralineModEvents {

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