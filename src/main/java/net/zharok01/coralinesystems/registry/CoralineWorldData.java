package net.zharok01.coralinesystems.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * Persists per-chunk mineshaft creak play counts to the world's data folder
 * (data/coraline_mineshaft_data.dat on the overworld).
 *
 * Once a chunk's count reaches PLAY_COUNT_THRESHOLD (3), it is permanently
 * blacklisted — the creaking sound will never play there again, even across
 * separate play sessions.
 */
public class CoralineWorldData extends SavedData {

    private static final String IDENTIFIER           = "coraline_mineshaft_data";
    private static final int    PLAY_COUNT_THRESHOLD = 1; //TODO: Changed it to 1 to test!

    /** Chunk coords → number of times the creak sound has played there. */
    private final Map<ChunkPos, Integer> chunkPlayCounts = new HashMap<>();

    // -------------------------------------------------------------------------
    // Access
    // -------------------------------------------------------------------------

    /**
     * Retrieves (or creates) the data instance.
     *
     * Mirrors StaticPortalLinkData.get() exactly:
     *   - accepts ServerLevel directly (no instanceof dance needed)
     *   - delegates to server.overworld() which is non-nullable, so the data
     *     file is always stored in one place regardless of calling dimension
     */
    public static CoralineWorldData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        CoralineWorldData::load,
                        CoralineWorldData::new,
                        IDENTIFIER
                );
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the creak sound has already played
     * {@value PLAY_COUNT_THRESHOLD} or more times in this chunk — meaning it
     * should never play again.
     */
    public boolean isBlacklisted(ChunkPos pos) {
        return chunkPlayCounts.getOrDefault(pos, 0) >= PLAY_COUNT_THRESHOLD;
    }

    /**
     * Records one creak play for the given chunk.
     * Marks the data dirty so Minecraft flushes it to disk on the next save.
     */
    public void recordPlay(ChunkPos pos) {
        int current = chunkPlayCounts.getOrDefault(pos, 0);
        chunkPlayCounts.put(pos, current + 1);
        setDirty();
    }

    // -------------------------------------------------------------------------
    // Serialisation
    // -------------------------------------------------------------------------

    public static CoralineWorldData load(CompoundTag tag) {
        CoralineWorldData data = new CoralineWorldData();
        ListTag list = tag.getList("MineshaftChunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ChunkPos pos   = new ChunkPos(entry.getInt("X"), entry.getInt("Z"));
            int      count = entry.getInt("Count");
            data.chunkPlayCounts.put(pos, count);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<ChunkPos, Integer> entry : chunkPlayCounts.entrySet()) {
            CompoundTag chunk = new CompoundTag();
            chunk.putInt("X",     entry.getKey().x);
            chunk.putInt("Z",     entry.getKey().z);
            chunk.putInt("Count", entry.getValue());
            list.add(chunk);
        }
        tag.put("MineshaftChunks", list);
        return tag;
    }
}