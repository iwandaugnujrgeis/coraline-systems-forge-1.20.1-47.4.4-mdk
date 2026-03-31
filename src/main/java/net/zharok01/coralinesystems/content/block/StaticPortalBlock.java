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
import net.zharok01.coralinesystems.util.StaticTeleporter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaticPortalBlock extends Block {

    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    // The last game-tick we saw each entity standing in a static portal.
    // If there's a gap of more than 1 tick, the entity left and the counter resets.
    private static final Map<UUID, Long> lastPortalTick = new HashMap<>();

    // How many consecutive ticks each entity has stood in the portal.
    private static final Map<UUID, Integer> portalProgress = new HashMap<>();

    /**
     * 100 ticks = 5 seconds at 20 TPS.
     * This value is public so ClientPortalEffect can read it and match the overlay timing.
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
     * Portal blocks must be walk-through, exactly like vanilla nether portals.
     * Without this override, getCollisionShape falls back to getShape(), making them solid walls
     * that the player physically cannot walk into.
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

        // A gap larger than 1 tick means the entity stepped out — fresh entry, reset the counter.
        if (lastTick == null || currentTick - lastTick > 1L) {
            portalProgress.put(id, 0);
        }

        lastPortalTick.put(id, currentTick);
        int progress = portalProgress.getOrDefault(id, 0) + 1;
        portalProgress.put(id, progress);

        // Every 10 ticks, intensify sound and particle feedback to signal the charge building up
        if (progress % 10 == 0) {
            float intensity = progress / (float) PORTAL_TICKS_REQUIRED;

            level.playSound(null, pos,
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.BLOCKS,
                    0.15F + intensity * 0.55F,   // volume grows from quiet to loud
                    1.6F - intensity * 0.4F);     // pitch drops as the charge builds

            serverLevel.sendParticles(
                    AMParticleRegistry.STATIC_SPARK.get(),
                    entity.getX(), entity.getY() + 1.0, entity.getZ(),
                    (int) (5 + intensity * 20),   // more sparks the closer to teleport
                    0.3, 0.5, 0.3,
                    0.05 + intensity * 0.1);
        }

        // Fully charged — clean up tracking and fire the teleport
        if (progress >= PORTAL_TICKS_REQUIRED) {
            lastPortalTick.remove(id);
            portalProgress.remove(id);
            StaticTeleporter.teleportToFarlands(entity, serverLevel, pos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NetherPortalBlock.AXIS);
    }
}
