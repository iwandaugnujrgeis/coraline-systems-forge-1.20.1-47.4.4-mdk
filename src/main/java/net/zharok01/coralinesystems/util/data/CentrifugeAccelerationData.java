package net.zharok01.coralinesystems.util.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CentrifugeAccelerationData extends SavedData {

    private static final String IDENTIFIER = "coraline_acceleration_data";

    private static final String KEY_SESSIONS = "Sessions";
    private static final String KEY_SOURCE_X = "SourceX";
    private static final String KEY_SOURCE_Y = "SourceY";
    private static final String KEY_SOURCE_Z = "SourceZ";
    private static final String KEY_SOURCE_DIMENSION = "SourceDimension";
    private static final String KEY_TICKS_REMAINING = "TicksRemaining";

    private List<SavedSession> sessions = new ArrayList<>();

    public static CentrifugeAccelerationData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        CentrifugeAccelerationData::load,
                        CentrifugeAccelerationData::new,
                        IDENTIFIER
                );
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

    public static CentrifugeAccelerationData load(CompoundTag tag) {
        CentrifugeAccelerationData data = new CentrifugeAccelerationData();

        ListTag list = tag.getList(KEY_SESSIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag sessionTag = list.getCompound(i);

            BlockPos pos = new BlockPos(
                    sessionTag.getInt(KEY_SOURCE_X),
                    sessionTag.getInt(KEY_SOURCE_Y),
                    sessionTag.getInt(KEY_SOURCE_Z)
            );
            ResourceKey<Level> dimension = sessionTag.contains(KEY_SOURCE_DIMENSION)
                    ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(sessionTag.getString(KEY_SOURCE_DIMENSION)))
                    : Level.OVERWORLD;
            int ticksRemaining = sessionTag.getInt(KEY_TICKS_REMAINING);

            data.sessions.add(new SavedSession(GlobalPos.of(dimension, pos), ticksRemaining));
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();

        for (SavedSession session : sessions) {
            CompoundTag sessionTag = new CompoundTag();
            sessionTag.putInt(KEY_SOURCE_X, session.source().pos().getX());
            sessionTag.putInt(KEY_SOURCE_Y, session.source().pos().getY());
            sessionTag.putInt(KEY_SOURCE_Z, session.source().pos().getZ());
            sessionTag.putString(KEY_SOURCE_DIMENSION, session.source().dimension().location().toString());
            sessionTag.putInt(KEY_TICKS_REMAINING, session.ticksRemaining());
            list.add(sessionTag);
        }

        tag.put(KEY_SESSIONS, list);
        return tag;
    }

    public record SavedSession(GlobalPos source, int ticksRemaining) {}
}