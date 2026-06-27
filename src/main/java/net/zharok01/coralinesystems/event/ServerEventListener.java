package net.zharok01.coralinesystems.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.time.TimeAccelerationManager;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventListener {

    /**
     * One manager per loaded server. Nulled on world unload so
     * a fresh one is created on the next load.
     */
    private static TimeAccelerationManager manager = null;

    /** @return the active manager, or null if no world is loaded */
    public static TimeAccelerationManager getManager() {
        return manager;
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (level instanceof ServerLevel serverLevel
                && serverLevel.dimension().equals(Level.OVERWORLD)) {
            manager = new TimeAccelerationManager();
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();
        if (level instanceof ServerLevel serverLevel
                && serverLevel.dimension().equals(Level.OVERWORLD)) {
            manager = null;
        }
    }

    /**
     * Drives the time acceleration every server tick.
     *
     * LOWEST priority so we run after all other mods have had their say,
     * matching the pattern used by Better Days.
     */
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