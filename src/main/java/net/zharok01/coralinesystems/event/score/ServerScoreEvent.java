package net.zharok01.coralinesystems.event.score;

import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.gui.score.ScoreManager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class ServerScoreEvent {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        ScoreManager.awardScore(event.getPlayer(), ScoreManager.REWARD_BLOCK_ACTIVITY);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            ScoreManager.awardScore(player, ScoreManager.REWARD_BLOCK_ACTIVITY);
        }
    }

    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (event.getEntity() instanceof Monster) {
                ScoreManager.awardScore(player, ScoreManager.REWARD_HOSTILE_KILL);
            }
        }
    }
}