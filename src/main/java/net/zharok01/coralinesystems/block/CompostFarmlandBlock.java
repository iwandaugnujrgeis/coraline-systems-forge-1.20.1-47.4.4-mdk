package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.PlantType;
import net.zharok01.coralinesystems.registry.CoralineBlocks;

import javax.annotation.Nullable;

/**
 * Compost Farmland block.
 *
 * <p>Produced by tilling an {@link OrganicCompostBlock} with a hoe. Behaves like vanilla
 * {@link FarmBlock} in most respects, with these differences:
 * <ul>
 *   <li>Trampling (falling on it) reverts it to {@link OrganicCompostBlock}, not Dirt.</li>
 *   <li>When it can no longer survive (e.g. solid block placed above), it also reverts to
 *       {@link OrganicCompostBlock} instead of Dirt.</li>
 *   <li>On a fully hydrated random tick (moisture == 7) it has a 25% chance to apply one
 *       bonemeal growth tick to the crop above — identical to FD's RichSoilFarmlandBlock.</li>
 *   <li>Supports CROP and PLAINS plant types only (same as FD).</li>
 *   <li>If placement would fail the survival check, the block reverts to
 *       {@link OrganicCompostBlock} instead of Dirt.</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("deprecation")
public class CompostFarmlandBlock extends FarmBlock {

    /** Probability that a fully-hydrated random tick boosts the crop above. */
    private static final float BOOST_CHANCE = 0.25f;

    public CompostFarmlandBlock(Properties properties) {
        super(properties);
    }

    // -------------------------------------------------------------------------
    // Random-tick logic
    // -------------------------------------------------------------------------

    /**
     * Identical to {@link FarmBlock#randomTick} moisture handling, except when fully hydrated
     * (moisture == 7) we also attempt to boost the crop above.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);

        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            }
            // When dry and no plant is sustaining us, vanilla would turn to dirt here.
            // We intentionally skip that path — turnToCompost is handled in tick() instead,
            // mirroring FD's choice to only do it in the scheduled tick, not randomTick.
        } else if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        } else {
            // moisture == 7 and near water: try to boost the crop above.
            if (random.nextFloat() < BOOST_CHANCE) {
                tryBoostPlantAbove(level, pos);
            }
        }
    }

    /**
     * Attempts to apply one bonemeal growth tick to the block directly above {@code pos}.
     */
    private static void tryBoostPlantAbove(ServerLevel level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        if (aboveState.getBlock() instanceof BonemealableBlock growable) {
            if (growable.isValidBonemealTarget(level, abovePos, aboveState, false)
                    && ForgeHooks.onCropsGrowPre(level, abovePos, aboveState, true)) {
                growable.performBonemeal(level, level.random, abovePos, aboveState);
                level.levelEvent(LevelEvent.PARTICLES_PLANT_GROWTH, abovePos, 0);
                ForgeHooks.onCropsGrowPost(level, abovePos, aboveState);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Survival check — revert to OrganicCompost, not Dirt
    // -------------------------------------------------------------------------

    /**
     * Called by the scheduled tick set up in {@link FarmBlock#updateShape} when a solid block
     * is placed above. Reverts to {@link OrganicCompostBlock} instead of vanilla Dirt.
     */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            turnToCompost(null, state, level, pos);
        }
    }

    /**
     * Pushes entities up and replaces this block with {@link OrganicCompostBlock}.
     * Used by both {@link #tick} and {@link #fallOn}.
     */
    public static void turnToCompost(@Nullable Entity entity, BlockState state,
                                     Level level, BlockPos pos) {
        BlockState compostState = pushEntitiesUp(
                state,
                CoralineBlocks.ORGANIC_COMPOST.get().defaultBlockState(),
                level,
                pos
        );
        level.setBlockAndUpdate(pos, compostState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, compostState));
    }

    // -------------------------------------------------------------------------
    // Trampling — revert to OrganicCompost instead of Dirt
    // -------------------------------------------------------------------------

    /**
     * Trampling (landing on farmland from height) converts this block back to
     * {@link OrganicCompostBlock} rather than vanilla Dirt.
     *
     * <p>Note: we still call {@code ForgeHooks.onFarmlandTrample} so other mods can cancel the
     * event. We pass {@link OrganicCompostBlock}'s default state as the "new state" argument so
     * any listener that inspects it sees the correct target block.</p>
     */
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide
                && ForgeHooks.onFarmlandTrample(
                level, pos,
                CoralineBlocks.ORGANIC_COMPOST.get().defaultBlockState(),
                fallDistance, entity)) {
            turnToCompost(entity, state, level, pos);
        }
        // Always apply fall damage regardless of whether trampling happened.
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    // -------------------------------------------------------------------------
    // Placement fallback — place OrganicCompost if farmland can't survive here
    // -------------------------------------------------------------------------

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!this.defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())) {
            return CoralineBlocks.ORGANIC_COMPOST.get().defaultBlockState();
        }
        return super.getStateForPlacement(context);
    }

    // -------------------------------------------------------------------------
    // Survival — allow StemGrownBlock above (melons/pumpkins), matching FD
    // -------------------------------------------------------------------------

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        return super.canSurvive(state, level, pos)
                || aboveState.getBlock() instanceof StemGrownBlock;
    }

    // -------------------------------------------------------------------------
    // Forge fertility — fertile when moisture > 0
    // -------------------------------------------------------------------------

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.is(CoralineBlocks.COMPOST_FARMLAND.get())) {
            return state.getValue(CompostFarmlandBlock.MOISTURE) > 0;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Plant support — CROP and PLAINS only (same as FD's RichSoilFarmlandBlock)
    // -------------------------------------------------------------------------

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos,
                                   Direction facing, net.minecraftforge.common.IPlantable plantable) {
        PlantType plantType = plantable.getPlantType(level, pos.relative(facing));
        return plantType == PlantType.CROP || plantType == PlantType.PLAINS;
    }

    // -------------------------------------------------------------------------
    // Water detection — copied from FD, uses canBeHydrated + FarmlandWaterManager
    // -------------------------------------------------------------------------

    private static boolean isNearWater(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (state.canBeHydrated(level, pos, level.getFluidState(nearbyPos), nearbyPos)) {
                return true;
            }
        }
        return net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }
}