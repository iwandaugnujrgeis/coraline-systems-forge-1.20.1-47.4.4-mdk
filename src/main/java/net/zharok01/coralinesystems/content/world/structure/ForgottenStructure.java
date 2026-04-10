package net.zharok01.coralinesystems.content.world.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

/**
 * The Forgotten Dungeon — a large multi-level roguelike ruin that generates
 * underground in the Overworld, occasionally breaching the surface as crumbling
 * stone-brick towers (via StaircaseUp pieces).
 *
 * Registration checklist (things you must do outside this file):
 *
 *   1. Register a StructureType<ForgottenStructure> so the codec is known:
 *          public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
 *              DeferredRegister.create(Registries.STRUCTURE_TYPE, MOD_ID);
 *          public static final RegistryObject<StructureType<ForgottenStructure>> FORGOTTEN =
 *              STRUCTURE_TYPES.register("forgotten_dungeon.json",
 *                  () -> () -> ForgottenStructure.CODEC);
 *
 *   2. Add a JSON at:
 *          data/coraline_systems/worldgen/structure/forgotten_dungeon.json.json
 *      pointing at the correct biomes, step (UNDERGROUND_STRUCTURES), and
 *      terrain_adaptation (BURY or NONE).
 *
 *   3. Call CoralineStructurePieceTypes.register() early — from your mod
 *      constructor or FMLCommonSetupEvent, before any world loads.
 */
public class ForgottenStructure extends Structure {

    public static final Codec<ForgottenStructure> CODEC = simpleCodec(ForgottenStructure::new);

    public ForgottenStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        // Start the bounding box calculation at sea level; moveBelowSeaLevel
        // will sink it underground. The Y here is just a placeholder that gets
        // overwritten by the offset returned from generatePiecesAndAdjust.
        BlockPos startPos = new BlockPos(chunkPos.getMiddleBlockX(), 50, chunkPos.getMiddleBlockZ());
        StructurePiecesBuilder builder = new StructurePiecesBuilder();
        int yAdjust = generatePiecesAndAdjust(builder, context);
        return Optional.of(new GenerationStub(startPos.offset(0, yAdjust, 0), Either.right(builder)));
    }

    private static int generatePiecesAndAdjust(StructurePiecesBuilder builder, GenerationContext context) {
        ForgottenPieces.resetPieces();

        ForgottenPieces.StartRoom startRoom = new ForgottenPieces.StartRoom(
                context.random(),
                context.chunkPos().getBlockX(2),
                context.chunkPos().getBlockZ(2)
        );
        builder.addPiece(startRoom);
        startRoom.addChildren(startRoom, builder, context.random());

        // Process pending children exactly like StrongholdStructure does —
        // random order to prevent directional bias in the final layout.
        java.util.List<net.minecraft.world.level.levelgen.structure.StructurePiece> pending
                = startRoom.pendingChildren;
        while (!pending.isEmpty()) {
            int idx = context.random().nextInt(pending.size());
            net.minecraft.world.level.levelgen.structure.StructurePiece child = pending.remove(idx);
            child.addChildren(startRoom, builder, context.random());
        }

        // Sink the whole structure underground.
        // The +10 minimum clearance keeps the deepest parts above y=10,
        // while StaircaseUp pieces naturally poke toward the surface.
        return builder.moveBelowSeaLevel(
                context.chunkGenerator().getSeaLevel(),
                context.chunkGenerator().getMinY(),
                context.random(),
                10
        );
    }

    @Override
    public StructureType<?> type() {
        return net.zharok01.coralinesystems.registry.CoralineStructureTypes.FORGOTTEN.get();
    }
}
