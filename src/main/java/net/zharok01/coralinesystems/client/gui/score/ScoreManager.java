package net.zharok01.coralinesystems.client.gui.score;

import net.zharok01.coralinesystems.registry.CoralinePacketHandler;
import net.zharok01.coralinesystems.network.ScoreThresholdPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ScoreManager {
    public static final int REWARD_BLOCK_ACTIVITY = 1;
    public static final int REWARD_HOSTILE_KILL = 15;

    public static void awardScore(Player player, int amount) {
        if (player != null && !player.isCreative()) {
            int oldScore = player.getScore();
            player.increaseScore(amount);
            int newScore = player.getScore();

            if (newScore / 1000 > oldScore / 1000) {
                if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    CoralinePacketHandler.sendToClient(new ScoreThresholdPacket(newScore), serverPlayer);
                }
            }
        }
    }
}