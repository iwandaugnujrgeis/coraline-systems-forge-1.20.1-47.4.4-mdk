package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTags;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class SubwooferBlock extends Block {

    public static final DirectionProperty FACING  = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    private static final int MAX_RANGE = 12;

    private static final int POWERED_TICKS = 20;

    private static final int MAX_BOOM_PUFFS = 4;
    private static final double BOOM_PUFF_START_OFFSET = 1.0;
    private static final double BOOM_PUFF_SPACING = 2.0;
    private static final double BOOM_PUFF_SHRINK = 1.0;

    private static final Predicate<BlockState> IS_FRAGILE = state ->
            state.is(CoralineTags.FRAGILE) ||
                    state.is(BlockTags.LEAVES) ||
                    state.getBlock() == Blocks.FLOWER_POT ||
                    state.getBlock() == Blocks.GLASS ||
                    state.getBlock() == Blocks.GLASS_PANE;

    public SubwooferBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING,  context.getNearestLookingDirection().getOpposite())
                .setValue(POWERED, false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide()) return;

        boolean isPowered = level.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);

        if (isPowered && !wasPowered) {
            int signal = level.getBestNeighborSignal(pos);
            fire(state, level, pos, signal);

            level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
            level.scheduleTick(pos, this, POWERED_TICKS);

        } else if (!isPowered && wasPowered) {
            ServerLevel serverLevel = (ServerLevel) level;
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(@NotNull BlockState state, ServerLevel level, @NotNull BlockPos pos,
                     @NotNull RandomSource random) {
        if (!level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
        }
    }

    private void fire(BlockState state, Level level, BlockPos pos, int signal) {
        Direction dir = state.getValue(FACING);

        int range = 3 + (int) Math.round((MAX_RANGE - 3) * (signal / 15.0));

        double pushStrength = 0.2 + (signal / 15.0) * 0.8;

        int breakBudget = Math.max(1, (int) Math.round(range * (signal / 15.0) * 0.5));

        List<UUID> affected = new ArrayList<>();

        for (int i = 1; i <= range; i++) {
            BlockPos scanPos = pos.relative(dir, i);

            if (level.getBlockState(scanPos).is(BlockTags.OCCLUDES_VIBRATION_SIGNALS)) {
                break;
            }

            BlockState scan = level.getBlockState(scanPos);
            if (!scan.isAir() && IS_FRAGILE.test(scan)) {
                level.destroyBlock(scanPos, true);
                breakBudget--;
                if (breakBudget <= 0) {
                    break;
                }
                continue;
            }

            List<Entity> entities = level.getEntities(
                    (Entity) null,
                    new AABB(scanPos).inflate(0.6),
                    e -> canPush(e) && !affected.contains(e.getUUID())
            );

            for (Entity entity : entities) {
                affected.add(entity.getUUID());

                Vec3 push = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ())
                        .multiply(pushStrength, pushStrength, pushStrength);

                if (dir.getAxis() != Direction.Axis.Y) {
                    push = push.add(0, 0.15, 0);
                }

                entity.push(push.x, push.y, push.z);

                if (entity.getDeltaMovement().y < 0.1 && dir.getAxis() != Direction.Axis.Y) {
                    entity.setDeltaMovement(
                            entity.getDeltaMovement().x,
                            Math.max(entity.getDeltaMovement().y, 0.15),
                            entity.getDeltaMovement().z
                    );
                }

                if (dir == Direction.UP) entity.fallDistance = 0;

                if (entity.isOnFire()) {
                    entity.clearFire();
                }

                entity.hurtMarked = true;
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, pos,
                    CoralineSounds.SUBWOOFER_BOOM.get(), SoundSource.BLOCKS,
                    1.0F, 1.0F);

            Vec3 coraline$origin = Vec3.atCenterOf(pos);
            int coraline$puffCount = Math.max(1, Math.min(range / 3, MAX_BOOM_PUFFS));

            for (int puff = 0; puff < coraline$puffCount; puff++) {
                double distance = BOOM_PUFF_START_OFFSET + puff * BOOM_PUFF_SPACING;
                double x = coraline$origin.x + dir.getStepX() * distance;
                double y = coraline$origin.y + dir.getStepY() * distance;
                double z = coraline$origin.z + dir.getStepZ() * distance;

                serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 0, BOOM_PUFF_SHRINK, 0.0, 0.0, 1.0);
            }
        }

        level.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, pos);
    }

    private static boolean canPush(Entity e) {
        return e.isPushable()
                || e instanceof ItemEntity
                || e instanceof Projectile
                || e instanceof ArmorStand;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return state.getValue(POWERED) ? 15 : 0;
    }
}