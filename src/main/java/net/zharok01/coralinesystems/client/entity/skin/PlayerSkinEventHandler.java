package net.zharok01.coralinesystems.client.entity.skin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.network.ClientboundSyncPlayerSkinPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerSkinEventHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        SkinVariant stored = PlayerSkinAttachments.getVariant(player);
        PlayerSkinAttachments.broadcastToTrackers(player, stored);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerSkinAttachments.remove(player);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof ServerPlayer trackedPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer observer)) return;

        SkinVariant variant = PlayerSkinAttachments.getVariant(trackedPlayer);
        ClientboundSyncPlayerSkinPacket packet =
                new ClientboundSyncPlayerSkinPacket(trackedPlayer.getUUID(), variant);

        CoralinePacketHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> observer),
                packet
        );
    }
}