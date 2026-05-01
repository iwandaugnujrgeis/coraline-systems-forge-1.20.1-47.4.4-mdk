package net.zharok01.coralinesystems.event;

import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.gui.ScoreOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, value = Dist.CLIENT)
public class ClientScoreEvents {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        // Draw the custom overlay whenever the HOTBAR is rendering[cite: 7]
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            ScoreOverlay.render(event.getGuiGraphics(), event.getPartialTick());
        }
    }
}