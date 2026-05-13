package net.zharok01.coralinesystems.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.zharok01.coralinesystems.content.zipline.ZiplineInputPacket;

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

        INSTANCE.registerMessage(packetId++, ScoreThresholdPacket.class, ScoreThresholdPacket::encode, ScoreThresholdPacket::decode, ScoreThresholdPacket::handle);
    }

    /**
     * FIX: typed to ZiplineInputPacket rather than Object.
     * The original Object overload compiled fine but lost type information at the
     * call site — the channel's sendToServer is generic and passing raw Object
     * causes a ClassCastException at runtime for any packet type not matching
     * the channel's registered handler.
     *
     * Add additional typed overloads here as you register more packet types.
     */
    public static void sendToServer(ZiplineInputPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    /**
     * Sends a packet from the Server to a specific Client.
     * Uses PacketDistributor to target the exact player who earned the score.
     */
    public static void sendToClient(ScoreThresholdPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}