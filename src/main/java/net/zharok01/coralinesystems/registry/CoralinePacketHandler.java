package net.zharok01.coralinesystems.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
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

        // Registers using the same messageBuilder style as ZiplineInputPacket
        // for consistency. PLAY_TO_SERVER because this is always client -> server.
        INSTANCE.messageBuilder(OpenInventoryPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenInventoryPacket::decode)
                .encoder(OpenInventoryPacket::encode)
                .consumerMainThread(OpenInventoryPacket::handle)
                .add();
    }

    public static void sendToServer(ZiplineInputPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    // New typed overload for OpenInventoryPacket
    public static void sendToServer(OpenInventoryPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToClient(ScoreThresholdPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}