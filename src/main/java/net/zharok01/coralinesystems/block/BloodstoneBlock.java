package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineTags;

public class BloodstoneBlock extends Block {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    private static final int IDLE_LIGHT = 1;
    private static final int MAX_LIGHT = 15;
    private static final double DETECTION_RANGE = 16.0;
    private static final int STEP_GRID_TICKS = 3;
    private static final int FADE_IN_STEP_TICKS = STEP_GRID_TICKS;
    private static final int FADE_OUT_STEP_TICKS = STEP_GRID_TICKS * 2;
    public static final int SETTLED_SCAN_TICKS = STEP_GRID_TICKS * 3;

    public BloodstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, IDLE_LIGHT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock()) && !level.isClientSide) {
            coraline$scheduleAligned(level, pos, this, FADE_IN_STEP_TICKS);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Vec3 center = pos.getCenter();
        double nearestDistanceSqr = DETECTION_RANGE * DETECTION_RANGE;
        boolean found = false;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(DETECTION_RANGE),
                candidate -> candidate.isAlive() && !candidate.isSpectator() && candidate.getType().is(CoralineTags.BLEEDS))) {
            double distanceSqr = entity.distanceToSqr(center);
            if (distanceSqr < nearestDistanceSqr) {
                nearestDistanceSqr = distanceSqr;
                found = true;
            }
        }
        int targetLight = IDLE_LIGHT;
        if (found) {
            double closeness = 1.0 - Math.sqrt(nearestDistanceSqr) / DETECTION_RANGE;
            targetLight = IDLE_LIGHT + (int) (closeness * (MAX_LIGHT - IDLE_LIGHT));
        }
        int currentLight = state.getValue(LEVEL);
        int nextLight = currentLight + Integer.signum(targetLight - currentLight);
        if (nextLight != currentLight) {
            level.setBlock(pos, state.setValue(LEVEL, nextLight), Block.UPDATE_ALL);
        }
        int nextDelay = SETTLED_SCAN_TICKS;
        if (nextLight < targetLight) {
            nextDelay = FADE_IN_STEP_TICKS;
        } else if (nextLight > targetLight) {
            nextDelay = FADE_OUT_STEP_TICKS;
        }
        coraline$scheduleAligned(level, pos, this, nextDelay);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LEVEL) - IDLE_LIGHT;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LEVEL) > IDLE_LIGHT) {
            coraline$spawnParticles(level, pos, random);
        }
    }

    private static void coraline$spawnParticles(Level level, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            if (!level.getBlockState(adjacentPos).isSolidRender(level, adjacentPos)) {
                double x = direction.getAxis() == Direction.Axis.X
                        ? 0.5 + 0.5625 * direction.getStepX()
                        : random.nextFloat();
                double y = direction.getAxis() == Direction.Axis.Y
                        ? 0.5 + 0.5625 * direction.getStepY()
                        : random.nextFloat();
                double z = direction.getAxis() == Direction.Axis.Z
                        ? 0.5 + 0.5625 * direction.getStepZ()
                        : random.nextFloat();
                level.addParticle(DustParticleOptions.REDSTONE, pos.getX() + x, pos.getY() + y, pos.getZ() + z, 0.0, 0.0, 0.0);
            }
        }
    }

    public static void coraline$scheduleAligned(LevelAccessor level, BlockPos pos, Block block, int intervalTicks) {
        long now = level.getLevelData().getGameTime();
        long alignedTime = (now + intervalTicks) / STEP_GRID_TICKS * STEP_GRID_TICKS;
        level.scheduleTick(pos, block, (int) Math.max(1, alignedTime - now));
    }
}
