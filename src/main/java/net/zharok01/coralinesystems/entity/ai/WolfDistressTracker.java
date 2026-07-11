package net.zharok01.coralinesystems.entity.ai;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which tamed Wolves are currently in "fight or flight" distress —
 * i.e. have fled combat at low health and are roaming/howling until healed.
 *
 * We can't add a synced EntityDataAccessor directly to vanilla Wolf.java,
 * so distress state is tracked here instead, keyed by entity UUID.
 *
 * {@link WolfFleeGoal} sets a Wolf as distressed when it flees.
 * {@link WolfDistressRoamGoal} clears it once the Wolf's health recovers
 * above the flee threshold.
 *
 * Entries are cleaned up by WolfDistressRoamGoal.stop() once resolved;
 * there is no persistence across server restarts/wolf despawns by design —
 * distress is a transient combat state, not something worth saving to NBT.
 */
public final class WolfDistressTracker {

    private static final Set<UUID> DISTRESSED_WOLVES = ConcurrentHashMap.newKeySet();

    private WolfDistressTracker() {
    }

    public static void setDistressed(UUID wolfId) {
        DISTRESSED_WOLVES.add(wolfId);
    }

    public static void clearDistressed(UUID wolfId) {
        DISTRESSED_WOLVES.remove(wolfId);
    }

    public static boolean isDistressed(UUID wolfId) {
        return DISTRESSED_WOLVES.contains(wolfId);
    }
}