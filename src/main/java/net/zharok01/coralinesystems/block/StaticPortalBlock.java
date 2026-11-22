package net.zharok01.coralinesystems.block;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zharok01.coralinesystems.event.static_portal.StaticPortalLinkData;
import net.zharok01.coralinesystems.event.static_portal.StaticPortalManager;
import net.zharok01.coralinesystems.event.static_portal.StaticPortalTeleporter;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@SuppressWarnings("deprecation")
public class StaticPortalBlock extends Block {

    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    public static final int PORTAL_TICKS_REQUIRED = 100;

    public StaticPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NetherPortalBlock.AXIS, Direction.Axis.X));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return state.getValue(NetherPortalBlock.AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (entity instanceof Player player) {
            if (!player.canChangeDimensions() || player.isPassenger() || player.isVehicle()) return;

            StaticPortalManager.setInPortal(player);

            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                int progress = StaticPortalManager.getTicksInPortal(player);

                if (progress > 0 && progress < PORTAL_TICKS_REQUIRED && progress % 10 == 0) {
                    float intensity = progress / (float) PORTAL_TICKS_REQUIRED;

                    level.playSound(null, pos,
                            CoralineSounds.STATIC_BUZZ.get(),
                            SoundSource.BLOCKS,
                            0.15F + intensity * 0.55F,
                            1.6F - intensity * 0.4F);

                    serverLevel.sendParticles(
                            AMParticleRegistry.STATIC_SPARK.get(),
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            (int) (5 + intensity * 20),
                            0.3, 0.5, 0.3,
                            0.05 + intensity * 0.1);
                }
            }
        }
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, RandomSource random) {
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
                g = random.nextFloat() * 2.0F * (float)k;
            } else {
                f = (double)pos.getZ() + (double)0.5F + (double)0.25F * (double)k;
                j = random.nextFloat() * 2.0F * (float)k;
            }

            level.addParticle(AMParticleRegistry.STATIC_SPARK.get(), d, e, f, g, h, j);
        }
    }

    @Override
    public void onRemove(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Set<BlockPos> group = StaticPortalTeleporter.findPortalGroup(serverLevel, pos);
            if (!group.isEmpty()) {
                StaticPortalLinkData linkData = StaticPortalLinkData.get(serverLevel);
                BlockPos sisterSpawn = linkData.getLinkedDestination(pos);
                linkData.unlinkGroup(group);
                if (sisterSpawn != null) {
                    Set<BlockPos> sisterGroup = StaticPortalTeleporter.findPortalGroup(serverLevel, sisterSpawn);
                    linkData.unlinkGroup(sisterGroup);
                    linkData.unlinkAllPointingTo(sisterSpawn);
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
