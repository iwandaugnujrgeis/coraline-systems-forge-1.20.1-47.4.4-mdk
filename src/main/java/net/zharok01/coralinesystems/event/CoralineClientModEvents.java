package net.zharok01.coralinesystems.event;

import net.mcreator.sweetygarden.init.SweetyGardenModBlocks;
import net.mcreator.sweetygarden.init.SweetyGardenModItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.block.CentrifugeRenderer;
import net.zharok01.coralinesystems.block.TesseractBlockEntityRenderer;
import net.zharok01.coralinesystems.client.color.CoralineBlockColors;
import net.zharok01.coralinesystems.client.color.CoralineItemColors;
import net.zharok01.coralinesystems.client.entity.render.ObsidianBoatRenderer;
import net.zharok01.coralinesystems.client.entity.render.brume.BrumeRenderer;
import net.zharok01.coralinesystems.client.entity.render.cryptid.CryptidModel;
import net.zharok01.coralinesystems.client.entity.render.cryptid.CryptidRenderer;
import net.zharok01.coralinesystems.client.entity.render.helper.HelperRenderer;
import net.zharok01.coralinesystems.client.entity.render.monster.MonsterRenderer;
import net.zharok01.coralinesystems.client.entity.render.orb.OrbModel;
import net.zharok01.coralinesystems.client.entity.render.orb.OrbPulseRenderer;
import net.zharok01.coralinesystems.client.entity.render.orb.OrbRenderer;
import net.zharok01.coralinesystems.client.entity.render.star.StarRenderer;
import net.zharok01.coralinesystems.client.gui.ContainerBlockScreen;
import net.zharok01.coralinesystems.client.model.BrewingCauldronGeometry;
import net.zharok01.coralinesystems.client.particle.*;
import net.zharok01.coralinesystems.event.static_portal.StaticPortalOverlay;
import net.zharok01.coralinesystems.mixin.accessor.BiomeColorsAccessor;
import net.zharok01.coralinesystems.mixin.accessor.EntityAccessor;
import net.zharok01.coralinesystems.registry.*;
import net.zharok01.coralinesystems.util.CoralineDyeColors;
import net.zharok01.coralinesystems.worldgen.CoralineBiomeColors;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CoralineClientModEvents {

    private static final double DAMAGE_INDICATOR_BLEED_RADIUS = 1.5;
    private static final int SMOKE_PUFFS_PER_INDICATOR = 3;
    private static final double SMOKE_VELOCITY_SCALE = 0.25;
    private static final double SMOKE_RISE_SPEED = 0.02;
    private static final double SMOKE_FALLBACK_HEIGHT_OFFSET = 0.4;

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CoralineKeyMappings.TOGGLE_RECIPE_BOOK);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BiomeColorsAccessor.coraline$setGrassColorResolver(CoralineBiomeColors.GRASS_RESOLVER);

            CauldronSplashParticle.TYPE = CoralineParticles.CAULDRON_SPLASH.get();
            CauldronBubbleParticle.TYPE = CoralineParticles.CAULDRON_BUBBLE.get();
            CauldronFizzParticle.TYPE = CoralineParticles.CAULDRON_FIZZ.get();

            CoralineBlocks.registerRenderLayers();
            StaticPortalOverlay.init();
            MenuScreens.register(CoralineMenuTypes.CONTAINER_BLOCK.get(), ContainerBlockScreen::new);
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(IsotopicEntities.HELPER.get(), HelperRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.MONSTER.get(), MonsterRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.BRUME.get(), BrumeRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.ORB.get(), OrbRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.CRYPTID.get(), CryptidRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.ORB_PULSE.get(), OrbPulseRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.THROWN_PLATYPUS_EGG.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.THROWN_CAIMAN_EGG.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.THROWN_DIMENSIONAL_SHARD.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.THROWN_TURTLE_EGG.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.OBSIDIAN_BOAT.get(), ObsidianBoatRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.STAR.get(), StarRenderer::new);
        event.registerEntityRenderer(IsotopicEntities.THROWN_STICK.get(), ThrownItemRenderer::new);

        event.registerBlockEntityRenderer(CoralineBlockEntities.CENTRIFUGE.get(), CentrifugeRenderer::new);
        event.registerBlockEntityRenderer(CoralineBlockEntities.TESSERACT.get(), TesseractBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CoralineModelLayers.ORB_LAYER, OrbModel::createBodyLayer);
        event.registerLayerDefinition(CoralineModelLayers.OBSIDIAN_BOAT_LAYER, BoatModel::createBodyModel);
        event.registerLayerDefinition(CoralineModelLayers.CRYPTID, CryptidModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {

        event.registerSpecial(CoralineParticles.ORB_SHIELD_BURST.get(), new OrbShieldBurstParticle.Factory());
        event.registerSpriteSet(CoralineParticles.TIMEDUST_SPARKLE.get(), OrbSparkleParticle.TimeDustFactory::new);
        event.registerSpriteSet(CoralineParticles.STAR_SPARKLE.get(), OrbSparkleParticle.StarFactory::new);

        event.registerSpriteSet(CoralineParticles.ORB_SPARKLE.get(), OrbSparkleParticle.Factory::new);
        event.registerSpriteSet(CoralineParticles.BLAZE_SPARKLE.get(), OrbSparkleParticle.BlazeFactory::new);
        event.registerSpriteSet(CoralineParticles.CAULDRON_SPLASH.get(), CauldronSplashParticle.Provider::new);
        event.registerSpriteSet(CoralineParticles.CAULDRON_BUBBLE.get(), CauldronBubbleParticle.Provider::new);
        event.registerSpriteSet(CoralineParticles.CAULDRON_FIZZ.get(), CauldronFizzParticle.Provider::new);
        event.registerSpriteSet(CoralineParticles.BLOOD_DROPLET.get(), BloodDropletParticle.Provider::new);
        event.registerSpecial(CoralineParticles.SHARD_BURST.get(), new ShardBurstParticle.Factory());

        event.registerSpriteSet(ParticleTypes.DAMAGE_INDICATOR, spriteSet -> (type, level, x, y, z, xSpeed, ySpeed, zSpeed) -> {
            AABB searchArea = new AABB(x, y, z, x, y, z).inflate(DAMAGE_INDICATOR_BLEED_RADIUS);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, searchArea);
            boolean bleeds = nearby.stream().anyMatch(entity -> entity.getType().is(CoralineTags.BLEEDS));

            if (!bleeds) {
                LivingEntity target = nearby.stream()
                        .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(x, y, z)))
                        .orElse(null);

                if (target != null) {
                    RandomSource entityRandom = ((EntityAccessor) target).coraline$getRandom();
                    for (int i = 0; i < SMOKE_PUFFS_PER_INDICATOR; i++) {
                        double px = target.getX() + (entityRandom.nextDouble() - 0.5) * target.getBbWidth();
                        double py = target.getY() + entityRandom.nextDouble() * target.getBbHeight();
                        double pz = target.getZ() + (entityRandom.nextDouble() - 0.5) * target.getBbWidth();

                        level.addParticle(ParticleTypes.SMOKE,
                                px,
                                py,
                                pz,
                                xSpeed * SMOKE_VELOCITY_SCALE + (entityRandom.nextDouble() - 0.5) * 0.1,
                                ySpeed * SMOKE_VELOCITY_SCALE + SMOKE_RISE_SPEED + entityRandom.nextDouble() * 0.1,
                                zSpeed * SMOKE_VELOCITY_SCALE + (entityRandom.nextDouble() - 0.5) * 0.1);
                    }
                } else {
                    for (int i = 0; i < SMOKE_PUFFS_PER_INDICATOR; i++) {
                        level.addParticle(ParticleTypes.SMOKE,
                                x + (level.random.nextDouble() - 0.5) * 0.2,
                                y + SMOKE_FALLBACK_HEIGHT_OFFSET + (level.random.nextDouble() - 0.5) * 0.2,
                                z + (level.random.nextDouble() - 0.5) * 0.2,
                                xSpeed * SMOKE_VELOCITY_SCALE + (level.random.nextDouble() - 0.5) * 0.1,
                                ySpeed * SMOKE_VELOCITY_SCALE + SMOKE_RISE_SPEED + level.random.nextDouble() * 0.1,
                                zSpeed * SMOKE_VELOCITY_SCALE + (level.random.nextDouble() - 0.5) * 0.1);
                    }
                }
            }

            return null;
        });
    }

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("brewing_cauldron", BrewingCauldronGeometry.Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register(CoralineBlockColors.CAULDRON_CONTENT, CoralineBlocks.BREWING_CAULDRON.get());
        // event.register(CoralineBlockColors.WOOD_SORREL, SweetyGardenModBlocks.WOOD_SORREL.get());

        for (CoralineDyeColors color : CoralineDyeColors.values()) {
            int tint = coraline$tintOf(color);
            var dye = color.dyeColor();
            event.register((state, level, pos, index) -> tint,
                    CoralineBlocks.STAINED_GLASS.get(dye).get(),
                    CoralineBlocks.STAINED_GLASS_PANE.get(dye).get());
        }
    }

    @SubscribeEvent
    public static void onRegisterItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register(CoralineItemColors.MULBERRY_JUICE, CoralineItems.MULBERRY_JUICE_BOTTLE.get());
        event.register(CoralineItemColors.WINE, CoralineItems.WINE_BOTTLE.get());
        event.register(CoralineItemColors.TEA, CoralineItems.TEA_BOTTLE.get());
        event.register(CoralineItemColors.KOMBUCHA, CoralineItems.KOMBUCHA_BOTTLE.get());
        event.register(CoralineItemColors.DREGS, CoralineItems.DREGS_BOTTLE.get());

        // event.register(CoralineItemColors.WOOD_SORREL, SweetyGardenModItems.WOOD_SORREL.get());

        for (CoralineDyeColors color : CoralineDyeColors.values()) {
            int tint = coraline$tintOf(color);
            var dye = color.dyeColor();
            event.register((stack, index) -> tint,
                    CoralineBlocks.STAINED_GLASS.get(dye).get(),
                    CoralineBlocks.STAINED_GLASS_PANE.get(dye).get());
        }
    }

    private static int coraline$tintOf(CoralineDyeColors color) {
        float[] rgb = color.getTextureDiffuseColors();
        return ((int) (rgb[0] * 255) << 16) | ((int) (rgb[1] * 255) << 8) | (int) (rgb[2] * 255);
    }
}