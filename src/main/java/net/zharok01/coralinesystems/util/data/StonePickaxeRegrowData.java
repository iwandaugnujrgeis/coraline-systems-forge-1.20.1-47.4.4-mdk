package net.zharok01.coralinesystems.util.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StonePickaxeRegrowData extends SavedData {

    private static final String DATA_NAME = "coraline_stone_pickaxe_regrow";
    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_POS = "Pos";
    private static final String TAG_STATE = "State";
    private static final String TAG_EFFECT_TICK = "EffectTick";
    private static final String TAG_RESTORE_TICK = "RestoreTick";
    private static final String TAG_EFFECT_PLAYED = "EffectPlayed";

    public static class Entry {
        public final BlockPos pos;
        public final BlockState state;
        public final long effectTick;
        public final long restoreTick;
        public boolean effectPlayed;

        private Entry(BlockPos pos, BlockState state, long effectTick, long restoreTick, boolean effectPlayed) {
            this.pos = pos;
            this.state = state;
            this.effectTick = effectTick;
            this.restoreTick = restoreTick;
            this.effectPlayed = effectPlayed;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public static StonePickaxeRegrowData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(StonePickaxeRegrowData::load, StonePickaxeRegrowData::new, DATA_NAME);
    }

    public List<Entry> entries() {
        return entries;
    }

    public void schedule(BlockPos pos, BlockState state, long effectTick, long restoreTick) {
        entries.add(new Entry(pos, state, effectTick, restoreTick, false));
        setDirty();
    }

    private static StonePickaxeRegrowData load(CompoundTag tag) {
        StonePickaxeRegrowData data = new StonePickaxeRegrowData();
        ListTag list = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            data.entries.add(new Entry(
                    NbtUtils.readBlockPos(entryTag.getCompound(TAG_POS)),
                    NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entryTag.getCompound(TAG_STATE)),
                    entryTag.getLong(TAG_EFFECT_TICK),
                    entryTag.getLong(TAG_RESTORE_TICK),
                    entryTag.getBoolean(TAG_EFFECT_PLAYED)
            ));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put(TAG_POS, NbtUtils.writeBlockPos(entry.pos));
            entryTag.put(TAG_STATE, NbtUtils.writeBlockState(entry.state));
            entryTag.putLong(TAG_EFFECT_TICK, entry.effectTick);
            entryTag.putLong(TAG_RESTORE_TICK, entry.restoreTick);
            entryTag.putBoolean(TAG_EFFECT_PLAYED, entry.effectPlayed);
            list.add(entryTag);
        }
        tag.put(TAG_ENTRIES, list);
        return tag;
    }
}