package net.zharok01.coralinesystems.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.zharok01.coralinesystems.quiver.QuiverContents;
import net.zharok01.coralinesystems.quiver.QuiverInventoryMenu;

public record QuiverInventoryPacket(boolean open) {
    public static void encode(QuiverInventoryPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.open());
    }

    public static QuiverInventoryPacket decode(FriendlyByteBuf buffer) {
        return new QuiverInventoryPacket(buffer.readBoolean());
    }

    public static void handle(QuiverInventoryPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (packet.open()) {
            if (player.containerMenu == player.inventoryMenu && QuiverContents.isWornBy(player) && !player.getAbilities().instabuild) {
                ItemStack carried = player.containerMenu.getCarried();
                player.containerMenu.setCarried(ItemStack.EMPTY);
                NetworkHooks.openScreen(player, new SimpleMenuProvider((containerId, inventory, owner) -> new QuiverInventoryMenu(containerId, owner), Component.translatable("container.crafting")));
                player.containerMenu.setCarried(carried);
            }
        } else if (player.containerMenu instanceof QuiverInventoryMenu) {
            ItemStack carried = player.containerMenu.getCarried();
            player.containerMenu.setCarried(ItemStack.EMPTY);
            player.doCloseContainer();
            player.containerMenu.setCarried(carried);
        }
    }
}
