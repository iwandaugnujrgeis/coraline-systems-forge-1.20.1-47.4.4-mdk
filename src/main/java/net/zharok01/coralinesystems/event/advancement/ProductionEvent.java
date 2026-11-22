package net.zharok01.coralinesystems.event.advancement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ProductionEvent {

    @SubscribeEvent
    public static void onPlayerSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.PLAYER_SMELTED.trigger(serverPlayer, event.getSmelting());
        }
    }

    @SubscribeEvent
    public static void onPlayerCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.PLAYER_CRAFTED.trigger(serverPlayer, event.getCrafting());
        }
    }
}