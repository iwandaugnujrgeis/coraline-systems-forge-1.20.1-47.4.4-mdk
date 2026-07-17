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
 * <p>
 * SESSION 2.5 ("Phantom Liquid Trap" fix): {@code waterLevel} is GONE from
 * here -- water volume now lives on {@link BrewingCauldronBlock#LEVEL}
 * (blockstate), matching vanilla LayeredCauldronBlock's own approach, so
 * the liquid plane visually drops per-bottle using vanilla's existing
 * water_cauldron_levelN models. In its place, {@link #solidStrength} (1-5)
 * now lives here -- the Mulberry/Tea Leaf count never affected physical
 * shape, only (eventually) tint, so it belongs on the BE per the
 * blockstate-is-shape / BE-is-data split. See BrewingCauldronBlock's class
 * javadoc for the full rationale.
 */
public class BrewingCauldronBlockEntity extends BlockEntity {

    private static final String TAG_CULTURE = "Culture";
    private static final String TAG_IMPLIED_CULTURE = "ImpliedCulture";
    private static final String TAG_PROGRESS = "BrewProgress";
    private static final String TAG_SOLID_STRENGTH = "SolidStrength";
    private static final String TAG_BREW_STATE = "BrewState";

    public static final int MIN_SOLID_STRENGTH = BrewingCauldronBlock.MIN_SOLID_LEVEL; // 1
    public static final int MAX_SOLID_STRENGTH = BrewingCauldronBlock.MAX_SOLID_LEVEL; // 5

    private CultureType culture = CultureType.NONE;
    private CultureType impliedCulture = CultureType.NONE;
    private long brewProgress = 0L;
    private int solidStrength = MIN_SOLID_STRENGTH;
    private BrewState brewState = BrewState.BREWING;

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

    /**
     * Solid-ingredient strength, 1-5. Formerly BrewingCauldronBlock.LEVEL;
     * moved here in the Phantom Liquid Trap fix -- see class javadoc.
     */
    public int getSolidStrength() {
        return solidStrength;
    }

    public void setSolidStrength(int solidStrength) {
        this.solidStrength = Math.max(MIN_SOLID_STRENGTH, Math.min(MAX_SOLID_STRENGTH, solidStrength));
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

    public BrewState getBrewState() {
        return brewState;
    }

    public void setBrewState(BrewState brewState) {
        this.brewState = brewState;
        this.sync();
    }

    public void reset() {
        this.culture = CultureType.NONE;
        this.impliedCulture = CultureType.NONE;
        this.brewProgress = 0L;
        this.solidStrength = MIN_SOLID_STRENGTH;
        this.brewState = BrewState.BREWING;
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
        if (tag.contains(TAG_SOLID_STRENGTH)) {
            solidStrength = tag.getInt(TAG_SOLID_STRENGTH);
        }
        if (tag.contains(TAG_BREW_STATE)) {
            try {
                brewState = BrewState.valueOf(tag.getString(TAG_BREW_STATE));
            } catch (IllegalArgumentException e) {
                brewState = BrewState.BREWING;
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_CULTURE, culture.name());
        tag.putString(TAG_IMPLIED_CULTURE, impliedCulture.name());
        tag.putLong(TAG_PROGRESS, brewProgress);
        tag.putInt(TAG_SOLID_STRENGTH, solidStrength);
        tag.putString(TAG_BREW_STATE, brewState.name());
    }
}