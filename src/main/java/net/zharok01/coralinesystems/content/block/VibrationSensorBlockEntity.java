package net.zharok01.coralinesystems.content.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.slf4j.Logger;

public class VibrationSensorBlockEntity extends BlockEntity
        implements GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem {

    private static final Logger LOGGER = LogUtils.getLogger();

    private VibrationSystem.Data vibrationData;
    private final VibrationSystem.Listener vibrationListener;
    private final VibrationSystem.User vibrationUser;

    /**
     * Kept as a field so onDataChanged() can access the level.
     * Set during the first onDataChanged() call where getLevel() is non-null.
     */
    @Nullable
    private ServerLevel cachedLevel = null;

    public VibrationSensorBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.VIBRATION_SENSOR.get(), pos, state);
        this.vibrationData     = new VibrationSystem.Data();
        this.vibrationUser     = new SensorVibrationUser(pos);
        this.vibrationListener = new VibrationSystem.Listener(this);
    }

    // -------------------------------------------------------------------------
    // VibrationSystem
    // -------------------------------------------------------------------------

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    @Override
    public VibrationSystem.Listener getListener() {
        return this.vibrationListener;
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("listener", 10)) {
            VibrationSystem.Data.CODEC
                    .parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("listener")))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(data -> this.vibrationData = data);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        VibrationSystem.Data.CODEC
                .encodeStart(NbtOps.INSTANCE, this.vibrationData)
                .resultOrPartial(LOGGER::error)
                .ifPresent(nbt -> tag.put("listener", nbt));
    }

    // -------------------------------------------------------------------------
    // Inner VibrationUser
    // -------------------------------------------------------------------------

    protected class SensorVibrationUser implements VibrationSystem.User {

        private static final int LISTENER_RANGE = 8;

        private final BlockPos blockPos;
        private final PositionSource positionSource;

        public SensorVibrationUser(BlockPos pos) {
            this.blockPos       = pos;
            this.positionSource = new BlockPositionSource(pos);
        }

        @Override
        public int getListenerRadius() {
            return LISTENER_RANGE;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, GameEvent gameEvent,
                                           @Nullable GameEvent.Context context) {
            return VibrationSensorBlock.canActivate(
                    VibrationSensorBlockEntity.this.getBlockState());
        }

        @Override
        public void onReceiveVibration(ServerLevel level, BlockPos pos, GameEvent gameEvent,
                                       @Nullable Entity entity, @Nullable Entity playerEntity,
                                       float distance) {
            BlockState state = VibrationSensorBlockEntity.this.getBlockState();
            if (VibrationSensorBlock.canActivate(state)) {
                if (state.getBlock() instanceof VibrationSensorBlock sensor) {
                    sensor.activate(entity, level, this.blockPos, state);
                }
            }
        }

        /**
         * Called by VibrationSystem.Ticker the moment a vibration is selected
         * and the travelling particle is spawned — the perfect place for the
         * "something detected" audio cue, before the signal actually fires.
         *
         * We cache the level reference here because getLevel() on the block
         * entity can occasionally be null during early load; by the time
         * onDataChanged() is called from the Ticker the level is always live.
         */
        @Override
        public void onDataChanged() {
            VibrationSensorBlockEntity.this.setChanged();

            // Cache the level so we can play sounds server-side
            if (VibrationSensorBlockEntity.this.level instanceof ServerLevel sl) {
                cachedLevel = sl;
            }

            // Only play the detection ping when a vibration has just been picked up
            // (currentVibration becomes non-null at that moment). Ignore the
            // subsequent call where it is cleared back to null after the signal fires.
            if (cachedLevel != null
                    && VibrationSensorBlockEntity.this.vibrationData.getCurrentVibration() != null) {

                BlockPos p = VibrationSensorBlockEntity.this.getBlockPos();
                cachedLevel.playSound(
                        null,
                        p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                        CoralineSounds.VIBRATION_SENSOR_PING.get(),
                        SoundSource.BLOCKS,
                        1.0F,
                        0.8F + cachedLevel.random.nextFloat() * 0.4F
                );
            }
        }

        @Override
        public boolean requiresAdjacentChunksToBeTicking() {
            return true;
        }
    }
}