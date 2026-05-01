package net.zharok01.coralinesystems.client.score;

import net.minecraft.world.entity.player.Player;

/**
 * Manages the Gamma Files scoring system.
 * Reference: Kaupenjoe Forge Tutorial Series
 */
public class CoralineScoreManager {

    // Easily extensible reward values
    public static final int REWARD_BLOCK_ACTIVITY = 1;
    public static final int REWARD_HOSTILE_KILL = 15;

    /**
     * Updates the player's internal score value directly.
     * This avoids the XP system entirely, as required for Gamma Files.
     */
    public static void awardScore(Player player, int amount) {
        if (player != null && !player.isCreative()) {
            // increaseScore directly modifies the synched data used by the UI
            player.increaseScore(amount);
        }
    }
}