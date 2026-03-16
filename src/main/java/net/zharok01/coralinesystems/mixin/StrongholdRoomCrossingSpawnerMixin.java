package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import net.minecraftforge.common.DungeonHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StrongholdPieces.RoomCrossing.class)
public abstract class StrongholdRoomCrossingSpawnerMixin extends StructurePiece {

    @Final
    @Shadow
    protected int type;

    protected StrongholdRoomCrossingSpawnerMixin(StructurePieceType pType, int pGenDepth, BoundingBox pBoundingBox) {
        super(pType, pGenDepth, pBoundingBox);
    }

    @Inject(method = "postProcess", at = @At("TAIL"))
    public void coraline$addRandomSquareRoomSpawner(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox bb, ChunkPos chunkPos, BlockPos pos, CallbackInfo ci) {
        if (this.type == 0) {
            int x = 5, y = 4, z = 5;
            this.placeBlock(level, Blocks.SPAWNER.defaultBlockState(), x, y, z, bb);

            BlockPos worldPos = this.getWorldPos(x, y, z);
            if (bb.isInside(worldPos) && level.getBlockEntity(worldPos) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(DungeonHooks.getRandomDungeonMob(random), random);
            }
        }
    }
}