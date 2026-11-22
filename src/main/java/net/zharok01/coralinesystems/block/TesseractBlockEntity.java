package net.zharok01.coralinesystems.block;

import com.legacy.rediscovered.entity.dragon.RedDragonBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TesseractBlockEntity extends BlockEntity {

    public static final int MAX_ALTAR_DISTANCE = 16;
    private static final double DRAGON_SEARCH_RADIUS = 70.0;

    private static final double RENDER_EXTENT = 96.0;

    private static final int AMBIENT_SOUND_INTERVAL_TICKS = 80;
    private static final float AMBIENT_HUM_PITCH = 0.5f;
    private static final float AMBIENT_SHIMMER_VOLUME = 1.0f;
    private static final float AMBIENT_SHIMMER_PITCH = 0.7f;

    @Nullable private AABB renderBoundingBox = null;

    private static final double DRAGON_SEARCH_EXTENT = DRAGON_SEARCH_RADIUS;

    private static final int DRAGON_CHUNK_RADIUS = 9;
    private static final int LOADER_CHUNK_RADIUS = 1;

    public boolean isCharged = false;
    public int ticks = 0;

    @Nullable private BlockPos linkedAltarPos = null;
    @Nullable private UUID tetheredDragonUUID = null;

    private static final String KEY_CHARGED = "charged";
    private static final String KEY_ALTAR_POS = "altar_pos";
    private static final String KEY_DRAGON_UUID = "dragon_uuid";

    public TesseractBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.TESSERACT.get(), pos, state);
    }

    public void initFromStructure(UUID dragonUUID, BlockPos altarPos) {
        this.tetheredDragonUUID = dragonUUID;
        this.linkedAltarPos = altarPos.immutable();
        this.isCharged = true;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TesseractBlockEntity be) {
        if (level.isClientSide()) {
            be.ticks++;
            return;
        }

        ServerLevel sl = (ServerLevel) level;
        be.ticks++;

        ChunkPos homeChunk = new ChunkPos(pos);

        if (be.tetheredDragonUUID != null) {
            ChunkPos anchorChunk = be.linkedAltarPos != null
                    ? new ChunkPos(be.linkedAltarPos)
                    : homeChunk;
            sl.getChunkSource().addRegionTicket(
                    RedDragonBossEntity.RED_DRAGON_TICKET, anchorChunk, DRAGON_CHUNK_RADIUS, anchorChunk);

            RedDragonBossEntity dragon = (RedDragonBossEntity) sl.getEntity(be.tetheredDragonUUID);
            if (dragon != null && !dragon.isRemoved()) {
                dragon.setPylonTime(20);
            } else {
                be.tetheredDragonUUID = null;
                be.setChanged();
                sl.getChunkSource().addRegionTicket(
                        RedDragonBossEntity.RED_DRAGON_TICKET, homeChunk, LOADER_CHUNK_RADIUS, homeChunk);
            }
        } else {
            sl.getChunkSource().addRegionTicket(
                    RedDragonBossEntity.RED_DRAGON_TICKET, homeChunk, LOADER_CHUNK_RADIUS, homeChunk);
        }

        if (be.ticks % 20 != 0) return;

        if (!be.isAltarPresent(sl)) {
            if (be.isCharged) {
                be.discharge(sl, pos, state);
            }
            return;
        }

        if (!be.isCharged) {
            be.isCharged = true;
            be.setChanged();
            sl.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        if (be.ticks % AMBIENT_SOUND_INTERVAL_TICKS == 0) {
            sl.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0f, AMBIENT_HUM_PITCH);
            sl.playSound(null, pos, SoundEvents.CONDUIT_AMBIENT, SoundSource.BLOCKS,
                    AMBIENT_SHIMMER_VOLUME, AMBIENT_SHIMMER_PITCH);
        }

        if (be.tetheredDragonUUID == null) {
            sl.getEntitiesOfClass(RedDragonBossEntity.class,
                    new AABB(pos).inflate(DRAGON_SEARCH_EXTENT),
                    e -> !e.isRemoved()
            ).stream().findFirst().ifPresent(dragon -> {
                be.tetheredDragonUUID = dragon.getUUID();
                be.setChanged();
                sl.sendBlockUpdated(pos, state, state, 3);
            });
        }
    }

    private boolean isAltarPresent(ServerLevel level) {
        if (linkedAltarPos != null) {
            if (level.getBlockState(linkedAltarPos).is(CoralineBlocks.ALTAR.get())) return true;
            linkedAltarPos = null;
        }

        BlockPos checkPos = worldPosition.below();
        for (int i = 0; i < MAX_ALTAR_DISTANCE; i++, checkPos = checkPos.below()) {
            BlockState below = level.getBlockState(checkPos);
            if (below.isAir()) continue;
            if (below.is(CoralineBlocks.ALTAR.get())) {
                linkedAltarPos = checkPos.immutable();
                return true;
            }
            return false;
        }
        return false;
    }

    private void discharge(ServerLevel level, BlockPos pos, BlockState state) {
        unleashDragon();
        linkedAltarPos = null;
        isCharged = false;
        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    public void unleashDragon() {
        if (tetheredDragonUUID == null) return;
        Level level = getLevel();
        if (!(level instanceof ServerLevel sl)) return;

        RedDragonBossEntity dragon = (RedDragonBossEntity) sl.getEntity(tetheredDragonUUID);
        if (dragon != null && !dragon.isRemoved()) {
            dragon.setPylonTime(0);
        }
        tetheredDragonUUID = null;
        isCharged = false;
        setChanged();
    }

    public void markStructureDirty() {
        linkedAltarPos = null;
    }

    @Nullable
    public BlockPos getLinkedAltarPos() {
        return linkedAltarPos;
    }

    @Nullable
    public UUID getTetheredDragonUUID() {
        return tetheredDragonUUID;
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide() && isCharged) {
            unleashDragon();
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(KEY_CHARGED, isCharged);
        if (linkedAltarPos != null) {
            tag.putLong(KEY_ALTAR_POS, linkedAltarPos.asLong());
        }
        if (tetheredDragonUUID != null) {
            tag.putUUID(KEY_DRAGON_UUID, tetheredDragonUUID);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        isCharged = tag.getBoolean(KEY_CHARGED);
        linkedAltarPos = tag.contains(KEY_ALTAR_POS) ? BlockPos.of(tag.getLong(KEY_ALTAR_POS)) : null;
        tetheredDragonUUID = tag.hasUUID(KEY_DRAGON_UUID) ? tag.getUUID(KEY_DRAGON_UUID) : null;
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
    public @NotNull AABB getRenderBoundingBox() {
        if (renderBoundingBox == null) {
            renderBoundingBox = new AABB(worldPosition).inflate(RENDER_EXTENT);
        }
        return renderBoundingBox;
    }
}