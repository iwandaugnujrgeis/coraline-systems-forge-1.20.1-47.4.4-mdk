package net.zharok01.coralinesystems.content.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import net.zharok01.coralinesystems.registry.CoralineSounds;

public class VibrationSensorBlock extends BaseEntityBlock {

    public static final int ACTIVE_TICKS   = 30;
    public static final int COOLDOWN_TICKS = 10;
    public static final int SIGNAL_POWER   = 15;

    public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;

    // FIX: full 16×16×16 cube instead of the previous 16×8×16 slab shape
    protected static final VoxelShape SHAPE = Shapes.block();

    public VibrationSensorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, SculkSensorPhase.INACTIVE));
    }

    // -------------------------------------------------------------------------
    // Block entity
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VibrationSensorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide
                ? createTickerHelper(
                type,
                CoralineBlockEntities.VIBRATION_SENSOR.get(),
                (lvl, pos, bState, be) -> VibrationSystem.Ticker.tick(lvl, be.getVibrationData(), be.getVibrationUser())
        )
                : null;
    }

    // -------------------------------------------------------------------------
    // Phase helpers
    // -------------------------------------------------------------------------

    public static SculkSensorPhase getPhase(BlockState state) {
        return state.getValue(PHASE);
    }

    public static boolean canActivate(BlockState state) {
        return getPhase(state) == SculkSensorPhase.INACTIVE;
    }

    public void activate(@Nullable Entity entity, Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(PHASE, SculkSensorPhase.ACTIVE), 3);
        level.scheduleTick(pos, this, ACTIVE_TICKS);
        updateNeighbours(level, pos, state);

        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                CoralineSounds.DETECTOR_ACTIVATED.get(), SoundSource.BLOCKS, 0.8F,
                level.random.nextFloat() * 0.2F + 0.8F);
    }

    public static void deactivate(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(PHASE, SculkSensorPhase.COOLDOWN), 3);
        level.scheduleTick(pos, state.getBlock(), COOLDOWN_TICKS);
        updateNeighbours(level, pos, state);
    }

    private static void updateNeighbours(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        level.updateNeighborsAt(pos, block);
        level.updateNeighborsAt(pos.below(), block);
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (getPhase(state) == SculkSensorPhase.ACTIVE) {
            deactivate(level, pos, state);
        } else if (getPhase(state) == SculkSensorPhase.COOLDOWN) {
            level.setBlock(pos, state.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
        }
    }

    // -------------------------------------------------------------------------
    // Redstone
    // -------------------------------------------------------------------------

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getPhase(state) == SculkSensorPhase.ACTIVE ? SIGNAL_POWER : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return direction == Direction.UP ? getSignal(state, level, pos, direction) : 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return false;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.setBlock(pos, state.setValue(PHASE, SculkSensorPhase.INACTIVE), 18);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (getPhase(state) == SculkSensorPhase.ACTIVE) {
                updateNeighbours(level, pos, state);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }
}