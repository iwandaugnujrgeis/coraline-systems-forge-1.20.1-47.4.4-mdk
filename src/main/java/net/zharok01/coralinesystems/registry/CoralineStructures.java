package net.zharok01.coralinesystems.registry;

import com.legacy.structure_gel.api.registry.registrar.RegistrarHandler;
import com.legacy.structure_gel.api.registry.registrar.StructureRegistrar;
import com.legacy.structure_gel.api.structure.GridStructurePlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.eventbus.api.IEventBus;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.worldgen.structure.RuinsPieces;
import net.zharok01.coralinesystems.worldgen.structure.RuinsStructure;

public class CoralineStructures {

    public static final RegistrarHandler<StructureTemplatePool> HANDLER =
            RegistrarHandler.getOrCreate(Registries.TEMPLATE_POOL, CoralineSystems.MOD_ID);

    public static final StructureRegistrar<RuinsStructure> RUINS;

    public static void init(IEventBus bus) {
        RegistrarHandler.registerHandlers(CoralineSystems.MOD_ID, bus);
    }

    static {
        ResourceLocation ruinsId = CoralineSystems.of("ruins");

        RUINS = StructureRegistrar
                .builder(ruinsId, () -> () -> RuinsStructure.CODEC)
                .addPiece(() -> RuinsPieces.Piece::new)
                .pushStructure(RuinsStructure::new)
                .terrainAdjustment(TerrainAdjustment.NONE)
                .biomes(CoralineTags.HAS_RUINS)
                .popStructure()
                .placement(c -> GridStructurePlacement.builder(20, 8, 0.9f)
                        .allowedNearSpawn(true)
                        .build(ruinsId))
                .build();
    }
}