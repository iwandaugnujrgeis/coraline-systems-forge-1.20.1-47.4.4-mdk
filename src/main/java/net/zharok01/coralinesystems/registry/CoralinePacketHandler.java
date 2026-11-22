package net.zharok01.coralinesystems.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.zharok01.coralinesystems.network.*;
import net.zharok01.coralinesystems.event.zipline.ZiplineInputPacket;

@SuppressWarnings("removal")
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

        INSTANCE.messageBuilder(ClientboundZiplineSoundPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientboundZiplineSoundPacket::decode)
                .encoder(ClientboundZiplineSoundPacket::encode)
                .consumerMainThread(ClientboundZiplineSoundPacket::handle)
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

        INSTANCE.messageBuilder(CauldronSplashPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CauldronSplashPacket::decode)
                .encoder(CauldronSplashPacket::encode)
                .consumerMainThread(CauldronSplashPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerboundSelectSkinPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerboundSelectSkinPacket::decode)
                .encoder(ServerboundSelectSkinPacket::encode)
                .consumerMainThread(ServerboundSelectSkinPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundSyncPlayerSkinPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientboundSyncPlayerSkinPacket::decode)
                .encoder(ClientboundSyncPlayerSkinPacket::encode)
                .consumerMainThread(ClientboundSyncPlayerSkinPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerboundNameTagPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerboundNameTagPacket::decode)
                .encoder(ServerboundNameTagPacket::encode)
                .consumerMainThread(ServerboundNameTagPacket::handle)
                .add();

        INSTANCE.messageBuilder(OpenRecipeBookPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenRecipeBookPacket::decode)
                .encoder(OpenRecipeBookPacket::encode)
                .consumerMainThread(OpenRecipeBookPacket::handle)
                .add();

        INSTANCE.messageBuilder(SunshowerPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SunshowerPacket::decode)
                .encoder(SunshowerPacket::encode)
                .consumerMainThread(SunshowerPacket::handle)
                .add();

        INSTANCE.messageBuilder(FogWeatherPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FogWeatherPacket::decode)
                .encoder(FogWeatherPacket::encode)
                .consumerMainThread(FogWeatherPacket::handle)
                .add();

        INSTANCE.messageBuilder(QuiverInventoryPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(QuiverInventoryPacket::decode)
                .encoder(QuiverInventoryPacket::encode)
                .consumerMainThread(QuiverInventoryPacket::handle)
                .add();
    }

    public static void sendToServer(ZiplineInputPacket packet) {
        INSTANCE.sendToServer(packet);
    }
    public static void sendToServer(OpenInventoryPacket packet) {
        INSTANCE.sendToServer(packet);
    }
    public static void sendToServer(ServerboundSelectSkinPacket packet) {
        INSTANCE.sendToServer(packet);
    }
    public static void sendToServer(ServerboundNameTagPacket packet) {
        INSTANCE.sendToServer(packet);
    }
    public static void sendToServer(OpenRecipeBookPacket packet) {
        INSTANCE.sendToServer(packet);
    }
    public static void sendToServer(QuiverInventoryPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToClient(ScoreThresholdPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToClient(ClientboundZiplineSoundPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToTracking(Entity entity, Object packet) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }

    public static void sendCauldronSplash(BlockPos pos, int color, ServerPlayer triggeringPlayer) {
        CauldronSplashPacket packet = new CauldronSplashPacket(pos, color);
        INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(
                        () -> triggeringPlayer.level().getChunkAt(pos)),
                packet);
    }

    public static void broadcastCauldronSplash(BlockPos pos, int color, ServerLevel serverLevel) {
        CauldronSplashPacket packet = new CauldronSplashPacket(pos, color);
        INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(
                        () -> serverLevel.getChunkAt(pos)),
                packet);
    }

    public static void broadcastCauldronBurst(BlockPos pos, int color, ServerLevel serverLevel) {
        CauldronSplashPacket packet = new CauldronSplashPacket(pos, color, true);
        INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(
                        () -> serverLevel.getChunkAt(pos)),
                packet);
    }

    public static void sendSunshowerToAll(SunshowerPacket packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendSunshowerToPlayer(SunshowerPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendFogWeatherToAll(FogWeatherPacket packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendFogWeatherToPlayer(FogWeatherPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}