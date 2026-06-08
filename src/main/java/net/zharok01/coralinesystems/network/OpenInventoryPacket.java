package net.zharok01.coralinesystems.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

import java.util.function.Supplier;

/**
 * Sent client -> server when the player opens their inventory screen.
 * Carries no payload — the sender's identity is sufficient.
 *
 * Defined as a record to make clear this is a data-carrying packet instance,
 * not a utility class. The record has no components because there is no data
 * to carry, but it is still meaningfully instantiated as new OpenInventoryPacket().
 */
public record OpenInventoryPacket() {

    public static void encode(OpenInventoryPacket packet, FriendlyByteBuf buf) {}

    public static OpenInventoryPacket decode(FriendlyByteBuf buf) {
        return new OpenInventoryPacket();
    }

    public static void handle(OpenInventoryPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                CoralineTriggers.OPEN_INVENTORY.trigger(player);
            }
        });
        context.setPacketHandled(true);
    }
}