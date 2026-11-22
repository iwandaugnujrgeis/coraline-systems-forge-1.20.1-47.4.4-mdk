package net.zharok01.coralinesystems.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.client.weather.FogWeatherClientState;

import java.util.function.Supplier;

public record FogWeatherPacket(boolean active) {

    public static void encode(FogWeatherPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
    }

    public static FogWeatherPacket decode(FriendlyByteBuf buffer) {
        return new FogWeatherPacket(buffer.readBoolean());
    }

    public static void handle(FogWeatherPacket packet, Supplier<NetworkEvent.Context> context) {
        FogWeatherClientState.active = packet.active;
    }
}