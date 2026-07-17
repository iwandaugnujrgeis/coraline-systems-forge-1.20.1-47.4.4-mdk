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
import net.zharok01.coralinesystems.block.BrewState;
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
 * brewing system needs: adding solids/cultures, universal DRAIN (Bottle/
 * Bucket collection) and FILL (pouring a finished drink back into an
 * empty cauldron) for all five collectible substances -- Mulberry Juice,
 * Tea, Kombucha, Wine, Dregs.
 * <p>
 * <b>Session 1.8 changes -- fixing two confirmed playtesting bugs:</b>
 * <ol>
 *   <li><b>Strength/volume conflation (fixed):</b> collection previously
 *   read and drained {@code BrewingCauldronBlock.LEVEL} (the 1-5
 *   solid-ingredient strength) as if it were also a volume counter.
 *   Fixed by introducing {@link BrewingCauldronBlockEntity#getWaterLevel()}
 *   (1-3, independent of the solid LEVEL), which collection now drains
 *   instead. The solid LEVEL is read once per draw (to stamp strength) and
 *   is never modified by collection.</li>
 *   <li><b>No fill path (fixed):</b> previously only drain (Bottle/Bucket
 *   -&gt; player) was wired. Fixed via {@code universalFillInteraction},
 *   registered on {@code CauldronInteraction.EMPTY} for all ten container
 *   items, mirroring vanilla's own {@code FILL_WATER}/{@code FILL_LAVA}
 *   shape.</li>
 * </ol>
 * <p>
 * <b>Session 2 changes:</b>
 * <ul>
 *   <li>Drain interactions now branch on {@link BrewingCauldronBlockEntity#getBrewState()}
 *   once a culture is set: {@code FINISHED} yields the real Wine/Kombucha,
 *   {@code SPOILED} (Wine only) yields Dregs, and still-{@code BREWING}
 *   cauldrons refuse collection outright so a Player can't harvest a
 *   still-fermenting batch early. Wine carries its strength through to
 *   collection; Kombucha does not (Tea Leaf count only ever affected
 *   pre-culture Tea's strength, never Kombucha's -- confirmed by design).</li>
 *   <li><b>Round-trip pour-back bug (fixed):</b> playtesting found that
 *   collecting a finished Wine/Kombucha/Dregs and pouring it straight back
 *   into an empty cauldron, then collecting again, silently returned
 *   Mulberry Juice/Tea instead of the substance actually poured in. Root
 *   cause: {@code universalFillInteraction} only ever set the BE's
 *   {@code impliedCulture}, never its actual {@code culture} or
 *   {@code brewState} -- so a poured-back Wine landed in a BE with
 *   {@code culture == NONE}, which is exactly the state the drain
 *   interactions read as "pre-culture, hand back the unfermented drink."
 *   Fixed by giving {@link FillSpec} an explicit {@code resultCulture}/
 *   {@code resultState} pair: Mulberry Juice/Tea (genuinely pre-culture)
 *   still leave culture at NONE, but Wine/Kombucha/Dregs now restore both
 *   {@code culture} and {@code brewState} on pour-back, making them
 *   immediately re-collectible as themselves rather than needing to
 *   re-brew from scratch.</li>
 * </ul>
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
        // LEVEL), full water volume, and (Session 2) actual culture/
        // brewState for finished/spoiled drinks -- see class javadoc,
        // "Round-trip pour-back bug" entry.
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

                // PLACEHOLDER (Session 2, temp for testing -- Session 3 owns
                // real SFX/particles per design doc Section 3/4: "Triggers a
                // 'shh' fizzle sound effect and particle burst" on culture
                // add for both Wine and Kombucha). Reusing vanilla brewing
                // stand's own fizzle sound as the closest existing asset to
                // "shh," and SPLASH particles as a generic burst.
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,
                            pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                            12, 0.25, 0.1, 0.25, 0.0);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    // ── Universal DRAIN (Bottle/Bucket <- BrewingCauldronBlock) ─────────────

    /**
     * Unified DRAIN logic for the Glass Bottle: pulls exactly ONE unit of
     * {@link BrewingCauldronBlockEntity#getWaterLevel()} per draw, stamping
     * the resulting drink's strength from the UNTOUCHED solid
     * {@code BrewingCauldronBlock.LEVEL}. Water volume and solid strength
     * are read/written completely independently.
     * <p>
     * Session 2: once a culture is set, branches on
     * {@link BrewingCauldronBlockEntity#getBrewState()} -- FINISHED yields
     * the real drink (Wine keeps its strength; Kombucha doesn't), SPOILED
     * (Wine only) yields Dregs, and still-BREWING cauldrons refuse
     * collection so a Player can't harvest a still-fermenting batch.
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
            } else if (be.getBrewState() == BrewState.FINISHED) {
                if (be.getCulture() == CultureType.WINE) {
                    resultItem = CoralineItems.WINE_BOTTLE.get();
                    appliesStrength = true;
                } else if (be.getCulture() == CultureType.KOMBUCHA) {
                    resultItem = CoralineItems.KOMBUCHA_BOTTLE.get();
                }
            } else if (be.getBrewState() == BrewState.SPOILED) {
                resultItem = CoralineItems.DREGS_BOTTLE.get();
            } else {
                // Still actively BREWING -- block collection so a Player
                // can't instantly harvest a still-fermenting batch.
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
                    // Blocks.CAULDRON on underflow. This destroys the BE,
                    // implicitly clearing culture/impliedCulture/progress/
                    // brewState/waterLevel with it -- nothing left to brew.
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
     * unconditionally. Session 2 branching mirrors the Bottle path above.
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
            } else if (be.getBrewState() == BrewState.FINISHED) {
                if (be.getCulture() == CultureType.WINE) {
                    resultItem = CoralineItems.WINE_BUCKET.get();
                    appliesStrength = true;
                } else if (be.getCulture() == CultureType.KOMBUCHA) {
                    resultItem = CoralineItems.KOMBUCHA_BUCKET.get();
                }
            } else if (be.getBrewState() == BrewState.SPOILED) {
                resultItem = CoralineItems.DREGS_BUCKET.get();
            } else {
                return InteractionResult.PASS;
            }

            if (resultItem == null) return InteractionResult.PASS;

            // FIX (Session 1.8): Buckets strictly require the cauldron to
            // be filled to the brim!
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
     * Describes one drink's fill behavior. {@code impliesCulture} is the
     * branch an empty cauldron gets seeded into either way (via
     * {@code impliedCulture}, mirroring what dropping the loose solid
     * ingredient would have produced). {@code hasStrength} controls
     * whether the poured item's strength value seeds the new solid LEVEL.
     * <p>
     * {@code resultCulture}/{@code resultState} are the Session 2 fix for
     * the round-trip pour-back bug (see class javadoc): a finished/spoiled
     * drink poured back in must restore BOTH the BE's actual
     * {@code culture} (not just {@code impliedCulture}) AND
     * {@code brewState}, or the universal drain interactions -- which
     * branch on {@code culture == NONE} to decide "give the pre-culture
     * unfermented drink" -- silently misread the freshly-poured cauldron
     * as still pre-culture. {@code null resultCulture} means "leave
     * culture at NONE," which is correct for Mulberry Juice/Tea (they
     * really are pre-culture drinks, so misreading them as pre-culture
     * isn't a bug).
     */
    private record FillSpec(CultureType impliesCulture, boolean hasStrength,
                            CultureType resultCulture, BrewState resultState) {
    }

    private static void registerFillInteractions() {
        // Mulberry Juice and Tea are genuinely pre-culture -- pouring them
        // back in leaves the cauldron exactly where dropping loose
        // Mulberries/Tea Leaves into a fresh water cauldron would: culture
        // stays NONE, only impliedCulture is set. resultCulture is null,
        // so the round-trip fix below is a no-op for these two (correctly
        // -- there's nothing to fix here, culture==NONE is the right read).
        registerFillPair(CoralineItems.MULBERRY_JUICE_BOTTLE, CoralineItems.MULBERRY_JUICE_BUCKET,
                new FillSpec(CultureType.WINE, true, null, BrewState.BREWING));
        registerFillPair(CoralineItems.TEA_BOTTLE, CoralineItems.TEA_BUCKET,
                new FillSpec(CultureType.KOMBUCHA, true, null, BrewState.BREWING));

        // Wine and Kombucha are FINISHED, fermented drinks. Pouring one
        // back in restores culture AND marks the BE FINISHED, making it
        // immediately re-collectible as itself rather than being
        // misclassified as pre-culture. Strength re-applies for Wine
        // (still carries its 1-5 level); Kombucha has no strength concept
        // per design, so its solid LEVEL seed is inert either way.
        registerFillPair(CoralineItems.WINE_BOTTLE, CoralineItems.WINE_BUCKET,
                new FillSpec(CultureType.WINE, true, CultureType.WINE, BrewState.FINISHED));
        registerFillPair(CoralineItems.KOMBUCHA_BOTTLE, CoralineItems.KOMBUCHA_BUCKET,
                new FillSpec(CultureType.KOMBUCHA, false, CultureType.KOMBUCHA, BrewState.FINISHED));

        // Dregs is the SPOILED product of a Wine batch (and separately
        // doubles as Kombucha's required culture INPUT item -- those are
        // different roles). Poured back in, it's the spoiled substance
        // itself, so it restores as a spoiled Wine-branch cauldron, not as
        // some half-finished Kombucha state. No strength concept.
        registerFillPair(CoralineItems.DREGS_BOTTLE, CoralineItems.DREGS_BUCKET,
                new FillSpec(CultureType.WINE, false, CultureType.WINE, BrewState.SPOILED));
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
     * Bottle-of-water-into-empty-cauldron behavior); Bucket pours always
     * seed a FULL cauldron (3), matching vanilla's Bucket-fill behavior.
     * <p>
     * Session 2: also restores {@code culture}/{@code brewState} from
     * {@link FillSpec} when the poured drink is a finished/spoiled result
     * rather than a pre-culture drink -- see {@link FillSpec}'s javadoc
     * and the class-level "Round-trip pour-back bug" note for why this is
     * necessary.
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

                    // THE FIX: restore actual culture + brewState for
                    // finished/spoiled drinks, so the drain interactions'
                    // culture==NONE check doesn't misclassify them as
                    // pre-culture on the very next collection attempt.
                    if (spec.resultCulture() != null) {
                        be.setCulture(spec.resultCulture());
                        be.setBrewState(spec.resultState());
                    }
                }

                level.playSound(null, pos, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }
}