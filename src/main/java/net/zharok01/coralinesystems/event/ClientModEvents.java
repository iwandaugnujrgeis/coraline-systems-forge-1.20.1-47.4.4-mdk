package net.zharok01.coralinesystems.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.entity.brume.BrumeRenderer;
import net.zharok01.coralinesystems.client.entity.helper.HelperRenderer;
import net.zharok01.coralinesystems.client.entity.monster.MonsterRenderer;
import net.zharok01.coralinesystems.client.entity.orb.OrbModel;
import net.zharok01.coralinesystems.client.entity.orb.OrbRenderer;
import net.zharok01.coralinesystems.client.entity.orb.OrbPulseRenderer;
import net.zharok01.coralinesystems.registry.CoralineModelLayers;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(IsotopicEntities.HELPER.get(), HelperRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.MONSTER.get(), MonsterRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.BRUME.get(), BrumeRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.ORB.get(), OrbRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.ORB_PULSE.get(), OrbPulseRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CoralineModelLayers.ORB_LAYER, OrbModel::createBodyLayer);
    }
}