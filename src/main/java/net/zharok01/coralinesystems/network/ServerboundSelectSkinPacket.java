package net.zharok01.coralinesystems.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.client.entity.skin.SkinVariant;
import net.zharok01.coralinesystems.client.entity.skin.PlayerSkinAttachments;

import java.util.function.Supplier;

public class ServerboundSelectSkinPacket {

    private final SkinVariant variant;

    public ServerboundSelectSkinPacket(SkinVariant variant) {
        this.variant = variant;
    }

    public static void encode(ServerboundSelectSkinPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.variant.toId());
    }

    public static ServerboundSelectSkinPacket decode(FriendlyByteBuf buf) {
        return new ServerboundSelectSkinPacket(SkinVariant.fromId(buf.readUtf()));
    }

    public static void handle(ServerboundSelectSkinPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) return;

            PlayerSkinAttachments.setVariant(sender, packet.variant);
        });
        ctx.setPacketHandled(true);
    }
}