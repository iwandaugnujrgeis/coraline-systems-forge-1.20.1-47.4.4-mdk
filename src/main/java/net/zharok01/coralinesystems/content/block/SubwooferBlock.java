package net.zharok01.coralinesystems.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Subwoofer — a redstone-powered directional air-blast block.
 *
 * MECHANICS:
 *   - Place it facing any direction. On a rising redstone edge, it fires a
 *     compressed air blast forward.
 *   - The blast pushes entities (mobs, players, items, projectiles, armor stands)
 *     away from the face of the block.
 *   - Push STRENGTH scales with redstone signal (1-15). Full signal = maximum push.
 *   - Push RANGE also scales: minimum 3 blocks at signal 1, up to MAX_RANGE at 15.
 *   - Fragile blocks (glass, glass panes, flower pots) in the blast path are broken.
 *   - Entities on fire have their fire extinguished by the blast.
 *   - POWERED state lingers for POWERED_TICKS after firing so the blockstate
 *     visually shows a "recharging" state before it can accept another pulse.
 *
 * REGISTRATION:
 *   Add to CoralineBlocks as a standard block with your chosen properties.
 *   Assign it a BlockItem. No BlockEntity needed.
 */
public class SubwooferBlock extends Block {

    public static final DirectionProperty FACING  = BlockStateProperties.FACING;
    public static final BooleanProperty   POWERED = BooleanProperty.create("powered");

    /** Maximum blast range in blocks (at signal strength 15). */
    private static final int MAX_RANGE = 12;

    /**
     * How many ticks the POWERED=true state lingers after firing before the
     * block accepts a new pulse. Prevents spam-firing on a fast clock.
     * 20 ticks = 1 second.
     */
    private static final int POWERED_TICKS = 20;

    /**
     * These block types are "fragile" and shatter when the blast wave passes through.
     * We check by tag rather than hardcoding specific blocks so modded glass etc. works.
     */
    private static final java.util.function.Predicate<BlockState> IS_FRAGILE = state ->
            state.is(CoralineTags.FRAGILE) ||          // glass, stained glass
                    state.is(net.minecraft.tags.BlockTags.LEAVES) ||
                    state.getBlock() == net.minecraft.world.level.block.Blocks.FLOWER_POT ||
                    state.getBlock() == net.minecraft.world.level.block.Blocks.GLASS ||
                    state.getBlock() == net.minecraft.world.level.block.Blocks.GLASS_PANE;

