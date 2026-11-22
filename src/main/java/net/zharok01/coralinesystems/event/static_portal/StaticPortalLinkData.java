package net.zharok01.coralinesystems.event.static_portal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StaticPortalLinkData extends SavedData {

    private static final String DATA_NAME = "coraline_portal_links";

    private final Map<BlockPos, BlockPos> links = new HashMap<>();

    public BlockPos getLinkedDestination(BlockPos portalBlock) {
        return links.get(portalBlock);
    }

    public boolean isGroupLinked(Collection<BlockPos> group) {
        for (BlockPos pos : group) {
            if (links.containsKey(pos)) return true;
        }
        return false;
    }

    public void linkPortals(Collection<BlockPos> groupA, BlockPos spawnForA, Collection<BlockPos> groupB, BlockPos spawnForB) {
        for (BlockPos pos : groupA) links.put(pos.immutable(), spawnForB.immutable());
        for (BlockPos pos : groupB) links.put(pos.immutable(), spawnForA.immutable());
        setDirty();
    }

    public void unlinkGroup(Collection<BlockPos> group) {
        boolean changed = false;
        for (BlockPos pos : group) {
            if (links.remove(pos) != null) changed = true;
        }
        if (changed) setDirty();
    }

    public void unlinkAllPointingTo(BlockPos destination) {
        if (links.values().removeIf(pos -> pos.equals(destination))) {
            setDirty();
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, BlockPos> entry : links.entrySet()) {
            CompoundTag pair = new CompoundTag();
            pair.putInt("SX", entry.getKey().getX());
            pair.putInt("SY", entry.getKey().getY());
            pair.putInt("SZ", entry.getKey().getZ());
            pair.putInt("DX", entry.getValue().getX());
            pair.putInt("DY", entry.getValue().getY());
            pair.putInt("DZ", entry.getValue().getZ());
            list.add(pair);
        }
        tag.put("Links", list);
        return tag;
    }

    public static StaticPortalLinkData load(CompoundTag tag) {
        StaticPortalLinkData data = new StaticPortalLinkData();
        ListTag list = tag.getList("Links", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag pair = list.getCompound(i);
            BlockPos source = new BlockPos(pair.getInt("SX"), pair.getInt("SY"), pair.getInt("SZ"));
            BlockPos dest = new BlockPos(pair.getInt("DX"), pair.getInt("DY"), pair.getInt("DZ"));
            data.links.put(source, dest);
        }
        return data;
    }

    public static StaticPortalLinkData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(StaticPortalLinkData::load, StaticPortalLinkData::new, DATA_NAME);
    }
}