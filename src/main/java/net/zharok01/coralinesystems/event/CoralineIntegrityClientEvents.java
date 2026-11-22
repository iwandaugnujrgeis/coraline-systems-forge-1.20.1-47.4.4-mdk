package net.zharok01.coralinesystems.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.gui.CoralineIntegrityScreen;
import net.zharok01.coralinesystems.registry.CoralineIntegrityChecker;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, value = Dist.CLIENT)
public class CoralineIntegrityClientEvents {

    @SubscribeEvent
    public static void coraline$onScreenOpening(ScreenEvent.Opening event) {
        if (CoralineIntegrityChecker.getIssues().isEmpty() || event.getNewScreen() instanceof CoralineIntegrityScreen) return;

        event.setNewScreen(new CoralineIntegrityScreen(CoralineIntegrityChecker.getIssues()));
    }
}