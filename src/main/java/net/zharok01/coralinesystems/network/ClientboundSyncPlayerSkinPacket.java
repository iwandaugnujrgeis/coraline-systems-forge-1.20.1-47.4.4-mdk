package net.zharok01.coralinesystems.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.client.entity.skin.ClientSkinOverrideCache;
import net.zharok01.coralinesystems.client.entity.skin.SkinVariant;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundSyncPlayerSkinPacket {

    private final UUID playerUuid;
    private final SkinVariant variant;

    public ClientboundSyncPlayerSkinPacket(UUID playerUuid, SkinVariant variant) {
        this.playerUuid = playerUuid;
        this.variant = variant;
    }

    public static void encode(ClientboundSyncPlayerSkinPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.playerUuid);
        buf.writeUtf(packet.variant.toId());
    }

    public static ClientboundSyncPlayerSkinPacket decode(FriendlyByteBuf buf) {
        return new ClientboundSyncPlayerSkinPacket(buf.readUUID(), SkinVariant.fromId(buf.readUtf()));
    }

    public static void handle(ClientboundSyncPlayerSkinPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> handleClient(packet)
        ));
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(ClientboundSyncPlayerSkinPacket packet) {
        ClientSkinOverrideCache.put(packet.playerUuid, packet.variant);
    }
}
