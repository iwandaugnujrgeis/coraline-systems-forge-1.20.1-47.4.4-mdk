package net.zharok01.coralinesystems.util;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.zharok01.coralinesystems.block.BrewingCauldronBlock;
import net.zharok01.coralinesystems.block.BrewingCauldronBlockEntity;
import net.zharok01.coralinesystems.block.CultureType;
import net.zharok01.coralinesystems.item.CoralineFluidUtils;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import net.zharok01.coralinesystems.registry.CoralineItems;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Central registration point for every {@code CauldronInteraction} our
 * brewing system needs: adding solids/cultures, and now (Session 1.8)
 * universal DRAIN (Bottle/Bucket collection) and FILL (pouring a finished
 * drink back into an empty cauldron) for all five collectible substances --
 * Mulberry Juice, Tea, Kombucha, Wine, Dregs.
 * <p>
 * <b>Session 1.8 changes -- fixing two confirmed playtesting bugs:</b>
 * <ol>
 *   <li><b>Strength/volume conflation (fixed):</b> collection previously
 *   read and drained {@code BrewingCauldronBlock.LEVEL} (the 1-5
 *   solid-ingredient strength) as if it were also a volume counter. This
 *   meant a cauldron with only 1 Tea Leaf yielded exactly 1 Tea Bottle
 *   (draining the solid level down to 0/reverting the block), while a
 *   5-Tea-Leaf cauldron yielded 5. Vanilla's own water cauldron always
 *   yields exactly 3 Bottles regardless of what's dissolved in it --
 *   volume and strength are different axes and were wrongly sharing one
 *   number. Fixed by introducing {@link BrewingCauldronBlockEntity#getWaterLevel()}
 *   (1-3, independent of the solid LEVEL), which collection now drains
 *   instead. The solid LEVEL is read once per draw (to stamp strength) and
 *   is never modified by collection.</li>
 *   <li><b>No fill path (fixed):</b> previously only drain (Bottle/Bucket
 *   -&gt; player) was wired. Pouring a filled Wine/Tea/Kombucha/Mulberry-Juice/
 *   Dregs Bottle or Bucket INTO an empty vanilla {@code CAULDRON} did
 *   nothing. Fixed via {@code universalFillInteraction}, registered on
 *   {@code CauldronInteraction.EMPTY} (the interaction map a plain empty
 *   cauldron actually dispatches through) for all ten container items,
 *   mirroring vanilla's own {@code FILL_WATER}/{@code FILL_LAVA} shape.</li>
 * </ol>
 * <p>
 * Water volume is deliberately independent of the solid-ingredient LEVEL by
 * design decision -- adding a Mulberry/Tea Leaf must never change how much
 * liquid is in the cauldron. See
 * {@link BrewingCauldronBlockEntity#waterLevel}'s javadoc for the full
 * rationale. A future session may relax "conversion requires a FULL water
 * cauldron" to allow starting a brew from a partially-filled one -- parked,
 * not implemented here.
 */
public final class BrewingCauldronInteractions {

    private BrewingCauldronInteractions() {
    }

    public static final Map<Item, CauldronInteraction> BREWING = CauldronInteraction.newInteractionMap();

    private static final CauldronInteraction REJECT = (state, level, pos, player, hand, itemStack) -> InteractionResult.PASS;

    public static void bootstrap() {
        BREWING.put(Items.WATER_BUCKET, REJECT);
        BREWING.put(Items.LAVA_BUCKET, REJECT);
        BREWING.put(Items.POWDER_SNOW_BUCKET, REJECT);

        BREWING.put(CoralineItems.MULBERRIES.get(), addSolidInteraction(CultureType.WINE, CoralineItems.MULBERRIES.get()));
        BREWING.put(CoralineItems.TEA_LEAVES.get(), addSolidInteraction(CultureType.KOMBUCHA, CoralineItems.TEA_LEAVES.get()));

        BREWING.put(CoralineItems.YEAST.get(), addCultureInteraction(CultureType.WINE, CoralineItems.YEAST.get()));
        BREWING.put(CoralineItems.DREGS.get(), addCultureInteraction(CultureType.KOMBUCHA, CoralineItems.DREGS.get()));

        // Universal DRAIN: empty Bottle/Bucket -> filled drink container,
        // pulled FROM a BrewingCauldronBlock.
        BREWING.put(Items.GLASS_BOTTLE, universalCollectBottleInteraction(SoundEvents.BOTTLE_FILL));
        BREWING.put(Items.BUCKET, universalCollectBucketInteraction(SoundEvents.BUCKET_FILL));

        CauldronInteraction.WATER.put(CoralineItems.MULBERRIES.get(), convertToBrewingCauldron(CultureType.WINE, CoralineItems.MULBERRIES.get()));
        CauldronInteraction.WATER.put(CoralineItems.TEA_LEAVES.get(), convertToBrewingCauldron(CultureType.KOMBUCHA, CoralineItems.TEA_LEAVES.get()));

        // Universal FILL: filled drink Bottle/Bucket -> empty vanilla
        // CAULDRON, converting it into a fresh BrewingCauldronBlock seeded
        // with the poured drink's implied culture, strength (as solid
        // LEVEL), and full water volume. Registered on EMPTY, the map a
        // plain, empty cauldron actually dispatches through -- mirrors
        // vanilla's own FILL_WATER/FILL_LAVA/FILL_POWDER_SNOW slot.
        registerFillInteractions();
    }

    // ── Conversion (WATER_CAULDRON -> BrewingCauldronBlock via first solid) ──

    private static CauldronInteraction convertToBrewingCauldron(CultureType impliesCulture, Item solidItem) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!state.is(Blocks.WATER_CAULDRON) || state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) itemStack.shrink(1);
                player.awardStat(Stats.ITEM_USED.get(solidItem));

                level.setBlockAndUpdate(pos, CoralineBlocks.BREWING_CAULDRON.get().defaultBlockState());

                if (level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be) {
                    be.setImpliedCulture(impliesCulture);
                    be.setWaterLevel(BrewingCauldronBlockEntity.MAX_WATER_LEVEL);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    private static CauldronInteraction addSolidInteraction(CultureType impliesCulture, Item solidItem) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return InteractionResult.PASS;
            if (be.getCulture() != CultureType.NONE) return InteractionResult.PASS;

            CultureType existingBranch = be.getImpliedCulture();
            if (existingBranch != CultureType.NONE && existingBranch != impliesCulture) return InteractionResult.PASS;

            int currentLevel = state.getValue(BrewingCauldronBlock.LEVEL);
            if (currentLevel >= BrewingCauldronBlock.MAX_SOLID_LEVEL) return InteractionResult.PASS;

            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) itemStack.shrink(1);
                player.awardStat(Stats.ITEM_USED.get(solidItem));

                if (existingBranch == CultureType.NONE) be.setImpliedCulture(impliesCulture);
                // Solid LEVEL only -- waterLevel is untouched, by design.
                level.setBlockAndUpdate(pos, state.setValue(BrewingCauldronBlock.LEVEL, currentLevel + 1));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    private static CauldronInteraction addCultureInteraction(CultureType culture, Item cultureItem) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return InteractionResult.PASS;
            if (be.getCulture() != CultureType.NONE || be.getImpliedCulture() != culture) return InteractionResult.PASS;

            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) itemStack.shrink(1);
                player.awardStat(Stats.ITEM_USED.get(cultureItem));
                be.setCulture(culture);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    // ── Universal DRAIN (Bottle/Bucket <- BrewingCauldronBlock) ─────────────

    /**
     * Unified DRAIN logic for the Glass Bottle: pulls exactly ONE unit of
     * {@link BrewingCauldronBlockEntity#getWaterLevel()} per draw (mirrors
     * vanilla's own water cauldron, which always yields exactly 3 Bottles
     * from a full cauldron regardless of contents), stamping the resulting
     * drink's strength from the UNTOUCHED solid {@code BrewingCauldronBlock.LEVEL}.
     * Water volume and solid strength are read/written completely
     * independently -- this is the fix for the strength/volume conflation
     * bug (see class javadoc, item 1).
     */
    private static CauldronInteraction universalCollectBottleInteraction(SoundEvent fillSound) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return InteractionResult.PASS;

            Item resultItem = null;
            boolean appliesStrength = false;

            if (be.getCulture() == CultureType.NONE) {
                if (be.getImpliedCulture() == CultureType.KOMBUCHA) {
                    resultItem = CoralineItems.TEA_BOTTLE.get();
                    appliesStrength = true;
                } else if (be.getImpliedCulture() == CultureType.WINE) {
                    resultItem = CoralineItems.MULBERRY_JUICE_BOTTLE.get();
                    appliesStrength = true;
                }
            } else {
                // TODO (Session 2/4): once brewProgress/finished-state checks
                // exist, branch here: finished Wine -> WINE_BOTTLE, spoiled
                // Wine -> DREGS_BOTTLE, finished Kombucha -> KOMBUCHA_BOTTLE.
                // Blocked for now so players can't instantly harvest a
                // still-brewing batch.
                return InteractionResult.PASS;
            }

            if (resultItem == null) return InteractionResult.PASS;
            if (be.getWaterLevel() <= BrewingCauldronBlockEntity.MIN_WATER_LEVEL) return InteractionResult.PASS;

            // Strength comes from the SOLID level, which this interaction
            // never modifies -- only waterLevel is drained below.
            int strengthLevel = state.getValue(BrewingCauldronBlock.LEVEL);

            if (!level.isClientSide) {
                ItemStack filledStack = new ItemStack(resultItem);
                if (appliesStrength) {
                    CoralineFluidUtils.setStrength(filledStack, strengthLevel);
                }

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, filledStack));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));

                int newWaterLevel = be.getWaterLevel() - 1;
                if (newWaterLevel <= BrewingCauldronBlockEntity.MIN_WATER_LEVEL) {
                    // Cauldron is out of liquid -- revert fully, same as
                    // vanilla reverting LayeredCauldronBlock to plain
                    // Blocks.CAULDRON on underflow. This destroys the BE
                    // (standard onRemove behavior for an EntityBlock swap
                    // away), implicitly clearing culture/impliedCulture/
                    // progress/waterLevel with it -- nothing left to brew.
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                } else {
                    be.setWaterLevel(newWaterLevel);
                    // Solid LEVEL blockstate is untouched -- strength for
                    // the NEXT draw stays exactly what it was.
                }

                level.playSound(null, pos, fillSound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    /**
     * Unified DRAIN logic for the Bucket: always drains the ENTIRE
     * cauldron in one go (mirrors vanilla -- a Bucket never partially
     * empties a cauldron), reverting to a plain empty {@code CAULDRON}
     * unconditionally.
     */
    private static CauldronInteraction universalCollectBucketInteraction(SoundEvent fillSound) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return InteractionResult.PASS;

            Item resultItem = null;
            boolean appliesStrength = false;

            if (be.getCulture() == CultureType.NONE) {
                if (be.getImpliedCulture() == CultureType.KOMBUCHA) {
                    resultItem = CoralineItems.TEA_BUCKET.get();
                    appliesStrength = true;
                } else if (be.getImpliedCulture() == CultureType.WINE) {
                    resultItem = CoralineItems.MULBERRY_JUICE_BUCKET.get();
                    appliesStrength = true;
                }
            } else {
                // TODO (Session 2/4): fermented progress checks go here.
                return InteractionResult.PASS;
            }

            if (resultItem == null) return InteractionResult.PASS;

            // FIX: Buckets strictly require the cauldron to be filled to the brim!
            if (be.getWaterLevel() != BrewingCauldronBlockEntity.MAX_WATER_LEVEL) return InteractionResult.PASS;

            int strengthLevel = state.getValue(BrewingCauldronBlock.LEVEL);

            if (!level.isClientSide) {
                ItemStack filledStack = new ItemStack(resultItem);
                if (appliesStrength) {
                    CoralineFluidUtils.setStrength(filledStack, strengthLevel);
                }

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, filledStack));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(Items.BUCKET));

                // Buckets always take everything, same as vanilla.
                level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());

                level.playSound(null, pos, fillSound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    // ── Universal FILL (Bottle/Bucket of a finished drink -> empty CAULDRON) ──

    /**
     * Describes one drink's fill behavior: which implied culture it
     * converts an empty cauldron into, and whether the poured item carries
     * a strength value that should seed the new cauldron's solid LEVEL.
     */
    private record FillSpec(CultureType impliesCulture, boolean hasStrength) {
    }

    private static void registerFillInteractions() {
        // Mulberry Juice and Tea both pour into a cauldron the same way a
        // partially-brewed pre-culture drink would -- they seed the solid
        // LEVEL from the poured strength and set impliedCulture, exactly
        // mirroring what dropping loose Mulberries/Tea Leaves into a fresh
        // water cauldron would have produced.
        registerFillPair(CoralineItems.MULBERRY_JUICE_BOTTLE, CoralineItems.MULBERRY_JUICE_BUCKET,
                new FillSpec(CultureType.WINE, true));
        registerFillPair(CoralineItems.TEA_BOTTLE, CoralineItems.TEA_BUCKET,
                new FillSpec(CultureType.KOMBUCHA, true));

        // Kombucha and Wine are FINISHED, fermented drinks. Session 2/4 own
        // the actual "sits there finished, ready to re-collect" BE state;
        // for now, pouring these back just seeds the branch (no strength
        // concept re-applies the same way, since a finished drink's
        // strength was already baked in at brew start) -- Session 2/4
        // should revisit whether re-pouring a finished drink should be
        // legal at all once brewProgress/finished-state exists. Left as a
        // straightforward implied-culture seed for now so the fill/drain
        // symmetry described in this session's task is complete.
        registerFillPair(CoralineItems.WINE_BOTTLE, CoralineItems.WINE_BUCKET,
                new FillSpec(CultureType.WINE, true));
        registerFillPair(CoralineItems.KOMBUCHA_BOTTLE, CoralineItems.KOMBUCHA_BUCKET,
                new FillSpec(CultureType.KOMBUCHA, false));

        // Dregs pours back in as the CULTURE item's implied branch
        // (Kombucha), matching its role as Kombucha's required culture
        // input. No strength concept.
        registerFillPair(CoralineItems.DREGS_BOTTLE, CoralineItems.DREGS_BUCKET,
                new FillSpec(CultureType.KOMBUCHA, false));
    }

    private static void registerFillPair(Supplier<? extends Item> bottleItem, Supplier<? extends Item> bucketItem, FillSpec spec) {
        CauldronInteraction.EMPTY.put(bottleItem.get(), universalFillInteraction(spec, Items.GLASS_BOTTLE, SoundEvents.BOTTLE_EMPTY));
        CauldronInteraction.EMPTY.put(bucketItem.get(), universalFillInteraction(spec, Items.BUCKET, SoundEvents.BUCKET_EMPTY));
    }

    /**
     * Pours a filled drink container into a plain, empty vanilla
     * {@code CAULDRON}, converting it into a fresh {@code BrewingCauldronBlock}
     * seeded from the poured item. Registered on {@code CauldronInteraction.EMPTY}
     * -- the map a genuinely empty cauldron dispatches through, mirroring
     * where vanilla's own {@code FILL_WATER}/{@code FILL_LAVA}/
     * {@code FILL_POWDER_SNOW} live.
     * <p>
     * Bottle pours seed water volume at 1 (matching vanilla's own
     * Bottle-of-water-into-empty-cauldron behavior, which only ever raises
     * {@code LayeredCauldronBlock.LEVEL} by 1 per Bottle); Bucket pours
     * always seed a FULL cauldron (3), matching vanilla's Bucket-fill
     * behavior.
     */
    private static CauldronInteraction universalFillInteraction(FillSpec spec, Item emptyResult, SoundEvent emptySound) {
        return (state, level, pos, player, hand, itemStack) -> {
            boolean isBucket = emptyResult == Items.BUCKET;

            if (!level.isClientSide) {
                Item poured = itemStack.getItem();
                int strength = spec.hasStrength() ? CoralineFluidUtils.getStrength(itemStack) : BrewingCauldronBlock.MIN_SOLID_LEVEL;

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(emptyResult)));
                player.awardStat(Stats.FILL_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(poured));

                level.setBlockAndUpdate(pos, CoralineBlocks.BREWING_CAULDRON.get().defaultBlockState()
                        .setValue(BrewingCauldronBlock.LEVEL, Math.max(BrewingCauldronBlock.MIN_SOLID_LEVEL, Math.min(BrewingCauldronBlock.MAX_SOLID_LEVEL, strength))));

                if (level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be) {
                    be.setImpliedCulture(spec.impliesCulture());
                    be.setWaterLevel(isBucket ? BrewingCauldronBlockEntity.MAX_WATER_LEVEL : 1);
                }

                level.playSound(null, pos, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }
}
