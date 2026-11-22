package net.zharok01.coralinesystems.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.FounderNameColors;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FounderPlayerNameHandler {

    @SubscribeEvent
    public static void coraline$onPlayerNameFormat(PlayerEvent.NameFormat event) {
        event.setDisplayname(FounderNameColors.coraline$applyColor(
                event.getEntity().getUUID(),
                event.getDisplayname()
        ));
    }

    @SubscribeEvent
    public static void coraline$onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        Player player = event.getEntity();
        Component displayName = event.getDisplayName();
        if (displayName == null) {
            displayName = player.getDisplayName();
        }
        event.setDisplayName(FounderNameColors.coraline$applyColor(player.getUUID(), displayName));
    }
}
