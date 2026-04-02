package net.zharok01.coralinesystems.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StaticPortalBlock extends Block {

    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    /**
     * STATIC MAP CROSS-WORLD SAFETY — same problem as StaticTeleporter:
     * These maps persist across world loads in the same JVM session.
     * If lastPortalTick stores a tick from World A, and World B starts at a
     * lower tick, (currentTick - lastTick) becomes negative. Negative is NOT > 1L,
     * so the "entity stepped out" reset never fires — the countdown appears stuck
     * or never fires correctly. Fix: treat a negative diff as a fresh entry too.
     */
    private static final Map<UUID, Long> lastPortalTick  = new HashMap<>();
    private static final Map<UUID, Integer> portalProgress = new HashMap<>();

    public static final int PORTAL_TICKS_REQUIRED = 100;

    public StaticPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NetherPortalBlock.AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(NetherPortalBlock.AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

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

        // Reset if: first entry, OR entity stepped out (gap > 1 tick),
        // OR the diff is negative (loaded a new world with lower game time — same cross-world bug).
        if (lastTick == null) {
            portalProgress.put(id, 0);
        } else {
            long diff = currentTick - lastTick;
            if (diff > 1L || diff < 0L) {
                portalProgress.put(id, 0);
            }
        }

        lastPortalTick.put(id, currentTick);
        int progress = portalProgress.getOrDefault(id, 0) + 1;
        portalProgress.put(id, progress);

        // ---------------------------------------------------------------
        // Teleport check FIRST — before feedback code.
        // Both checks share progress==100 (since 100 % 10 == 0). If feedback
        // threw an exception, it would swallow the teleport. Checking teleport
        // first and returning early avoids that entirely.
        // ---------------------------------------------------------------
        if (progress >= PORTAL_TICKS_REQUIRED) {
            lastPortalTick.remove(id);
            portalProgress.remove(id);
            StaticTeleporter.teleportToFarlands(entity, serverLevel, pos);
            return;
        }

        // Progressive sound and particle feedback
        if (progress % 10 == 0) {
            float intensity = progress / (float) PORTAL_TICKS_REQUIRED;

            level.playSound(null, pos,
                    CoralineSounds.STATIC_BUZZ.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.15F + intensity * 0.55F,
                    1.6F  - intensity * 0.4F);

            serverLevel.sendParticles(
                    AMParticleRegistry.STATIC_SPARK.get(),
                    entity.getX(), entity.getY() + 1.0, entity.getZ(),
                    (int) (5 + intensity * 20),
                    0.3, 0.5, 0.3,
                    0.05 + intensity * 0.1);
        }
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, CoralineSounds.STATIC_PORTAL_AMBIENT.get(), SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        for(int i = 0; i < 4; ++i) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + random.nextDouble();
            double f = (double)pos.getZ() + random.nextDouble();
            double g = ((double)random.nextFloat() - (double)0.5F) * (double)0.5F;
            double h = ((double)random.nextFloat() - (double)0.5F) * (double)0.5F;
            double j = ((double)random.nextFloat() - (double)0.5F) * (double)0.5F;
            int k = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                d = (double)pos.getX() + (double)0.5F + (double)0.25F * (double)k;
                g = (double)(random.nextFloat() * 2.0F * (float)k);
            } else {
                f = (double)pos.getZ() + (double)0.5F + (double)0.25F * (double)k;
                j = (double)(random.nextFloat() * 2.0F * (float)k);
            }

            level.addParticle(AMParticleRegistry.STATIC_SPARK.get(), d, e, f, g, h, j);
        }

    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Set<BlockPos> group = StaticTeleporter.findPortalGroup(serverLevel, pos);
            if (!group.isEmpty()) {
                StaticPortalLinkData linkData = StaticPortalLinkData.get(serverLevel);
                if (linkData != null) {
                    BlockPos sisterSpawn = linkData.getLinkedDestination(pos);
                    if (sisterSpawn != null) {
                        Set<BlockPos> sisterGroup = StaticTeleporter.findPortalGroup(serverLevel, sisterSpawn);
                        linkData.unlinkGroup(sisterGroup);
                    }
                    linkData.unlinkGroup(group);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NetherPortalBlock.AXIS);
    }
}