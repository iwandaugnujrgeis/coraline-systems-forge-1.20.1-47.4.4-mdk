package net.zharok01.coralinesystems.mixin;

import com.legacy.rediscovered.world.structure.pool_elements.BrickPyramidIslandPoolElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;
import org.joml.SimplexNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(BrickPyramidIslandPoolElement.class)
public abstract class BrickPyramidIslandMixin {

    @Shadow(remap = false, aliases = {"m_214015_"})
    public abstract BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation);

    @Inject(method = "m_213577_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$flattenSize(StructureTemplateManager structureTemplateManager, Rotation rotation, CallbackInfoReturnable<Vec3i> cir) {
        cir.setReturnValue(new Vec3i(190, 24, 190));
    }

    @Inject(method = "m_213695_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$indevPlacement(StructureTemplateManager structureTemplateManager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGen, BlockPos pos, BlockPos structureOrigin, Rotation rotation, BoundingBox chunkBounds, RandomSource rand, boolean pKeepJigsaws, CallbackInfoReturnable<Boolean> cir) {

        BlockState baseMaterial = Blocks.STONE.defaultBlockState();
        BlockState topMaterial = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState soilMaterial = Blocks.DIRT.defaultBlockState();

        float horizontalNoiseScale = 2.5F;
        float verticalNoiseScale = 5F;
        int deltaDist = 18;
        float dicingAmount = 0.85F;

        BoundingBox bb = this.getBoundingBox(structureTemplateManager, pos, rotation);
        int maxDist = bb.getXSpan() / 2 - deltaDist;
        Vec3 originVec = structureOrigin.getCenter();
        Set<BlockPos> positions = new HashSet<>();

        for (int x = chunkBounds.minX(); x <= chunkBounds.maxX(); x++) {
            for (int z = chunkBounds.minZ(); z <= chunkBounds.maxZ(); z++) {
                float heightScale = 0.03F;
                int maxY = bb.maxY() + (int) ((SimplexNoise.noise(x * heightScale, z * heightScale) + 1) * 2);

                for (int y = bb.minY(); y <= maxY; y++) {
                    BlockPos placePos = new BlockPos(x, y, z);
                    Vec3 vec = placePos.getCenter();

                    int dicing = 16;
                    Vec3 dicedVec8 = new BlockPos(x / dicing * dicing, y / dicing * dicing, z / dicing * dicing).getCenter();

                    float dist = (float) vec.distanceTo(originVec);
                    if (dist > maxDist + deltaDist) continue;

                    Vec3[] vecs = new Vec3[]{vec, dicedVec8};
                    float[] noises = new float[vecs.length];
                    for (int i = 0; i < vecs.length; i++) {
                        Vec3 v = vecs[i];
                        Vec3 angle = originVec.subtract(v).normalize().add(originVec);
                        noises[i] = SimplexNoise.noise((float) angle.x() * horizontalNoiseScale, (float) angle.y() * verticalNoiseScale, (float) angle.z() * horizontalNoiseScale);
                    }

                    float n = noises[0] * (1 - dicingAmount) + noises[1] * dicingAmount;

                    //Terraced/jagged rounding at the bottom:
                    float rawYScaling = (y - bb.minY()) / (float) bb.getYSpan();

                    //Hard-step the scaling into 5 distinct horizontal layers to kill the "smooth bowl" look
                    float yScaling = (float) Math.floor(rawYScaling * 5.0F) / 5.0F;

                    float scale;
                    if (yScaling < 0.8335)
                        scale = 1 - (float) Math.pow(1 - yScaling, 5);
                    else
                        scale = (float) Math.sin(yScaling * 2.0);

                    float threshold = (maxDist + (deltaDist * n)) * scale;

                    if (dist < threshold) {
                        positions.add(placePos);
                    }
                }
            }
        }

        if (positions.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        ChunkAccess chunk = level.getChunk(new BlockPos(chunkBounds.minX(), chunkBounds.minY(), chunkBounds.minZ()));
        Heightmap surfaceHM = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        Heightmap oceanHM = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);

        for (BlockPos placePos : positions) {
            BlockState state = baseMaterial;
            if (!positions.contains(placePos.above(1)))
                state = topMaterial;
            else if (!positions.contains(placePos.above(2)))
                state = soilMaterial;
            else if (!positions.contains(placePos.above(3)) && rand.nextBoolean())
                state = soilMaterial;

            level.setBlock(placePos, state, Block.UPDATE_CLIENTS);
            BlockPos hmPos = new BlockPos(SectionPos.sectionRelative(placePos.getX()), placePos.getY() + 1, SectionPos.sectionRelative(placePos.getZ()));
            surfaceHM.update(hmPos.getX(), hmPos.getY(), hmPos.getZ(), state);
            oceanHM.update(hmPos.getX(), hmPos.getY(), hmPos.getZ(), state);
        }

        cir.setReturnValue(true);
    }
}