package net.zharok01.coralinesystems.content.zipline;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.network.CoralinePacketHandler;

@Mod.EventBusSubscriber(modid = "coraline_systems", value = Dist.CLIENT)
public class ZiplineClientInput {

    private static boolean lastState = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // FIX: only fire at phase END — ClientTickEvent fires twice per tick
        // (START and END), so without this guard packets are sent twice as often.
        if (event.phase != TickEvent.Phase.END) return;

        boolean isPressed = Minecraft.getInstance().options.keyUse.isDown();
        if (isPressed != lastState) {
            CoralinePacketHandler.sendToServer(new ZiplineInputPacket(isPressed));
            lastState = isPressed;
        }
    }
}