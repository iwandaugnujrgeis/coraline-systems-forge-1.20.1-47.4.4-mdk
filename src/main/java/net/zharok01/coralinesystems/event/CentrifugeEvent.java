package net.zharok01.coralinesystems.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.time.TimeAccelerationManager;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CentrifugeEvent {

    private static TimeAccelerationManager manager = null;

    public static TimeAccelerationManager getManager() {
        return manager;
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.dimension().equals(Level.OVERWORLD)) {
            // Pass the level so the manager can fetch SavedData and restore
            // any session that was running before the world closed.
            manager = new TimeAccelerationManager(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.dimension().equals(Level.OVERWORLD)) {
            manager = null;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.side != LogicalSide.SERVER) return;
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;
        if (manager == null) return;

        manager.tick(serverLevel);
    }
}