package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlockEntity extends BlockEntity {

    private static final String TAG_CULTURE = "Culture";
    private static final String TAG_IMPLIED_CULTURE = "ImpliedCulture";
    private static final String TAG_PROGRESS = "BrewProgress";
    private static final String TAG_SOLID_STRENGTH = "SolidStrength";
    private static final String TAG_BREW_STATE = "BrewState";

    public static final int MIN_SOLID_STRENGTH = BrewingCauldronBlock.MIN_SOLID_LEVEL;
    public static final int MAX_SOLID_STRENGTH = BrewingCauldronBlock.MAX_SOLID_LEVEL;

    private static final int SPLASH_COOLDOWN_TICKS = 8;

    private CultureType culture = CultureType.NONE;
    private CultureType impliedCulture = CultureType.NONE;
    private long brewProgress = 0L;
    private int solidStrength = MIN_SOLID_STRENGTH;
    private BrewState brewState = BrewState.BREWING;

    private long lastSplashGameTime = Long.MIN_VALUE;

    public BrewingCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.BREWING_CAULDRON.get(), pos, state);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    private BlockState coraline$displayState() {
        return BrewingCauldronBlock.coraline$withDisplay(getBlockState(), culture, impliedCulture, brewState, solidStrength);
    }

    public void coraline$refreshState() {
        if (level == null || level.isClientSide) return;

        BlockState updated = coraline$displayState();
        if (updated != getBlockState()) {
            level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        }
    }

    public void syncToClient() {
        coraline$refreshState();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel && coraline$displayState() != getBlockState()) {
            serverLevel.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    public CultureType getCulture() {
        return culture;
    }

    public CultureType getImpliedCulture() {
        return impliedCulture;
    }

    public void setImpliedCulture(CultureType impliedCulture) {
        this.impliedCulture = impliedCulture;
        syncToClient();
    }

    public void setCulture(CultureType culture) {
        this.culture = culture;
        syncToClient();
    }

    public int getSolidStrength() {
        return solidStrength;
    }

    public void setSolidStrength(int solidStrength) {
        this.solidStrength = Math.max(MIN_SOLID_STRENGTH, Math.min(MAX_SOLID_STRENGTH, solidStrength));
        syncToClient();
    }

    public long getBrewProgress() {
        return brewProgress;
    }

    public void setBrewProgress(long brewProgress) {
        this.brewProgress = brewProgress;
        setChanged();
    }

    public void addBrewProgress(long amount) {
        this.brewProgress += amount;
        setChanged();
    }

    public BrewState getBrewState() {
        return brewState;
    }

    public void setBrewState(BrewState brewState) {
        this.brewState = brewState;
        syncToClient();
    }

    public void reset() {
        this.culture = CultureType.NONE;
        this.impliedCulture = CultureType.NONE;
        this.brewProgress = 0L;
        this.solidStrength = MIN_SOLID_STRENGTH;
        this.brewState = BrewState.BREWING;
        syncToClient();
    }

    public void initializeVisualsSilently(CultureType culture, CultureType impliedCulture,
                                          int solidStrength, BrewState brewState, long brewProgress) {
        this.culture = culture;
        this.impliedCulture = impliedCulture;
        this.solidStrength = Math.max(MIN_SOLID_STRENGTH, Math.min(MAX_SOLID_STRENGTH, solidStrength));
        this.brewState = brewState;
        this.brewProgress = brewProgress;
    }

    public boolean tryConsumeSplashCooldown(long currentGameTime) {
        if (currentGameTime - lastSplashGameTime < SPLASH_COOLDOWN_TICKS) {
            return false;
        }
        lastSplashGameTime = currentGameTime;
        return true;
    }

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
            solidStrength = Math.max(MIN_SOLID_STRENGTH, Math.min(MAX_SOLID_STRENGTH, tag.getInt(TAG_SOLID_STRENGTH)));
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