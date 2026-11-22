package net.zharok01.coralinesystems.client.entity.skin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.zharok01.coralinesystems.network.ClientboundSyncPlayerSkinPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerSkinAttachments {

    private static final Map<UUID, SkinVariant> SERVER_CACHE = new ConcurrentHashMap<>();

    private PlayerSkinAttachments() {}

    public static SkinVariant getVariant(ServerPlayer player) {
        return SERVER_CACHE.getOrDefault(player.getUUID(), SkinVariant.DEFAULT);
    }

    public static void setVariant(ServerPlayer player, SkinVariant variant) {
        SERVER_CACHE.put(player.getUUID(), variant);
        broadcastToTrackers(player, variant);
    }

    public static void remove(ServerPlayer player) {
        SERVER_CACHE.remove(player.getUUID());
    }

    public static void broadcastToTrackers(ServerPlayer player, SkinVariant variant) {
        ClientboundSyncPlayerSkinPacket packet =
                new ClientboundSyncPlayerSkinPacket(player.getUUID(), variant);

        CoralinePacketHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                packet
        );
    }
}