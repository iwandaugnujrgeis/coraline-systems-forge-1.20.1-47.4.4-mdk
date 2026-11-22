package net.zharok01.coralinesystems.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.item.CoralineNameTagItem;

import java.util.function.Supplier;

public class ServerboundNameTagPacket {

    public static final int MAX_NAME_LENGTH = 50;

    private final InteractionHand hand;
    private final String name;

    public ServerboundNameTagPacket(InteractionHand hand, String name) {
        this.hand = hand;
        this.name = name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }

    public static void encode(ServerboundNameTagPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.hand);
        buf.writeUtf(packet.name, MAX_NAME_LENGTH);
    }

    public static ServerboundNameTagPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        String name = buf.readUtf(MAX_NAME_LENGTH);
        return new ServerboundNameTagPacket(hand, name);
    }

    public static void handle(ServerboundNameTagPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;

        ItemStack itemStack = player.getItemInHand(packet.hand);
        if (!(itemStack.getItem() instanceof CoralineNameTagItem)) return;

        String filteredName = SharedConstants.filterText(packet.name).trim();
        String previousName = itemStack.hasCustomHoverName() ? itemStack.getHoverName().getString() : "";

        if (filteredName.isEmpty()) {
            itemStack.resetHoverName();
        } else {
            itemStack.setHoverName(Component.literal(filteredName));
        }

        if (!filteredName.equals(previousName)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.INK_SAC_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        CoralineSystems.LOGGER.debug("NameTag renamed by {} to '{}'", player.getScoreboardName(), filteredName);
    }
}