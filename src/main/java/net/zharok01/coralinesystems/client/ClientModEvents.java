package net.zharok01.coralinesystems.client;

import com.github.alexthe666.citadel.client.rewards.CitadelCapes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.entity.brume.BrumeRenderer;
import net.zharok01.coralinesystems.client.entity.helper.HelperRenderer;
import net.zharok01.coralinesystems.client.entity.monster.MonsterRenderer;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // This is where we link our Entities to our Renderer:
        event.registerEntityRenderer(IsotopicEntities.HELPER.get(), HelperRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.MONSTER.get(), MonsterRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.BRUME.get(), BrumeRenderer::new);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        ClientPortalEffect.init();
    }
}