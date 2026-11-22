package net.zharok01.coralinesystems.mixin.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(WorldCarver.class)
public abstract class WorldCarverMixin<C extends CarverConfiguration> {

    @Unique private static final int LEDGE_CHANCE = 80;
    @Unique private static final int LEDGE_MAX_Y = 60;
    @Unique private static final int LEDGE_MIN_Y = -50;
    @Unique private static final int CLEANUP_MIN_Y = 40;
    @Unique private static final int CLEANUP_MAX_Y = 100;

    @Inject(
            method = "carveBlock",
            at = @At("RETURN")
    )
    private void coraline$tryPlaceLedge(
            CarvingContext context,
            C config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            CarvingMask carvingMask,
            BlockPos.MutableBlockPos pos,
            BlockPos.MutableBlockPos checkPos,
            Aquifer aquifer,
            MutableBoolean reachedSurface,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ()) return;

        int y = pos.getY();

        if (y > LEDGE_MAX_Y || y < LEDGE_MIN_Y) return;

        if (reachedSurface.isTrue()) return;

        long blockSeed = BlockPos.asLong(pos.getX(), y, pos.getZ());
        blockSeed ^= blockSeed >>> 33;
        blockSeed *= 0xff51afd7ed558ccdL;
        blockSeed ^= blockSeed >>> 33;
        if (Math.abs(blockSeed % LEDGE_CHANCE) != 0) return;

        long dirSeed = blockSeed * 0xc4ceb9fe1a85ec53L;
        Direction[] horizontals = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
        Direction shelfDir = horizontals[(int)(Math.abs(dirSeed) % horizontals.length)];

        BlockPos ledgePos = pos.relative(shelfDir);

        if (ledgePos.getY() < context.getMinGenY() || ledgePos.getY() >= context.getMinGenY() + context.getGenDepth()) return;

        BlockState existing = chunk.getBlockState(ledgePos);

        if (!existing.isAir() && existing.getFluidState().isEmpty()
                && coraline$isStoneType(existing)) {
            BlockPos aboveLedge = ledgePos.above();
            if (aboveLedge.getY() < context.getMinGenY() + context.getGenDepth()) {
                BlockState above = chunk.getBlockState(aboveLedge);
                if (!above.isAir() && coraline$isStoneType(above)) {
                    chunk.setBlockState(aboveLedge, Blocks.CAVE_AIR.defaultBlockState(), false);
                }
            }
        }
    }

    @Inject(
            method = "carveBlock",
            at = @At("RETURN")
    )
    private void coraline$cleanFloatingRemnants(
            CarvingContext context,
            C config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            CarvingMask carvingMask,
            BlockPos.MutableBlockPos pos,
            BlockPos.MutableBlockPos checkPos,
            Aquifer aquifer,
            MutableBoolean reachedSurface,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ()) return;

        int y = pos.getY();
        if (y < CLEANUP_MIN_Y || y > CLEANUP_MAX_Y) return;

        int rx = pos.getX();
        int ry = y + 1;
        int rz = pos.getZ();

        if (ry >= context.getMinGenY() + context.getGenDepth()) return;

        BlockState candidate = chunk.getBlockState(new BlockPos(rx, ry, rz));

        if (candidate.isAir() || !candidate.getFluidState().isEmpty()) return;
        if (!coraline$isSolidTerrain(candidate)) return;

        ChunkPos chunkPos = chunk.getPos();
        int localX = rx - chunkPos.getMinBlockX();
        int localZ = rz - chunkPos.getMinBlockZ();

        if (localX == 0 || localX == 15 || localZ == 0 || localZ == 15) return;

        boolean northAir = coraline$isAirLike(chunk.getBlockState(new BlockPos(rx,     ry, rz - 1)));
        boolean southAir = coraline$isAirLike(chunk.getBlockState(new BlockPos(rx,     ry, rz + 1)));
        boolean westAir  = coraline$isAirLike(chunk.getBlockState(new BlockPos(rx - 1, ry, rz    )));
        boolean eastAir  = coraline$isAirLike(chunk.getBlockState(new BlockPos(rx + 1, ry, rz    )));

        if (northAir && southAir && westAir && eastAir) {
            chunk.setBlockState(new BlockPos(rx, ry, rz), Blocks.AIR.defaultBlockState(), false);
        }
    }

    @Unique
    private static boolean coraline$isSolidTerrain(BlockState state) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.TUFF)
                || state.is(Blocks.CALCITE)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.SANDSTONE);
    }

    @Unique
    private static boolean coraline$isAirLike(BlockState state) {
        return state.isAir();
    }

    @Unique
    private static boolean coraline$isStoneType(BlockState state) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.TUFF)
                || state.is(Blocks.CALCITE);
    }
}