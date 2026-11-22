package net.zharok01.coralinesystems.block;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.zharok01.coralinesystems.event.CentrifugeEvent;
import net.zharok01.coralinesystems.item.OrbItem;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import net.zharok01.coralinesystems.event.time.TimeAccelerationManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CentrifugeBlockEntity extends BlockEntity {

    private static final int BLOCKS_CHECK_PER_TICK = 10;

    private static final float[] DEFAULT_BEAM_COLOR = { 1.0f, 1.0f, 1.0f };

    List<BeamSection> beamSections = Lists.newArrayList();
    private List<BeamSection> checkingBeamSections = Lists.newArrayList();
    private int lastCheckY;

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    public static void registerDispenserBehavior(net.minecraft.world.item.Item orbItem) {
        DispenserBlock.registerBehavior(orbItem, new OptionalDispenseItemBehavior() {
            @Override
            protected @NotNull ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
                ServerLevel level = source.getLevel();
                Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos targetPos = source.getPos().relative(facing);

                if (level.getBlockEntity(targetPos) instanceof CentrifugeBlockEntity centrifuge) {
                    boolean started = centrifuge.attemptStartFromDispenser();
                    if (started) {
                        this.setSuccess(true);
                        stack.shrink(1);
                        return stack;
                    }
                }

                this.setSuccess(false);
                return super.execute(source, stack);
            }
        });
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity be) {
        if (!state.getValue(CentrifugeBlock.ACTIVATED)) {
            if (!be.beamSections.isEmpty()) {
                be.beamSections = Lists.newArrayList();
                if (!level.isClientSide()) {
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
            return;
        }

        tickBeamScan(level, pos, be);

        if (!level.isClientSide() && level.getGameTime() % 80L == 0L && !be.beamSections.isEmpty()) {
            level.playSound(null, pos,
                    SoundEvents.BEACON_AMBIENT,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    private static void tickBeamScan(Level level, BlockPos pos, CentrifugeBlockEntity be) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        BlockPos scanPos;
        if (be.lastCheckY < y) {
            scanPos = pos;
            be.checkingBeamSections = Lists.newArrayList();
            be.lastCheckY = y - 1;
        } else {
            scanPos = new BlockPos(x, be.lastCheckY + 1, z);
        }

        BeamSection currentSection = be.checkingBeamSections.isEmpty()
                ? null
                : be.checkingBeamSections.get(be.checkingBeamSections.size() - 1);

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

        for (int i = 0; i < BLOCKS_CHECK_PER_TICK && scanPos.getY() <= surfaceY; i++) {
            BlockState scannedState = level.getBlockState(scanPos);
            float[] color = scannedState.getBeaconColorMultiplier(level, scanPos, pos);

            if (color != null) {
                if (currentSection == null) {
                    currentSection = new BeamSection(color);
                    be.checkingBeamSections.add(currentSection);
                } else if (Arrays.equals(color, currentSection.color)) {
                    currentSection.increaseHeight();
                } else {
                    currentSection = new BeamSection(new float[]{
                            (currentSection.color[0] + color[0]) / 2.0f,
                            (currentSection.color[1] + color[1]) / 2.0f,
                            (currentSection.color[2] + color[2]) / 2.0f
                    });
                    be.checkingBeamSections.add(currentSection);
                }
            } else {
                if (currentSection == null) {
                    currentSection = new BeamSection(DEFAULT_BEAM_COLOR);
                    be.checkingBeamSections.add(currentSection);
                } else {
                    currentSection.increaseHeight();
                }
            }

            scanPos = scanPos.above();
            be.lastCheckY++;
        }

        if (be.lastCheckY >= surfaceY) {
            be.lastCheckY = level.getMinBuildHeight() - 1;
            be.beamSections = be.checkingBeamSections;
            if (!level.isClientSide()) {
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    public static class BeamSection {
        final float[] color;
        private int height;

        public BeamSection(float[] color) {
            this.color = color;
            this.height = 1;
        }

        void increaseHeight() { this.height++; }
        public float[] getColor() { return color; }
        public int getHeight() { return height; }
    }

    public List<BeamSection> getBeamSections() {
        return beamSections;
    }

    public boolean tryActivate(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide()) return false;

        ItemStack heldStack = player.getItemInHand(hand);
        if (!(heldStack.getItem() instanceof OrbItem)) return false;

        if (!canAcceptOrb()) return false;

        boolean started = attemptStart();
        if (!started) return false;

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.CENTRIFUGE_ORB_INSERT.trigger(serverPlayer);
        }

        return true;
    }

    boolean attemptStartFromDispenser() {
        if (level == null || level.isClientSide()) return false;
        if (!canAcceptOrb()) return false;
        return attemptStart();
    }

    private boolean canAcceptOrb() {
        TimeAccelerationManager manager = CentrifugeEvent.getManager();
        return manager == null || !manager.isActive(level.dimension(), worldPosition);
    }

    private boolean attemptStart() {
        if (level == null || level.isClientSide()) return false;

        if (level.dimension() == Level.NETHER) {
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.SMOKE,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    7, 0.3, 0.3, 0.3, 0.02);
            level.playSound(null, worldPosition,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return false;
        }

        TimeAccelerationManager manager = CentrifugeEvent.getManager();
        if (manager == null) return false;

        boolean started = manager.startSession((ServerLevel) level, worldPosition);
        if (!started) return false;

        lastCheckY = level.getMinBuildHeight() - 1;

        level.playSound(null, worldPosition,
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS, 1.0f, 1.0f);

        ((ServerLevel) level).sendParticles(
                ParticleTypes.FLASH,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5,
                1, 0.0, 0.0, 0.0, 0.0);
        return true;
    }
}