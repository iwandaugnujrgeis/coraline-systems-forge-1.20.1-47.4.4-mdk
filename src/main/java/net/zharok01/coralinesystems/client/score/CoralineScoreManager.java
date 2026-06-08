package net.zharok01.coralinesystems.client.score;

import net.zharok01.coralinesystems.registry.CoralinePacketHandler;
import net.zharok01.coralinesystems.network.ScoreThresholdPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class CoralineScoreManager {
    public static final int REWARD_BLOCK_ACTIVITY = 1;
    public static final int REWARD_HOSTILE_KILL = 15;

    public static void awardScore(Player player, int amount) {
        if (player != null && !player.isCreative()) {
            int oldScore = player.getScore();
            player.increaseScore(amount); // Update vanilla score field
            int newScore = player.getScore();

            // Check if we crossed a 1000-point threshold (e.g., 999 -> 1000)
            if (newScore / 1000 > oldScore / 1000) {
                // IMPORTANT: Only send the packet if we are on the Server
                if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    CoralinePacketHandler.sendToClient(new ScoreThresholdPacket(newScore), serverPlayer);
                }
            }
        }
    }
}