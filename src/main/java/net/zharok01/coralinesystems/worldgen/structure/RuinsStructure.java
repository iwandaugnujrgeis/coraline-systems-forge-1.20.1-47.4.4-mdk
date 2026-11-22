package net.zharok01.coralinesystems.worldgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.zharok01.coralinesystems.registry.CoralineStructures;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RuinsStructure extends Structure {

    public static final Codec<RuinsStructure> CODEC = simpleCodec(RuinsStructure::new);

    private static final int MIN_SURFACE_Y = 50;

    public RuinsStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public @NotNull Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();

        int surfaceY = context.chunkGenerator().getBaseHeight(
                chunkPos.getMinBlockX() + 8,
                chunkPos.getMinBlockZ() + 8,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState()
        );

        if (surfaceY < MIN_SURFACE_Y) return Optional.empty();

        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG,
                builder -> generatePieces(builder, context));
    }

    private void generatePieces(StructurePiecesBuilder builder, GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        WorldgenRandom rand = context.random();

        BlockPos origin = new BlockPos(chunkPos.getMinBlockX() + 8, 90, chunkPos.getMinBlockZ() + 8);
        Rotation rotation = Rotation.getRandom(rand);

        RuinsPieces.addPieces(origin, rotation, builder, rand);
    }

    @Override
    public StructureType<?> type() {
        return CoralineStructures.RUINS.getType();
    }
}