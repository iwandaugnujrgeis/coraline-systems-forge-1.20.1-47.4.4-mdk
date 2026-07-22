package net.zharok01.coralinesystems.event;

import einstein.subtle_effects.ticking.tickers.TickerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
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
import net.zharok01.coralinesystems.block.BrewingCauldronBlockEntity;
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
import net.zharok01.coralinesystems.client.particle.CauldronFizzParticle;
import net.zharok01.coralinesystems.client.particle.CauldronSplashParticle;
import net.zharok01.coralinesystems.client.particle.OrbSparkleParticle;
import net.zharok01.coralinesystems.mixin.accessors.BiomeColorsAccessor;
import net.zharok01.coralinesystems.registry.*;
import net.zharok01.coralinesystems.util.block.BrewingCauldronInteractions;
import net.zharok01.coralinesystems.world.CoralineBiomeColors;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CoralineClientModEvents {

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
        event.registerSpriteSet(CoralineParticles.CAULDRON_FIZZ.get(), CauldronFizzParticle.Provider::new);
    }

    /**
     * Registers our custom "coraline_systems:brewing_cauldron" geometry loader.
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
            CauldronFizzParticle.TYPE   = CoralineParticles.CAULDRON_FIZZ.get();

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

    @Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            Level level = event.getLevel();
            if (!level.isClientSide) return;
            if (event.getCancellationResult() == InteractionResult.FAIL) return;

            BlockPos pos = event.getPos();
            BlockState stateBefore = level.getBlockState(pos);

            boolean isBrewingCauldron = stateBefore.getBlock() instanceof BrewingCauldronBlock;
            boolean isEmptyCauldron   = stateBefore.is(Blocks.CAULDRON);
            // Fix 1: also watch water cauldrons — Mulberries/Tea Leaves convert them
            // into a BrewingCauldron on the very first solid insertion.
            boolean isWaterCauldron   = stateBefore.is(Blocks.WATER_CAULDRON);

            // Gate 1 — we only care about these three cauldron types.
            if (!isBrewingCauldron && !isEmptyCauldron && !isWaterCauldron) return;

            // Gate 2 — filter to only items we care about for each cauldron type.
            net.minecraft.world.item.Item heldItem = event.getItemStack().getItem();
            if (isBrewingCauldron) {
                if (!BrewingCauldronInteractions.BREWING.containsKey(heldItem)) return;
            } else if (isEmptyCauldron) {
                // Only our custom liquid containers can convert an empty cauldron.
                if (!isCustomLiquidContainer(heldItem)) return;
            } else {
                // Water cauldron: only solid ingredients trigger a conversion.
                if (!isSolidIngredient(heldItem)) return;
            }

            // Snapshot pre-interaction state for change detection.
            int fingerprintBefore = brewingFingerprint(level, pos);
            int levelBefore = isBrewingCauldron
                    ? stateBefore.getValue(BrewingCauldronBlock.LEVEL)
                    : -1;
            int colorBefore = isBrewingCauldron
                    ? CoralineBlockColors.CAULDRON_CONTENT.getColor(stateBefore, level, pos, 1)
                    : -1;

            // Schedule the post-interaction check 2 ticks later, exactly as SE does.
            TickerManager.schedule(2, () -> {
                Level lvl = Minecraft.getInstance().level;
                if (lvl == null) return;

                BlockState stateAfter = lvl.getBlockState(pos);
                boolean isBrewingAfter = stateAfter.getBlock() instanceof BrewingCauldronBlock;

                // ── Case A: BrewingCauldron → BrewingCauldron ────────────────────
                // Covers: solid added, culture added, liquid topped up, partial drain.
                if (isBrewingCauldron && isBrewingAfter) {
                    int fingerprintAfter = brewingFingerprint(lvl, pos);
                    int levelAfter = stateAfter.getValue(BrewingCauldronBlock.LEVEL);
                    if (fingerprintAfter == fingerprintBefore && levelAfter == levelBefore) return;

                    int rgb = CoralineBlockColors.CAULDRON_CONTENT.getColor(stateAfter, lvl, pos, 1);
                    if (rgb == -1) return;
                    spawnSplash(lvl, pos, stateAfter, rgb);
                    return;
                }

                // ── Case B: BrewingCauldron → empty Cauldron ─────────────────────
                // Full-bucket drain: block replaced by vanilla empty cauldron.
                if (isBrewingCauldron && stateAfter.is(Blocks.CAULDRON)) {
                    if (colorBefore == -1) return;
                    spawnSplash(lvl, pos, stateBefore, colorBefore);
                    return;
                }

                // ── Case C: empty Cauldron → BrewingCauldron ─────────────────────
                // Pouring a custom liquid bottle/bucket into an empty vanilla cauldron.
                if (isEmptyCauldron && isBrewingAfter) {
                    int rgb = CoralineBlockColors.CAULDRON_CONTENT.getColor(stateAfter, lvl, pos, 1);
                    if (rgb == -1) return;
                    spawnSplash(lvl, pos, stateAfter, rgb);
                    return;
                }

                // ── Case D: Water Cauldron → BrewingCauldron ─────────────────────
                // First solid ingredient (Mulberries/Tea Leaves) dropped into a water
                // cauldron, converting it. This was the missing case causing no splash
                // on the very first interaction before the block type changed.
                if (isWaterCauldron && isBrewingAfter) {
                    int rgb = CoralineBlockColors.CAULDRON_CONTENT.getColor(stateAfter, lvl, pos, 1);
                    if (rgb == -1) return;
                    spawnSplash(lvl, pos, stateAfter, rgb);
                }

                // Any other outcome (no change, or some other block) → no particles.
            });
        }

        // ── Helpers ──────────────────────────────────────────────────────────────

        /**
         * Returns a compact integer fingerprint of the brewing-relevant fields of the
         * BrewingCauldronBlockEntity at {@code pos}, or {@code 0} if there is no BE
         * there.
         */
        private static int brewingFingerprint(Level level, BlockPos pos) {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return 0;
            return (be.getSolidStrength())
                    | (be.getCulture().ordinal()        << 3)
                    | (be.getImpliedCulture().ordinal() << 5)
                    | (be.getBrewState().ordinal()      << 7);
        }

        /**
         * Returns {@code true} if the item is a solid brewing ingredient that can be
         * added to a water cauldron to begin a brew (converting it to a
         * BrewingCauldronBlock in the process).
         */
        private static boolean isSolidIngredient(net.minecraft.world.item.Item item) {
            return item == CoralineItems.MULBERRIES.get()
                    || item == CoralineItems.TEA_LEAVES.get();
        }

        /**
         * Returns {@code true} if the given item is one of the custom liquid
         * containers that can be poured into a vanilla empty cauldron.
         */
        private static boolean isCustomLiquidContainer(net.minecraft.world.item.Item item) {
            return item == CoralineItems.MULBERRY_JUICE_BOTTLE.get()
                    || item == CoralineItems.MULBERRY_JUICE_BUCKET.get()
                    || item == CoralineItems.TEA_BOTTLE.get()
                    || item == CoralineItems.TEA_BUCKET.get()
                    || item == CoralineItems.WINE_BOTTLE.get()
                    || item == CoralineItems.WINE_BUCKET.get()
                    || item == CoralineItems.KOMBUCHA_BOTTLE.get()
                    || item == CoralineItems.KOMBUCHA_BUCKET.get()
                    || item == CoralineItems.DREGS_BOTTLE.get()
                    || item == CoralineItems.DREGS_BUCKET.get();
        }

        /**
         * Spawns a burst of tinted {@link CauldronSplashParticle}s at the fluid
         * surface of the cauldron at {@code pos}.
         */
        private static void spawnSplash(Level level, BlockPos pos, BlockState stateForHeight, int rgb) {
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >>  8) & 0xFF) / 255.0f;
            float b = ( rgb        & 0xFF) / 255.0f;

            int cauldronLevel = stateForHeight.getBlock() instanceof BrewingCauldronBlock
                    ? stateForHeight.getValue(BrewingCauldronBlock.LEVEL)
                    : 3;
            double surfaceY = pos.getY() + (6.0 + cauldronLevel * 3.0) / 16.0;

            RandomSource rand = level.getRandom();
            int count = 10 + rand.nextInt(7);
            for (int i = 0; i < count; i++) {
                double x = pos.getX() + 0.1875 + rand.nextDouble() * 0.625;
                double z = pos.getZ() + 0.1875 + rand.nextDouble() * 0.625;
                level.addParticle(CauldronSplashParticle.TYPE, x, surfaceY, z, r, g, b);
            }
        }
    }
}