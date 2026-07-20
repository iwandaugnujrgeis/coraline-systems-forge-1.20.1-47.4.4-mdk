package net.zharok01.coralinesystems.event;

import einstein.subtle_effects.ticking.tickers.TickerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
     * Subscribed to the Forge event bus to listen to block right-click events on
     * the client. Mirrors Subtle Effects' AbstractCauldronBlockMixin approach:
     *
     * <p>We snapshot the cauldron's relevant state (block identity + BE fingerprint)
     * <em>before</em> the interaction runs, then schedule a 2-tick callback via SE's
     * TickerManager to inspect the state <em>after</em> the interaction has resolved
     * on the client. Particles are only spawned if the state actually changed,
     * which is the precise robustness guarantee SE's mixin achieves via its
     * {@code @ModifyReturnValue} — we just reach the same "after" moment via delay
     * rather than a mixin hook.</p>
     *
     * <h3>What counts as a "changed" state?</h3>
     * <ul>
     *   <li><strong>Brewing Cauldron → Brewing Cauldron</strong> (solid/culture added,
     *       or liquid topped up): the BE fingerprint changes (solidStrength, culture,
     *       or impliedCulture). We read the post-interaction color and spawn particles.</li>
     *   <li><strong>Brewing Cauldron → Brewing Cauldron at a lower LEVEL</strong>
     *       (bottle/bucket drain): the block state's LEVEL property changes.</li>
     *   <li><strong>Brewing Cauldron → empty Cauldron</strong> (bucket drain of full
     *       cauldron): block identity changes away from BrewingCauldronBlock. We use
     *       the <em>pre</em>-interaction color because the BE is gone.</li>
     *   <li><strong>Empty Cauldron → Brewing Cauldron</strong> (pouring a custom liquid
     *       into an empty cauldron): the block was not a BrewingCauldronBlock before,
     *       but is one after. We use the <em>post</em>-interaction color.</li>
     * </ul>
     *
     * <p>For the "empty cauldron → Brewing Cauldron" case we also listen to
     * right-clicks on the vanilla empty cauldron block (Blocks.CAULDRON), because
     * the BREWING map is on the BrewingCauldronBlock — but the conversion interaction
     * is registered on CauldronInteraction.EMPTY (see BrewingCauldronInteractions).</p>
     */
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

            // Gate 1 — we only care about interactions with a BrewingCauldronBlock
            // or a vanilla empty cauldron (the conversion target for custom pours).
            if (!isBrewingCauldron && !isEmptyCauldron) return;

            // Gate 2 — the held item must be registered in one of our interaction
            // maps. For a BrewingCauldronBlock the relevant map is BREWING; for a
            // vanilla empty cauldron the relevant map is CauldronInteraction.EMPTY —
            // but rather than importing vanilla internals here, we check BREWING for
            // the BrewingCauldronBlock case, and for the empty-cauldron case we simply
            // check whether the item is any of our custom liquid containers (bottle or
            // bucket), since those are the only items that trigger a conversion there.
            if (isBrewingCauldron) {
                if (!BrewingCauldronInteractions.BREWING.containsKey(event.getItemStack().getItem())) return;
            } else {
                // Empty cauldron: only our custom liquid containers can convert it.
                if (!isCustomLiquidContainer(event.getItemStack().getItem())) return;
            }

            // Snapshot the pre-interaction BE fingerprint so we can detect changes
            // that don't manifest as block-state changes (solid/culture additions).
            int fingerprintBefore = brewingFingerprint(level, pos);

            // Snapshot the pre-interaction LEVEL so we can detect top-up and partial
            // drain interactions, which change only the block state and leave the BE
            // fingerprint untouched.
            int levelBefore = isBrewingCauldron
                    ? stateBefore.getValue(BrewingCauldronBlock.LEVEL)
                    : -1;

            // Snapshot the pre-interaction color for the "full drain → empty cauldron"
            // case, where the BE is gone by the time we check.
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
                // Covers: solid added, culture added, liquid topped up, bottle/bucket
                // drain that doesn't empty the cauldron completely.
                // We check BOTH the BE fingerprint (catches solid/culture additions,
                // which leave LEVEL untouched) AND the LEVEL property (catches top-up
                // and partial drain interactions, which leave the BE fields untouched).
                if (isBrewingCauldron && isBrewingAfter) {
                    int fingerprintAfter = brewingFingerprint(lvl, pos);
                    int levelAfter = stateAfter.getValue(BrewingCauldronBlock.LEVEL);
                    if (fingerprintAfter == fingerprintBefore && levelAfter == levelBefore) return; // Nothing changed.

                    int rgb = CoralineBlockColors.CAULDRON_CONTENT.getColor(stateAfter, lvl, pos, 1);
                    if (rgb == -1) return;
                    spawnSplash(lvl, pos, stateAfter, rgb);
                    return;
                }

                // ── Case B: BrewingCauldron → empty Cauldron ─────────────────────
                // Full-bucket drain: the block has been replaced by a vanilla empty
                // cauldron. Use the pre-interaction color (the BE is gone).
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
                }

                // Any other outcome (no change, or some other block entirely) → no particles.
            });
        }

        // ── Helpers ──────────────────────────────────────────────────────────────

        /**
         * Returns a compact integer fingerprint of the brewing-relevant fields of the
         * BrewingCauldronBlockEntity at {@code pos}, or {@code 0} if there is no BE
         * there. Used to detect additions of solid/culture items, which do not change
         * the block state but do mutate the BE.
         *
         * <p>Fields included: solidStrength (bits 0-2), culture ordinal (bits 3-4),
         * impliedCulture ordinal (bits 5-6), brewState ordinal (bits 7-8).</p>
         */
        private static int brewingFingerprint(Level level, BlockPos pos) {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return 0;
            return (be.getSolidStrength())
                    | (be.getCulture().ordinal()        << 3)
                    | (be.getImpliedCulture().ordinal() << 5)
                    | (be.getBrewState().ordinal()      << 7);
        }

        /**
         * Returns {@code true} if the given item is one of the custom liquid
         * containers that can be poured into a vanilla empty cauldron to produce
         * a BrewingCauldronBlock. These are the bottle and bucket forms of each
         * of our five liquid types.
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
         * surface of the cauldron at {@code pos}. The surface height is derived
         * from the block state that was actually active at the time of the splash
         * (pre for drains, post for fills/additions).
         */
        private static void spawnSplash(Level level, BlockPos pos, BlockState stateForHeight, int rgb) {
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >>  8) & 0xFF) / 255.0f;
            float b = ( rgb        & 0xFF) / 255.0f;

            // Surface Y derived from the cauldron LEVEL property, matching
            // BrewingCauldronBlock.getContentHeight().
            int cauldronLevel = stateForHeight.getBlock() instanceof BrewingCauldronBlock
                    ? stateForHeight.getValue(BrewingCauldronBlock.LEVEL)
                    : 3; // Pre-interaction state for full drains was level 3.
            double surfaceY = pos.getY() + (6.0 + cauldronLevel * 3.0) / 16.0;

            RandomSource rand = level.getRandom();
            int count = 10 + rand.nextInt(7); // 10–16 particles
            for (int i = 0; i < count; i++) {
                double x = pos.getX() + 0.1875 + rand.nextDouble() * 0.625;
                double z = pos.getZ() + 0.1875 + rand.nextDouble() * 0.625;
                level.addParticle(CauldronSplashParticle.TYPE, x, surfaceY, z, r, g, b);
            }
        }
    }
}