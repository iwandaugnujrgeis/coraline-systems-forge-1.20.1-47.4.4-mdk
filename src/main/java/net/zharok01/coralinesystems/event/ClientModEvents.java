package net.zharok01.coralinesystems.event;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.block.BrewingCauldronBlock;
import net.zharok01.coralinesystems.client.color.CoralineBlockColors;
import net.zharok01.coralinesystems.client.color.CoralineItemColors;
import net.zharok01.coralinesystems.client.entity.brume.BrumeRenderer;
import net.zharok01.coralinesystems.client.entity.helper.HelperRenderer;
import net.zharok01.coralinesystems.client.entity.monster.MonsterRenderer;
import net.zharok01.coralinesystems.client.entity.orb.OrbModel;
import net.zharok01.coralinesystems.client.entity.orb.OrbPulseRenderer;
import net.zharok01.coralinesystems.client.entity.orb.OrbRenderer;
import net.zharok01.coralinesystems.client.model.BrewingCauldronGeometry;
import net.zharok01.coralinesystems.client.particle.CauldronBubbleParticle;
import net.zharok01.coralinesystems.client.particle.CauldronSplashParticle;
import net.zharok01.coralinesystems.client.particle.OrbSparkleParticle;
import net.zharok01.coralinesystems.mixin.accessors.BiomeColorsAccessor;
import net.zharok01.coralinesystems.registry.*;
import net.zharok01.coralinesystems.util.BrewingCauldronInteractions;
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
        event.registerSpriteSet(CoralineParticles.CAULDRON_SPLASH.get(), CauldronSplashParticle.Provider::new);
        event.registerSpriteSet(CoralineParticles.CAULDRON_BUBBLE.get(), CauldronBubbleParticle.Provider::new);
    }

    /**
     * Registers our custom "coraline_systems:brewing_cauldron" geometry loader.
     *
     * This must fire on the MOD bus before model baking begins. The loader key
     * becomes "coraline_systems:brewing_cauldron" because ModelEvent.RegisterGeometryLoaders
     * automatically prepends the active mod namespace (set by ModLoadingContext).
     */
    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("brewing_cauldron", BrewingCauldronGeometry.Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BiomeColorsAccessor.coralineSystems$setGrassColorResolver(CoralineBiomeColors.GRASS_RESOLVER);
            BiomeColorsAccessor.coralineSystems$setFoliageColorResolver(CoralineBiomeColors.FOLIAGE_RESOLVER);
            BiomeColorsAccessor.coralineSystems$setWaterColorResolver(CoralineBiomeColors.WATER_RESOLVER);

            CauldronSplashParticle.TYPE = CoralineParticles.CAULDRON_SPLASH.get();
            CauldronBubbleParticle.TYPE = CoralineParticles.CAULDRON_BUBBLE.get();

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

    // ────────────────────────────────────────────────────────────────────────
    // Forge-bus gameplay events (client-only)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Separate inner class subscribed to the Forge event bus so we can listen
     * to gameplay events (like block interaction) on the client side.
     *
     * <p>This mirrors what Subtle Effects' {@code AbstractCauldronBlockMixin}
     * does for vanilla/Amendments cauldrons: on a successful right-click of our
     * Brewing Cauldron, spawn a burst of tinted splash particles at the fluid
     * surface. Because {@code PlayerInteractEvent.RightClickBlock} fires on the
     * client before the server round-trip completes, we fire the particles
     * immediately rather than scheduling them 2 ticks later — the visual
     * latency is imperceptible, and we avoid a dependency on SE's internal
     * {@code TickerManager}.</p>
     *
     * <p>We only fire particles when the held item is registered in our
     * {@link BrewingCauldronInteractions#BREWING} map, so random right-clicks
     * with irrelevant items produce no spurious effect.</p>
     */
    @Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            Level level = event.getLevel();
            // Only run on the client side, and only when the interaction was
            // not cancelled before it reached us.
            if (!level.isClientSide) return;
            if (event.getCancellationResult() == InteractionResult.FAIL) return;

            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);

            // Only our Brewing Cauldron.
            if (!(state.getBlock() instanceof BrewingCauldronBlock)) return;

            // Only items that are actually registered in our interaction map.
            // Items absent from the map would return PASS and produce no change,
            // so firing particles for them would be misleading.
            if (!BrewingCauldronInteractions.BREWING.containsKey(
                    event.getItemStack().getItem())) return;

            // Derive the tint color from the block entity the same way the
            // server does — the client BE is always in sync at this point.
            int rgb = CoralineBlockColors.CAULDRON_CONTENT.getColor(state, level, pos, 1);
            if (rgb == -1) return; // Cauldron is empty / no branch implied yet.

            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;

            // Fluid surface Y — same formula as BrewingCauldronBlock.getContentHeight.
            int cauldronLevel = state.getValue(BrewingCauldronBlock.LEVEL);
            double surfaceY = pos.getY() + (6.0 + cauldronLevel * 3.0) / 16.0;

            // Spawn a splash burst across the inner 10/16 of the cauldron.
            RandomSource rand = level.getRandom();
            int count = 10 + rand.nextInt(7); // 10-16 particles, slightly more than entity-entry
            for (int i = 0; i < count; i++) {
                double x = pos.getX() + 0.1875 + rand.nextDouble() * 0.625;
                double z = pos.getZ() + 0.1875 + rand.nextDouble() * 0.625;
                level.addParticle(CauldronSplashParticle.TYPE,
                        x, surfaceY, z,
                        r, g, b);
            }
        }
    }
}