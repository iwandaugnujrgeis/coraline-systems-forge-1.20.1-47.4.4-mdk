package net.zharok01.coralinesystems.client.gui.compat.controlify;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CoralineControlifyHook {

    @SubscribeEvent
    public static void coraline$onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("controlify")) {
            event.enqueueWork(CoralineControlifyCompat::coraline$register);
        }
    }
}
