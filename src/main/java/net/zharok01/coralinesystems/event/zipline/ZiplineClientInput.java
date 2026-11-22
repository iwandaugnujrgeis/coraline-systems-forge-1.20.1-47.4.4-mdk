package net.zharok01.coralinesystems.event.zipline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, value = Dist.CLIENT)
public class ZiplineClientInput {

    private static boolean lastState = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            lastState = false;
            return;
        }

        boolean isPressed = Minecraft.getInstance().options.keyUse.isDown();
        if (isPressed == lastState) return;

        CoralinePacketHandler.sendToServer(new ZiplineInputPacket(isPressed));
        lastState = isPressed;
        ZiplineStateCapability.setPressed(player, isPressed);

        if (!isPressed) {
            ZiplineHandler.stopZiplining(player);
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!ZiplineHandler.CLIENT_ZIPLINERS.containsKey(event.getEntity().getUUID())) return;

        Input input = event.getInput();
        input.leftImpulse = 0.0F;
        input.forwardImpulse = 0.0F;
        input.jumping = false;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ZiplineHandler.CLIENT_ZIPLINERS.clear();
        ZiplineHandler.coraline$clientObserverZiplines.clear();
        lastState = false;
    }
}