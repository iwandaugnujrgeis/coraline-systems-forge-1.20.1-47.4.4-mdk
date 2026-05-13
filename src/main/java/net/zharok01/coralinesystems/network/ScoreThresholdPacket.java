package net.zharok01.coralinesystems.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.client.gui.ScoreOverlay;

import java.util.function.Supplier;

public class ScoreThresholdPacket {
    private final int newScore;

    public ScoreThresholdPacket(int newScore) {
        this.newScore = newScore;
    }

    public static void encode(ScoreThresholdPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.newScore);
    }

    public static ScoreThresholdPacket decode(FriendlyByteBuf buf) {
        return new ScoreThresholdPacket(buf.readInt());
    }

    public static void handle(ScoreThresholdPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // This code runs ONLY on the client!
            ScoreOverlay.trigger(msg.newScore);
        });
        ctx.get().setPacketHandled(true);
    }
}