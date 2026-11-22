package net.zharok01.coralinesystems.event.advancement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class MinecartDistanceEvents {

    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int CM_PER_BLOCK = 100;

    @SubscribeEvent
    public static void coraline$onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;
        if (!(player.getVehicle() instanceof AbstractMinecart)) return;

        int blocksTravelled = player.getStats().getValue(Stats.CUSTOM, Stats.MINECART_ONE_CM) / CM_PER_BLOCK;
        CoralineTriggers.MINECART_DISTANCE.trigger(player, blocksTravelled);
    }
}