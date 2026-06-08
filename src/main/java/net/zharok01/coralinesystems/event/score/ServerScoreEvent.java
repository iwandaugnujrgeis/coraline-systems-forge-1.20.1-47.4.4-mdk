package net.zharok01.coralinesystems.event.score;

import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.score.CoralineScoreManager;
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
        // Award point for breaking a block
        CoralineScoreManager.awardScore(event.getPlayer(), CoralineScoreManager.REWARD_BLOCK_ACTIVITY);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        // Award point for placing a block
        if (event.getEntity() instanceof Player player) {
            CoralineScoreManager.awardScore(player, CoralineScoreManager.REWARD_BLOCK_ACTIVITY);
        }
    }

    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        // If the killer was a player and the victim was a hostile mob (Monster)
        if (event.getSource().getEntity() instanceof Player player) {
            if (event.getEntity() instanceof Monster) {
                CoralineScoreManager.awardScore(player, CoralineScoreManager.REWARD_HOSTILE_KILL);
            }
        }
    }
}