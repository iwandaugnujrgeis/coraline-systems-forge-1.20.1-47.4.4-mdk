package net.zharok01.coralinesystems.event.advancements;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

/**
 * Listens on the Forge event bus for inventory changes, then fires FullSacksTrigger.
 *
 * PlayerEvent.ItemPickupEvent covers:
 *   - Picking items up from the ground
 *
 * We also hook PlayerContainerEvent.Close to catch:
 *   - Items moved in from chests, crafting tables, etc.
 *
 * A PlayerTickEvent with a throttle catches any remaining edge cases
 * (commands, other mods giving items, etc.) without being too expensive.
 */
@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FullSacksEvent {

    /** Check every 20 ticks (once per second) as a catch-all. */
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
        // Only check server-side, only on the END phase to avoid double-firing
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        // Throttle: only check once per second per player
        if (serverPlayer.tickCount % TICK_CHECK_INTERVAL == 0) {
            CoralineTriggers.FULL_SACKS.trigger(serverPlayer);
        }
    }
}