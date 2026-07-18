package net.zharok01.coralinesystems.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.zharok01.coralinesystems.network.CauldronSplashPacket;
import net.zharok01.coralinesystems.network.OpenInventoryPacket;
import net.zharok01.coralinesystems.network.ScoreThresholdPacket;
import net.zharok01.coralinesystems.zipline.ZiplineInputPacket;

public class CoralinePacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("coraline_systems", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.messageBuilder(ZiplineInputPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ZiplineInputPacket::decode)
                .encoder(ZiplineInputPacket::encode)
                .consumerMainThread(ZiplineInputPacket::handle)
                .add();

        INSTANCE.registerMessage(
                packetId++,
                ScoreThresholdPacket.class,
                ScoreThresholdPacket::encode,
                ScoreThresholdPacket::decode,
                ScoreThresholdPacket::handle
        );

        INSTANCE.messageBuilder(OpenInventoryPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenInventoryPacket::decode)
                .encoder(OpenInventoryPacket::encode)
                .consumerMainThread(OpenInventoryPacket::handle)
                .add();

        // S2C: tells nearby clients to spawn tinted splash particles when an
        // entity enters the fluid surface of a Brewing Cauldron.
        INSTANCE.messageBuilder(CauldronSplashPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CauldronSplashPacket::decode)
                .encoder(CauldronSplashPacket::encode)
                .consumerMainThread(CauldronSplashPacket::handle)
                .add();
    }

    public static void sendToServer(ZiplineInputPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToServer(OpenInventoryPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToClient(ScoreThresholdPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /**
     * Sends a CauldronSplashPacket to all clients tracking the chunk that
     * contains the given cauldron position.  Using NEAR instead of
     * TRACKING_CHUNK so the effect is visible to the entity itself if it is
     * a player, without needing to also send to all chunk-trackers.
     */
    public static void sendCauldronSplash(BlockPos pos, int color, ServerPlayer triggeringPlayer) {
        CauldronSplashPacket packet = new CauldronSplashPacket(pos, color);
        // Send to every player within normal chunk-tracking distance of pos.
        INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(
                        () -> triggeringPlayer.level().getChunkAt(pos)),
                packet);
        // Also send to the triggering player themselves (they track their own
        // chunk, but TRACKING_CHUNK excludes the sender on some Forge builds).
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> triggeringPlayer), packet);
    }
}
