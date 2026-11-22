package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.zharok01.coralinesystems.event.ReactorEvent;
import net.zharok01.coralinesystems.item.OrbItem;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.event.ReactorSessionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReactorBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ReactorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ReactorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, CoralineBlockEntities.REACTOR.get(), ReactorBlockEntity::clientTick)
                : createTickerHelper(type, CoralineBlockEntities.REACTOR.get(), ReactorBlockEntity::serverTick);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(
            @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit) {

        if (!(player.getItemInHand(hand).getItem() instanceof OrbItem)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof ReactorBlockEntity reactor) {
            boolean activated = reactor.tryActivate(player, hand);
            return activated ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean isMoving) {
        boolean genuinelyRemoved = !state.is(newState.getBlock());

        if (genuinelyRemoved
                && !level.isClientSide()
                && level instanceof ServerLevel serverLevel
                && state.getValue(ACTIVE)) {

            ReactorSessionManager manager = ReactorEvent.getManager();
            if (manager != null) {
                manager.forceStop(serverLevel, pos);
            }

            coraline$explode(serverLevel, pos);
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    private static void coraline$explode(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0f, (level.random.nextFloat() - level.random.nextFloat()) * 0.2f + 1.0f);

        level.sendParticles(ParticleTypes.EXPLOSION, cx, cy, cz, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy, cz, 30, 0.4, 0.4, 0.4, 0.05);

        level.explode(null, cx, cy, cz, 2.25f, true, Level.ExplosionInteraction.TNT);
    }
}