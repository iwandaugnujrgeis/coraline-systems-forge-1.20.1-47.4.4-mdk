package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds all state for an in-progress brew (Wine or Kombucha) beyond what
 * lives in the blockstate.
 */
public class BrewingCauldronBlockEntity extends BlockEntity {

    private static final String TAG_CULTURE = "Culture";
    private static final String TAG_IMPLIED_CULTURE = "ImpliedCulture";
    private static final String TAG_PROGRESS = "BrewProgress";
    private static final String TAG_WATER_LEVEL = "WaterLevel";

    public static final int MAX_WATER_LEVEL = 3;
    public static final int MIN_WATER_LEVEL = 0;

    private CultureType culture = CultureType.NONE;
    private CultureType impliedCulture = CultureType.NONE;
    private long brewProgress = 0L;
    private int waterLevel = MAX_WATER_LEVEL;

    public BrewingCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.BREWING_CAULDRON.get(), pos, state);
    }

    // ── Client Syncing ───────────────────────────────────────────────────

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    /**
     * Marks the BlockEntity as changed and sends an update packet to the client.
     * This entirely prevents the "empty bucket" desync!
     */
    private void sync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // ── Accessors ────────────────────────────────────────────────────────

    public CultureType getCulture() {
        return culture;
    }

    public CultureType getImpliedCulture() {
        return impliedCulture;
    }

    public void setImpliedCulture(CultureType impliedCulture) {
        this.impliedCulture = impliedCulture;
        this.sync();
    }

    public void setCulture(CultureType culture) {
        this.culture = culture;
        this.sync();
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = Math.max(MIN_WATER_LEVEL, Math.min(MAX_WATER_LEVEL, waterLevel));
        this.sync();
    }

    public long getBrewProgress() {
        return brewProgress;
    }

    public void setBrewProgress(long brewProgress) {
        this.brewProgress = brewProgress;
        this.sync();
    }

    public void addBrewProgress(long amount) {
        this.brewProgress += amount;
        this.sync();
    }

    public void reset() {
        this.culture = CultureType.NONE;
        this.impliedCulture = CultureType.NONE;
        this.brewProgress = 0L;
        this.waterLevel = MAX_WATER_LEVEL;
        this.sync();
    }

    // ── Persistence ──────────────────────────────────────────────────────

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_CULTURE)) {
            try {
                culture = CultureType.valueOf(tag.getString(TAG_CULTURE));
            } catch (IllegalArgumentException e) {
                culture = CultureType.NONE;
            }
        }
        if (tag.contains(TAG_IMPLIED_CULTURE)) {
            try {
                impliedCulture = CultureType.valueOf(tag.getString(TAG_IMPLIED_CULTURE));
            } catch (IllegalArgumentException e) {
                impliedCulture = CultureType.NONE;
            }
        }
        if (tag.contains(TAG_PROGRESS)) {
            brewProgress = tag.getLong(TAG_PROGRESS);
        }
        if (tag.contains(TAG_WATER_LEVEL)) {
            waterLevel = tag.getInt(TAG_WATER_LEVEL);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_CULTURE, culture.name());
        tag.putString(TAG_IMPLIED_CULTURE, impliedCulture.name());
        tag.putLong(TAG_PROGRESS, brewProgress);
        tag.putInt(TAG_WATER_LEVEL, waterLevel);
    }
}