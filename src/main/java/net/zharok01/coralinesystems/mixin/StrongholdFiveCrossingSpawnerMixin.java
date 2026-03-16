package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import net.minecraftforge.common.DungeonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StrongholdPieces.FiveCrossing.class)
public abstract class StrongholdFiveCrossingSpawnerMixin extends StructurePiece {

    protected StrongholdFiveCrossingSpawnerMixin(StructurePieceType type, int i, BoundingBox box) {
        super(type, i, box);
    }

    @Inject(method = "postProcess", at = @At("TAIL"))
    public void coraline$addFiveCrossingSpawner(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox bb, ChunkPos chunkPos, BlockPos pos, CallbackInfo ci) {

        int x = 4, y = 3, z = 4;

        this.placeBlock(level, Blocks.SPAWNER.defaultBlockState(), x, y, z, bb);

        BlockPos worldPos = this.getWorldPos(x, y, z);

        if (bb.isInside(worldPos)) {
            BlockEntity blockEntity = level.getBlockEntity(worldPos);
            if (blockEntity instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(DungeonHooks.getRandomDungeonMob(random), random);
            }
        }
    }
}