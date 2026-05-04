package net.zharok01.coralinesystems.content.zipline;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.content.zipline.ZiplineStateCapability;

import java.util.function.Supplier;

public class ZiplineInputPacket {
    private final boolean isPressed;

    public ZiplineInputPacket(boolean isPressed) {
        this.isPressed = isPressed;
    }

    public static void encode(ZiplineInputPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isPressed);
    }

    public static ZiplineInputPacket decode(FriendlyByteBuf buf) {
        return new ZiplineInputPacket(buf.readBoolean());
    }

    public static void handle(ZiplineInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ZiplineStateCapability.setPressed(player, msg.isPressed);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}