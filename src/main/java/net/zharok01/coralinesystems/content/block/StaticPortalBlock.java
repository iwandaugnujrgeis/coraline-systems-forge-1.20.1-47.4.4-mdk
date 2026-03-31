package net.zharok01.coralinesystems.content.block;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.util.StaticPortalLinkData;
import net.zharok01.coralinesystems.util.StaticTeleporter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StaticPortalBlock extends Block {

    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    private static final Map<UUID, Long> lastPortalTick = new HashMap<>();
    private static final Map<UUID, Integer> portalProgress = new HashMap<>();

    /**
     * 100 ticks = 5 seconds. Public so ClientPortalEffect can reference it.
     */
    public static final int PORTAL_TICKS_REQUIRED = 100;

    public StaticPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NetherPortalBlock.AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(NetherPortalBlock.AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    /**
     * Portal blocks must be walk-through, exactly like vanilla Nether portals.
     * Without this override, the player physically cannot enter the portal.
     */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) return;
        if (!entity.canChangeDimensions() || entity.isPassenger() || entity.isVehicle()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        UUID id = entity.getUUID();
        long currentTick = level.getGameTime();
        Long lastTick = lastPortalTick.get(id);

        // Gap > 1 tick means the entity stepped out — reset the counter
        if (lastTick == null || currentTick - lastTick > 1L) {
            portalProgress.put(id, 0);
        }

        lastPortalTick.put(id, currentTick);
        int progress = portalProgress.getOrDefault(id, 0) + 1;
        portalProgress.put(id, progress);

        // Progressive sound and particle feedback every 10 ticks
        if (progress % 10 == 0) {
            float intensity = progress / (float) PORTAL_TICKS_REQUIRED;

            level.playSound(null, pos,
                    CoralineSounds.STATIC_BUZZ.get(),
                    SoundSource.BLOCKS,
                    0.15F + intensity * 0.55F,
                    1.6F  - intensity * 0.4F);

            serverLevel.sendParticles(
                    AMParticleRegistry.STATIC_SPARK.get(),
                    entity.getX(), entity.getY() + 1.0, entity.getZ(),
                    (int) (5 + intensity * 20),
                    0.3, 0.5, 0.3,
                    0.05 + intensity * 0.1);
        }

        if (progress >= PORTAL_TICKS_REQUIRED) {
            lastPortalTick.remove(id);
            portalProgress.remove(id);
            StaticTeleporter.teleportToFarlands(entity, serverLevel, pos);
        }
    }

    /**
     * When a Static portal block is broken, clean up its link registration
     * so the sister portal doesn't remain permanently locked.
     *
     * We find the full connected group (before the block has been removed,
     * so it's still in the level), unlink it, and also unlink any blocks
     * that pointed BACK to this group from the sister side.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // The block at pos is still in the level at this moment, so BFS still works
            Set<BlockPos> group = StaticTeleporter.findPortalGroup(serverLevel, pos);
            if (!group.isEmpty()) {
                StaticPortalLinkData linkData = StaticPortalLinkData.get(serverLevel);
                // Find where this group pointed (the sister's spawn pos)
                // and collect sister blocks to unlink them too
                // (We pick any block in the group — they all share the same destination)
                BlockPos sisterSpawn = linkData.getLinkedDestination(pos);
                if (sisterSpawn != null) {
                    Set<BlockPos> sisterGroup = StaticTeleporter.findPortalGroup(serverLevel, sisterSpawn);
                    linkData.unlinkGroup(sisterGroup);
                }
                linkData.unlinkGroup(group);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NetherPortalBlock.AXIS);
    }
}
