package net.zharok01.coralinesystems.event.zipline;

import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZiplineStateCapability {

    private static final Set<UUID> PRESSED = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<UUID> NEEDS_RELEASE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void setPressed(Player player, boolean pressed) {
        if (pressed) {
            PRESSED.add(player.getUUID());
        } else {
            PRESSED.remove(player.getUUID());
            NEEDS_RELEASE.remove(player.getUUID());
        }
    }

    public static boolean isPressed(Player player) {
        return PRESSED.contains(player.getUUID());
    }

    public static void coraline$requireRelease(Player player) {
        NEEDS_RELEASE.add(player.getUUID());
    }

    public static boolean coraline$needsRelease(Player player) {
        return NEEDS_RELEASE.contains(player.getUUID());
    }

    public static void clear(Player player) {
        PRESSED.remove(player.getUUID());
        NEEDS_RELEASE.remove(player.getUUID());
    }
}