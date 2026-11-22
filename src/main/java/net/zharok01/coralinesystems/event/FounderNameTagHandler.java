package net.zharok01.coralinesystems.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.FounderNameColors;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FounderNameTagHandler {

    @SubscribeEvent
    public static void coraline$onRenderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof Player player) {
            Component original = event.getContent();
            event.setContent(FounderNameColors.coraline$applyColor(player.getUUID(), original));
        }
    }
}