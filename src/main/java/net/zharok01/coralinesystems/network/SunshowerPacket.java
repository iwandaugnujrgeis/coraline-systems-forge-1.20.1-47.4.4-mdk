package net.zharok01.coralinesystems.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.client.weather.SunshowerClientState;

import java.util.function.Supplier;

public record SunshowerPacket(boolean active) {

    public static void encode(SunshowerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
    }

    public static SunshowerPacket decode(FriendlyByteBuf buffer) {
        return new SunshowerPacket(buffer.readBoolean());
    }

    public static void handle(SunshowerPacket packet, Supplier<NetworkEvent.Context> context) {
        SunshowerClientState.active = packet.active;
    }
}