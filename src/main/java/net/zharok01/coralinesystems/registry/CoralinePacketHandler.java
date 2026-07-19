package net.zharok01.coralinesystems.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

        // S2C: tinted splash particles when an entity enters the brewing cauldron's
        // fluid surface, and for the "brew finished" completion burst.
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
     * Sends a CauldronSplashPacket to all players tracking the chunk that
     * contains the given cauldron, including the triggering player themselves
     * (TRACKING_CHUNK may exclude the sender on some Forge builds).
     */
    public static void sendCauldronSplash(BlockPos pos, int color, ServerPlayer triggeringPlayer) {
        CauldronSplashPacket packet = new CauldronSplashPacket(pos, color);
        INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(
                        () -> triggeringPlayer.level().getChunkAt(pos)),
                packet);
        // Also explicitly send to the triggering player.
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> triggeringPlayer), packet);
    }

    /**
     * Broadcasts a CauldronSplashPacket to all players tracking the chunk at
     * {@code pos}, without requiring a specific triggering player.  Used by
     * {@code fireFinishedCue} (called from {@code randomTick}, which has no
     * player reference) to deliver the brew-completion particle burst to every
     * nearby client.
     */
    public static void broadcastCauldronSplash(BlockPos pos, int color, ServerLevel serverLevel) {
        CauldronSplashPacket packet = new CauldronSplashPacket(pos, color);
        INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(
                        () -> serverLevel.getChunkAt(pos)),
                packet);
    }
}