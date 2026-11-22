package net.zharok01.coralinesystems.quiver.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.network.QuiverInventoryPacket;
import net.zharok01.coralinesystems.quiver.QuiverContents;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class QuiverClientEvents {
    private static final int REQUEST_COOLDOWN_TICKS = 10;
    private static int requestCooldown;

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        if (screen != null && screen.getClass() == InventoryScreen.class && coraline$shouldUseQuiverInventory(Minecraft.getInstance().player)) {
            event.setCanceled(true);
            coraline$requestQuiverInventory();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (requestCooldown > 0) {
                requestCooldown--;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen != null && minecraft.screen.getClass() == InventoryScreen.class && coraline$shouldUseQuiverInventory(minecraft.player)) {
                coraline$requestQuiverInventory();
            }
        }
    }

    private static boolean coraline$shouldUseQuiverInventory(Player player) {
        return player != null && !player.getAbilities().instabuild && QuiverContents.isWornBy(player);
    }

    private static void coraline$requestQuiverInventory() {
        if (requestCooldown == 0) {
            CoralinePacketHandler.sendToServer(new QuiverInventoryPacket(true));
            requestCooldown = REQUEST_COOLDOWN_TICKS;
        }
    }
}
