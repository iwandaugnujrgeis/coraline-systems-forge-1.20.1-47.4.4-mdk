package net.zharok01.coralinesystems.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Server-side persistent store for Static portal pairs.
 *
 * Each entry maps EVERY block position inside a portal group to the canonical
 * spawn position of its linked sister portal. The mapping is bidirectional:
 * if portal A links to portal B, every block in A points to B's spawn pos
 * and every block in B points to A's spawn pos.
 *
 * Because this extends SavedData, Forge automatically writes it to
 * world/data/coraline_portal_links.dat on save and reads it back on load —
 * so links survive server restarts.
 *
 * HOW TO ACCESS (call from StaticTeleporter or anywhere on the server):
 *   StaticPortalLinkData data = StaticPortalLinkData.get(serverLevel);
 */
public class StaticPortalLinkData extends SavedData {

    private static final String DATA_NAME = "coraline_portal_links";

    /**
     * Maps every individual portal block position to the canonical spawn
     * position of the portal it is linked to.
     *
     * Canonical spawn pos = the bottom-interior-center of the sister portal
     * (the position the player is teleported TO when they use this portal).
     */
    private final Map<BlockPos, BlockPos> links = new HashMap<>();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the destination spawn position for a given portal block, or null
     * if that block has not been linked yet (i.e. the portal is brand new).
     */
    public BlockPos getLinkedDestination(BlockPos portalBlock) {
        return links.get(portalBlock);
    }

    /**
     * Returns true if ANY block in the supplied group is already linked.
     * Used to detect "has this portal been used before?" before teleporting.
     */
    public boolean isGroupLinked(Collection<BlockPos> group) {
        for (BlockPos pos : group) {
            if (links.containsKey(pos)) return true;
        }
        return false;
    }

    /**
     * Registers a bidirectional link between two portal groups.
     *
     * @param groupA       All interior block positions of the entry portal.
     * @param spawnForA    Where players arriving FROM group B should land
     *                     (canonical spawn pos inside portal A).
     * @param groupB       All interior block positions of the exit portal.
     * @param spawnForB    Where players arriving FROM group A should land
     *                     (canonical spawn pos inside portal B).
     */
    public void linkPortals(Collection<BlockPos> groupA, BlockPos spawnForA,
                            Collection<BlockPos> groupB, BlockPos spawnForB) {
        // Every block in A → spawn inside B (that's where you end up when leaving A)
        for (BlockPos pos : groupA) links.put(pos.immutable(), spawnForB.immutable());
        // Every block in B → spawn inside A (that's where you end up when leaving B)
        for (BlockPos pos : groupB) links.put(pos.immutable(), spawnForA.immutable());
        setDirty();
    }

    /**
     * Removes all link entries for the given group of blocks.
     * Call this when a portal is broken so the sister portal becomes
     * "unlinked" again (though you will also want to unlink the sister group).
     */
    public void unlinkGroup(Collection<BlockPos> group) {
        for (BlockPos pos : group) links.remove(pos);
        setDirty();
    }

    // -------------------------------------------------------------------------
    // SavedData plumbing — serialization / deserialization
    // -------------------------------------------------------------------------

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, BlockPos> entry : links.entrySet()) {
            CompoundTag pair = new CompoundTag();
            // Source block
            pair.putInt("SX", entry.getKey().getX());
            pair.putInt("SY", entry.getKey().getY());
            pair.putInt("SZ", entry.getKey().getZ());
            // Destination spawn
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
            BlockPos dest   = new BlockPos(pair.getInt("DX"), pair.getInt("DY"), pair.getInt("DZ"));
            data.links.put(source, dest);
        }
        return data;
    }

    // -------------------------------------------------------------------------
    // Access helper — always call this instead of constructing directly
    // -------------------------------------------------------------------------

    /**
     * Retrieves (or creates) the portal link store for the given server level.
     * Safe to call from any server-side code.
     */
    public static StaticPortalLinkData get(ServerLevel level) {
        // getServer().overworld() ensures all portals share ONE registry regardless
        // of which dimension the code is called from — useful if you ever add
        // cross-dimension Static portals later.
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(StaticPortalLinkData::load,
                        StaticPortalLinkData::new,
                        DATA_NAME);
    }
}
