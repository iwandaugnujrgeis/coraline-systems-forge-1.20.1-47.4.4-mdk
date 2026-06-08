package net.zharok01.coralinesystems.event.advancements;

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
        // ItemSmeltedEvent is always fired from FurnaceResultSlot.checkTakeAchievements,
        // which requires a player to physically take the item — so getEntity() is
        // guaranteed non-null. However, it can be a client-side Player on an
        // integrated server, so we guard for ServerPlayer before touching criteria.
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.PLAYER_SMELTED.trigger(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerCrafted(PlayerEvent.ItemCraftedEvent event) {
        // ItemCraftedEvent is fired inside ResultSlot.checkTakeAchievements,
        // guarded by removeCount > 0, so this only fires on genuine crafting.
        // The player reference here is a vanilla Player — we guard for
        // ServerPlayer before touching criteria since this can technically
        // reach us from the client side on an integrated server.
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.PLAYER_CRAFTED.trigger(serverPlayer, event.getCrafting());
        }
    }
}
