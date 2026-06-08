package net.zharok01.coralinesystems.zipline;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

@Mod.EventBusSubscriber(modid = "coraline_systems", value = Dist.CLIENT)
public class ZiplineClientInput {

    private static boolean lastState = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        boolean isPressed = Minecraft.getInstance().options.keyUse.isDown();
        if (isPressed != lastState) {
            CoralinePacketHandler.sendToServer(new ZiplineInputPacket(isPressed));
            lastState = isPressed;

            // Immediately stop ziplining on the client side for a snappy drop!
            if (!isPressed) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    ZiplineHandler.stopZiplining(player);
                }
            }
        }
    }
}