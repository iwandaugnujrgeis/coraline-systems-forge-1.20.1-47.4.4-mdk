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
import net.zharok01.coralinesystems.event.ReactorSessionManager;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReactorEvent {

    private static ReactorSessionManager manager = null;

    public static ReactorSessionManager getManager() {
        return manager;
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.dimension().equals(Level.OVERWORLD)) {
            manager = new ReactorSessionManager(serverLevel);
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