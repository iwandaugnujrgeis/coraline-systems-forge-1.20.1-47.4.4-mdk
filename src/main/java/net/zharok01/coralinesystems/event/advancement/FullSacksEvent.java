package net.zharok01.coralinesystems.event.advancement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FullSacksEvent {

    private static final int TICK_CHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.FULL_SACKS.trigger(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.FULL_SACKS.trigger(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        if (serverPlayer.tickCount % TICK_CHECK_INTERVAL == 0) {
            CoralineTriggers.FULL_SACKS.trigger(serverPlayer);
        }
    }
}