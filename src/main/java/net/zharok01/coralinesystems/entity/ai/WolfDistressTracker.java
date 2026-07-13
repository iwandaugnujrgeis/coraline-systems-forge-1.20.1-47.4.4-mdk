package net.zharok01.coralinesystems.entity.ai;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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