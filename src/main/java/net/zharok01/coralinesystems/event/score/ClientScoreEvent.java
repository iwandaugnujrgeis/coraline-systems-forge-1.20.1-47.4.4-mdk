package net.zharok01.coralinesystems.event.score;

import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.score.ScoreOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, value = Dist.CLIENT)
public class ClientScoreEvent {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        // Draw the custom overlay whenever the HOTBAR is rendering
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            ScoreOverlay.render(event.getGuiGraphics(), event.getPartialTick());
        }
    }
}