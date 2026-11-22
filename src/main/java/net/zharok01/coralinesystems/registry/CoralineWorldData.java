package net.zharok01.coralinesystems.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CoralineWorldData extends SavedData {

    private static final String IDENTIFIER = "coraline_mineshaft_data";
    private static final int PLAY_COUNT_THRESHOLD = 1;

    private final Map<ChunkPos, Integer> chunkPlayCounts = new HashMap<>();

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

    public boolean isBlacklisted(ChunkPos pos) {
        return chunkPlayCounts.getOrDefault(pos, 0) >= PLAY_COUNT_THRESHOLD;
    }

    public void recordPlay(ChunkPos pos) {
        int current = chunkPlayCounts.getOrDefault(pos, 0);
        chunkPlayCounts.put(pos, current + 1);
        setDirty();
    }

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
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<ChunkPos, Integer> entry : chunkPlayCounts.entrySet()) {
            CompoundTag chunk = new CompoundTag();
            chunk.putInt("X", entry.getKey().x);
            chunk.putInt("Z", entry.getKey().z);
            chunk.putInt("Count", entry.getValue());
            list.add(chunk);
        }
        tag.put("MineshaftChunks", list);
        return tag;
    }
}