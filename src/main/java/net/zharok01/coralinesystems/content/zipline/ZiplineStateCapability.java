package net.zharok01.coralinesystems.content.zipline;

import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which players currently have the zipline key (right-click) held down,
 * as reported by the client via ZiplineInputPacket.
 *
 * Lives entirely on the server — no Forge capability registration needed.
 * The client sends a packet whenever the key state changes; this class stores
 * the last known state so ZiplineHandler can query it synchronously each tick.
 *
 * ConcurrentHashMap-backed set is used in case packet handling and the game
 * tick ever land on different threads during server startup edge cases.
 */
public class ZiplineStateCapability {

    private static final Set<UUID> PRESSED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void setPressed(Player player, boolean pressed) {
        if (pressed) {
            PRESSED.add(player.getUUID());
        } else {
            PRESSED.remove(player.getUUID());
        }
    }

    public static boolean isPressed(Player player) {
        return PRESSED.contains(player.getUUID());
    }

    /**
     * Call this when a player disconnects so their state doesn't linger.
     * Hook into PlayerEvent.PlayerLoggedOutEvent in your event class.
     */
    public static void clear(Player player) {
        PRESSED.remove(player.getUUID());
    }
}