    public SubwooferBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING,  Direction.NORTH)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Face the direction the player is looking FROM so the blast fires
        // away from the player — intuitive placement.
        return this.defaultBlockState()
                .setValue(FACING,  context.getNearestLookingDirection().getOpposite())
                .setValue(POWERED, false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Redstone integration
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide()) return;

        boolean isPowered = level.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);

        if (isPowered && !wasPowered) {
            // Rising edge: Redstone turned ON. Fire the subwoofer!
            int signal = level.getBestNeighborSignal(pos);
            fire(state, level, pos, signal);

            // Set state to powered and schedule the cooldown
            level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
            level.scheduleTick(pos, this, POWERED_TICKS);

        } else if (!isPowered && wasPowered) {
            // Falling edge: Redstone just turned OFF!
            ServerLevel serverLevel = (ServerLevel) level;

            // Only reset to unpowered if our 20-tick cooldown has already finished.
            // If the cooldown tick is still pending, we do nothing and let the tick() method handle the reset later.
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos,
                     net.minecraft.util.RandomSource random) {
        // The 20-tick scheduled cooldown has finished.
        // Check if the redstone signal is ALREADY off. If so, we can reset safely.
        // If the redstone is still ON (like a lever), neighborChanged will catch it later when it's flicked off.
        if (!level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Blast logic
    // ─────────────────────────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────────────────────────
    // Blast logic
    // ─────────────────────────────────────────────────────────────────────────

    private void fire(BlockState state, Level level, BlockPos pos, int signal) {
        Direction dir = state.getValue(FACING);

        // Range: 3 blocks at signal 1, scaling up to MAX_RANGE at signal 15
        int range = 3 + (int) Math.round((MAX_RANGE - 3) * (signal / 15.0));

        // Push multiplier: scales 0.2 → 1.0 with signal
        double pushStrength = 0.2 + (signal / 15.0) * 0.8;

        // Breaking budget: how many fragile blocks the blast can punch through.
        // Scales from 1 at signal 1 to range/2 at signal 15.
        int breakBudget = Math.max(1, (int) Math.round(range * (signal / 15.0) * 0.5));

        List<UUID> affected = new ArrayList<>();

        for (int i = 1; i <= range; i++) {
            BlockPos scanPos = pos.relative(dir, i);

            // Vibration-occlusion blocks stop the blast (same logic as original)
            if (level.getBlockState(scanPos).is(BlockTags.OCCLUDES_VIBRATION_SIGNALS)) {
                break;
            }

            // --- Break fragile blocks in the path ---
            BlockState scan = level.getBlockState(scanPos);
            if (!scan.isAir() && IS_FRAGILE.test(scan)) {
                level.destroyBlock(scanPos, true);
                breakBudget--;

                if (breakBudget <= 0) {
                    break; // Budget exhausted — blast absorbed
                }

                continue; // Budget remaining — blast continues to the next block
            }

            // --- Push entities ---
            List<Entity> entities = level.getEntities(
                    (Entity) null,
                    new AABB(scanPos).inflate(0.6),
                    e -> canPush(e) && !affected.contains(e.getUUID())
            );

            for (Entity entity : entities) {
                affected.add(entity.getUUID());

                Vec3 push = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ())
                        .multiply(pushStrength, pushStrength, pushStrength);

                // Small upward bias so entities become airborne rather than
                // sliding along the ground, matching the original's feel
                if (dir.getAxis() != Direction.Axis.Y) {
                    push = push.add(0, 0.15, 0);
                }

                entity.push(push.x, push.y, push.z);

                // Prevent negative vertical velocity cancelling the upward push
                if (entity.getDeltaMovement().y < 0.1 && dir.getAxis() != Direction.Axis.Y) {
                    entity.setDeltaMovement(
                            entity.getDeltaMovement().x,
                            Math.max(entity.getDeltaMovement().y, 0.15),
                            entity.getDeltaMovement().z
                    );
                }

                // Stop fall damage if blasted upward
                if (dir == Direction.UP) entity.fallDistance = 0;

                // --- Extinguish fire ---
                // The blast is a gust of compressed air — it blows out fire.
                if (entity.isOnFire()) {
                    entity.clearFire();
                }

                entity.hurtMarked = true; // flag for movement sync to clients
            }
        }

        // --- Sound and particles (server-side, sent to clients) ---
        if (level instanceof ServerLevel serverLevel) {
            // Boom at the face of the block
            serverLevel.playSound(null, pos,
                    CoralineSounds.SUBWOOFER_BOOM.get(), SoundSource.BLOCKS,
                    1.0F, 1.0F);

            // Trail of sonic boom particles along the blast path
            for (int i = 1; i <= Math.min(range, 8); i++) {
                BlockPos particlePos = pos.relative(dir, i);
                serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        particlePos.getX() + 0.5,
                        particlePos.getY() + 0.5,
                        particlePos.getZ() + 0.5,
                        1, 0, 0, 0, 0
                );
            }
        }

        // Emit a game event so sculk sensors nearby react
        level.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, pos);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Which entity types the blast affects.
     * Living entities and players are covered by isPushable().
     */
    private static boolean canPush(Entity e) {
        return e.isPushable()
                || e instanceof ItemEntity
                || e instanceof Projectile
                || e instanceof ArmorStand;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        // Comparators read 15 while firing, 0 while idle — useful for automation
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(POWERED) ? 15 : 0;
    }
}