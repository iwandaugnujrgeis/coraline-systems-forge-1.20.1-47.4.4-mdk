package net.zharok01.coralinesystems.util.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReactorSavedData extends SavedData {

    private static final String IDENTIFIER = "coraline_reactor_data";
    private static final String KEY_SESSIONS = "Sessions";
    private static final String KEY_SOURCE_X = "SourceX";
    private static final String KEY_SOURCE_Y = "SourceY";
    private static final String KEY_SOURCE_Z = "SourceZ";
    private static final String KEY_TICKS_REMAINING = "TicksRemaining";

    private List<SavedSession> sessions = new ArrayList<>();

    public static ReactorSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(ReactorSavedData::load, ReactorSavedData::new, IDENTIFIER);
    }

    public List<SavedSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<SavedSession> newSessions) {
        this.sessions = newSessions;
        setDirty();
    }

    public void setSessionsQuiet(List<SavedSession> newSessions) {
        this.sessions = newSessions;
    }

    public static ReactorSavedData load(CompoundTag tag) {
        ReactorSavedData data = new ReactorSavedData();
        ListTag list = tag.getList(KEY_SESSIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos source = new BlockPos(
                    entry.getInt(KEY_SOURCE_X),
                    entry.getInt(KEY_SOURCE_Y),
                    entry.getInt(KEY_SOURCE_Z));
            data.sessions.add(new SavedSession(source, entry.getInt(KEY_TICKS_REMAINING)));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (SavedSession session : sessions) {
            CompoundTag entry = new CompoundTag();
            entry.putInt(KEY_SOURCE_X, session.source().getX());
            entry.putInt(KEY_SOURCE_Y, session.source().getY());
            entry.putInt(KEY_SOURCE_Z, session.source().getZ());
            entry.putInt(KEY_TICKS_REMAINING, session.ticksRemaining());
            list.add(entry);
        }
        tag.put(KEY_SESSIONS, list);
        return tag;
    }

    public record SavedSession(BlockPos source, int ticksRemaining) {}
}