package net.zharok01.coralinesystems.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.color.CoralineBlockColors;
import net.zharok01.coralinesystems.client.color.CoralineItemColors;
import net.zharok01.coralinesystems.client.entity.brume.BrumeRenderer;
import net.zharok01.coralinesystems.client.entity.helper.HelperRenderer;
import net.zharok01.coralinesystems.client.entity.monster.MonsterRenderer;
import net.zharok01.coralinesystems.client.entity.orb.OrbModel;
import net.zharok01.coralinesystems.client.entity.orb.OrbPulseRenderer;
import net.zharok01.coralinesystems.client.entity.orb.OrbRenderer;
import net.zharok01.coralinesystems.client.particle.CauldronSplashParticle;
import net.zharok01.coralinesystems.client.particle.OrbSparkleParticle;
import net.zharok01.coralinesystems.mixin.accessors.BiomeColorsAccessor;
import net.zharok01.coralinesystems.registry.*;
import net.zharok01.coralinesystems.world.CoralineBiomeColors;

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

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CoralineParticles.ORB_SPARKLE.get(), OrbSparkleParticle.Factory::new);
        // Tinted splash particle for the Brewing Cauldron entity-inside effect.
        // The Provider reads r/g/b from the xSpeed/ySpeed/zSpeed slots that
        // CauldronSplashPacket passes via Level#addParticle.
        event.registerSpriteSet(CoralineParticles.CAULDRON_SPLASH.get(), CauldronSplashParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BiomeColorsAccessor.coralineSystems$setGrassColorResolver(CoralineBiomeColors.GRASS_RESOLVER);
            BiomeColorsAccessor.coralineSystems$setFoliageColorResolver(CoralineBiomeColors.FOLIAGE_RESOLVER);
            BiomeColorsAccessor.coralineSystems$setWaterColorResolver(CoralineBiomeColors.WATER_RESOLVER);

            // Initialise the convenience TYPE reference on CauldronSplashParticle so
            // CauldronSplashPacket can call level.addParticle(TYPE, ...) directly.
            // enqueueWork guarantees this runs after all DeferredRegisters have fired.
            CauldronSplashParticle.TYPE = CoralineParticles.CAULDRON_SPLASH.get();

            CoralineSystems.LOGGER.info("Successfully injected Coraline Systems Bidirectional Biome Color Noise!");
        });
    }

    @SubscribeEvent
    public static void onRegisterBlockColorHandlers(net.minecraftforge.client.event.RegisterColorHandlersEvent.Block event) {
        event.register(CoralineBlockColors.CAULDRON_CONTENT, CoralineBlocks.BREWING_CAULDRON.get());
    }

    @SubscribeEvent
    public static void onRegisterItemColorHandlers(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register(CoralineItemColors.MULBERRY_JUICE, CoralineItems.MULBERRY_JUICE_BOTTLE.get(), CoralineItems.MULBERRY_JUICE_BUCKET.get());
        event.register(CoralineItemColors.WINE, CoralineItems.WINE_BOTTLE.get(), CoralineItems.WINE_BUCKET.get());
        event.register(CoralineItemColors.TEA, CoralineItems.TEA_BOTTLE.get(), CoralineItems.TEA_BUCKET.get());
        event.register(CoralineItemColors.KOMBUCHA, CoralineItems.KOMBUCHA_BOTTLE.get(), CoralineItems.KOMBUCHA_BUCKET.get());
        event.register(CoralineItemColors.DREGS, CoralineItems.DREGS_BOTTLE.get(), CoralineItems.DREGS_BUCKET.get());
    }
}
