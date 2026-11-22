package net.zharok01.coralinesystems.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

public record OpenRecipeBookPacket() {

    public static void encode(OpenRecipeBookPacket packet, FriendlyByteBuf buf) {}

    public static OpenRecipeBookPacket decode(FriendlyByteBuf buf) {
        return new OpenRecipeBookPacket();
    }

    public static void handle(OpenRecipeBookPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                CoralineTriggers.OPEN_RECIPE_BOOK.trigger(player);
            }
        });
        context.setPacketHandled(true);
    }
}