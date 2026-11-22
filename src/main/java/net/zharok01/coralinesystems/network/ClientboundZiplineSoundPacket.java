package net.zharok01.coralinesystems.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.event.zipline.ZiplineClientHelper;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundZiplineSoundPacket {

    private final UUID coraline$playerUuid;
    private final boolean coraline$ziplining;

    public ClientboundZiplineSoundPacket(UUID playerUuid, boolean ziplining) {
        this.coraline$playerUuid = playerUuid;
        this.coraline$ziplining = ziplining;
    }

    public static void encode(ClientboundZiplineSoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.coraline$playerUuid);
        buffer.writeBoolean(packet.coraline$ziplining);
    }

    public static ClientboundZiplineSoundPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundZiplineSoundPacket(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(ClientboundZiplineSoundPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> handleClient(packet)
        ));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(ClientboundZiplineSoundPacket packet) {
        ZiplineClientHelper.coraline$setObserverZipline(packet.coraline$playerUuid, packet.coraline$ziplining);
    }
}
