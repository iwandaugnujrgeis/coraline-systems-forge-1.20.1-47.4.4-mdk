package net.zharok01.coralinesystems.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.helper.HelperRenderer;
import net.zharok01.coralinesystems.client.monster.MonsterRenderer;
import net.zharok01.coralinesystems.client.render.StaticBlockRenderer;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // This is where we link our Entity to our Renderer
        // In the tutorial, he uses "RhinoRenderer::new", so we follow that pattern
        event.registerEntityRenderer(IsotopicEntities.HELPER.get(), HelperRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.MONSTER.get(), MonsterRenderer::new);

        event.registerBlockEntityRenderer(CoralineBlockEntities.STATIC_BLOCK_ENTITY.get(), StaticBlockRenderer::new);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        ClientPortalEffect.init();
    }
}