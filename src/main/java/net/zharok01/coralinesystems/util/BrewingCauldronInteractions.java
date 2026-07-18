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
        // CAULDRON, converting it into a fresh BrewingCauldronBlock.
        registerFillInteractions();

        // Universal TOP-UP: filled drink Bottle/Bucket -> an ALREADY-
        // converted BrewingCauldronBlock holding the same substance.
        // Fixes the bottle-restacking bug -- see class javadoc.
        registerTopUpInteractions();
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

                // New BrewingCauldronBlock defaults to LEVEL=3 (full water)
                // via its registerDefaultState -- correct here since we only
                // ever convert from an already-full water cauldron.
                level.setBlockAndUpdate(pos, CoralineBlocks.BREWING_CAULDRON.get().defaultBlockState());

                // setBlockAndUpdate above already fires the placement's own
                // block-update/render pass with a freshly-constructed BE that
                // still holds Java-default fields (culture=NONE, strength=1
                // as a coincidence, not a guarantee). Setting fields via the
                // normal syncVisuals()-triggering setters here would only add
                // a SECOND update after that first (wrong-looking) one --
                // which is exactly the white/untinted flash. Instead, set
                // every field silently in one shot, then send exactly one
                // correct sync ourselves.
                if (level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be) {
                    be.initializeVisualsSilently(
                            CultureType.NONE, impliesCulture,
                            BrewingCauldronBlockEntity.MIN_SOLID_STRENGTH,
                            BrewState.BREWING, 0L);
                    be.syncToClient();
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

            int currentStrength = be.getSolidStrength();
            if (currentStrength >= BrewingCauldronBlockEntity.MAX_SOLID_STRENGTH) return InteractionResult.PASS;

            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) itemStack.shrink(1);
                player.awardStat(Stats.ITEM_USED.get(solidItem));

                if (existingBranch == CultureType.NONE) be.setImpliedCulture(impliesCulture);
                // Solid strength only -- blockstate LEVEL (water volume) is
                // untouched, by design.
                be.setSolidStrength(currentStrength + 1);
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
                return InteractionResult.PASS;
            }

            if (resultItem == null) return InteractionResult.PASS;

            int currentWaterLevel = state.getValue(BrewingCauldronBlock.LEVEL);
            if (currentWaterLevel <= 0) return InteractionResult.PASS;

            // Strength comes from the BE, which this interaction never modifies.
            int strengthLevel = be.getSolidStrength();

            if (!level.isClientSide) {
                ItemStack filledStack = new ItemStack(resultItem);
                if (appliesStrength) {
                    CoralineFluidUtils.setStrength(filledStack, strengthLevel);
                }

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, filledStack));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));

                int newWaterLevel = currentWaterLevel - 1;
                if (newWaterLevel <= 0) {
                    // Cauldron is out of liquid -- revert fully, same as
                    // vanilla reverting LayeredCauldronBlock to plain
                    // Blocks.CAULDRON on underflow. This destroys the BE,
                    // implicitly clearing culture/impliedCulture/progress/
                    // brewState/solidStrength with it -- nothing left to brew.
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                } else {
                    level.setBlockAndUpdate(pos, state.setValue(BrewingCauldronBlock.LEVEL, newWaterLevel));
                    // Solid strength on the BE is untouched -- strength for
                    // the NEXT draw stays exactly what it was.
                }

                level.playSound(null, pos, fillSound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

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

            // Buckets strictly require the cauldron to be filled to the brim.
            if (state.getValue(BrewingCauldronBlock.LEVEL) != 3) return InteractionResult.PASS;

            int strengthLevel = be.getSolidStrength();

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

    private record FillSpec(CultureType impliesCulture, boolean hasStrength,
                            CultureType resultCulture, BrewState resultState) {
    }

    private static void registerFillInteractions() {
        registerFillPair(CoralineItems.MULBERRY_JUICE_BOTTLE, CoralineItems.MULBERRY_JUICE_BUCKET,
                new FillSpec(CultureType.WINE, true, null, BrewState.BREWING));
        registerFillPair(CoralineItems.TEA_BOTTLE, CoralineItems.TEA_BUCKET,
                new FillSpec(CultureType.KOMBUCHA, true, null, BrewState.BREWING));
        registerFillPair(CoralineItems.WINE_BOTTLE, CoralineItems.WINE_BUCKET,
                new FillSpec(CultureType.WINE, true, CultureType.WINE, BrewState.FINISHED));
        registerFillPair(CoralineItems.KOMBUCHA_BOTTLE, CoralineItems.KOMBUCHA_BUCKET,
                new FillSpec(CultureType.KOMBUCHA, false, CultureType.KOMBUCHA, BrewState.FINISHED));
        registerFillPair(CoralineItems.DREGS_BOTTLE, CoralineItems.DREGS_BUCKET,
                new FillSpec(CultureType.WINE, false, CultureType.WINE, BrewState.SPOILED));
    }

    private static void registerFillPair(Supplier<? extends Item> bottleItem, Supplier<? extends Item> bucketItem, FillSpec spec) {
        CauldronInteraction.EMPTY.put(bottleItem.get(), universalFillInteraction(spec, Items.GLASS_BOTTLE, SoundEvents.BOTTLE_EMPTY));
        CauldronInteraction.EMPTY.put(bucketItem.get(), universalFillInteraction(spec, Items.BUCKET, SoundEvents.BUCKET_EMPTY));
    }

    private static CauldronInteraction universalFillInteraction(FillSpec spec, Item emptyResult, SoundEvent emptySound) {
        return (state, level, pos, player, hand, itemStack) -> {
            boolean isBucket = emptyResult == Items.BUCKET;

            if (!level.isClientSide) {
                Item poured = itemStack.getItem();
                int strength = spec.hasStrength() ? CoralineFluidUtils.getStrength(itemStack) : BrewingCauldronBlockEntity.MIN_SOLID_STRENGTH;

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(emptyResult)));
                player.awardStat(Stats.FILL_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(poured));

                int seededWaterLevel = isBucket ? 3 : 1;
                level.setBlockAndUpdate(pos, CoralineBlocks.BREWING_CAULDRON.get().defaultBlockState()
                        .setValue(BrewingCauldronBlock.LEVEL, seededWaterLevel));

                // As with convertToBrewingCauldron: the block placement above
                // already renders once using a freshly-constructed BE with
                // default fields. Previously this used 3-4 chained setters,
                // each firing its own syncVisuals() -- i.e. several extra
                // renders on top of the placement's own, each one a chance
                // for a stale/untinted frame to slip through. Set everything
                // silently in one shot instead, then sync exactly once.
                if (level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be) {
                    CultureType finalCulture = spec.resultCulture() != null ? spec.resultCulture() : CultureType.NONE;
                    BrewState finalState = spec.resultCulture() != null ? spec.resultState() : BrewState.BREWING;
                    int clampedStrength = Math.max(BrewingCauldronBlockEntity.MIN_SOLID_STRENGTH,
                            Math.min(BrewingCauldronBlockEntity.MAX_SOLID_STRENGTH, strength));

                    be.initializeVisualsSilently(finalCulture, spec.impliesCulture(), clampedStrength, finalState, 0L);
                    be.syncToClient();
                }

                level.playSound(null, pos, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    // ── Universal TOP-UP (Bottle/Bucket of a drink -> an already-brewing
    //     BrewingCauldronBlock holding the SAME substance) ──────────────────
    //     Fixes the bottle-restacking bug -- see class javadoc.

    private static void registerTopUpInteractions() {
        registerTopUpPair(CoralineItems.MULBERRY_JUICE_BOTTLE, CoralineItems.MULBERRY_JUICE_BUCKET,
                new FillSpec(CultureType.WINE, true, null, BrewState.BREWING));
        registerTopUpPair(CoralineItems.TEA_BOTTLE, CoralineItems.TEA_BUCKET,
                new FillSpec(CultureType.KOMBUCHA, true, null, BrewState.BREWING));
        registerTopUpPair(CoralineItems.WINE_BOTTLE, CoralineItems.WINE_BUCKET,
                new FillSpec(CultureType.WINE, true, CultureType.WINE, BrewState.FINISHED));
        registerTopUpPair(CoralineItems.KOMBUCHA_BOTTLE, CoralineItems.KOMBUCHA_BUCKET,
                new FillSpec(CultureType.KOMBUCHA, false, CultureType.KOMBUCHA, BrewState.FINISHED));
        registerTopUpPair(CoralineItems.DREGS_BOTTLE, CoralineItems.DREGS_BUCKET,
                new FillSpec(CultureType.WINE, false, CultureType.WINE, BrewState.SPOILED));
    }

    private static void registerTopUpPair(Supplier<? extends Item> bottleItem, Supplier<? extends Item> bucketItem, FillSpec spec) {
        BREWING.put(bottleItem.get(), topUpInteraction(spec, false, SoundEvents.BOTTLE_EMPTY));
        BREWING.put(bucketItem.get(), topUpInteraction(spec, true, SoundEvents.BUCKET_EMPTY));
    }

    private static boolean matchesExistingContents(BrewingCauldronBlockEntity be, FillSpec spec) {
        if (spec.resultCulture() != null) {
            return be.getCulture() == spec.resultCulture() && be.getBrewState() == spec.resultState();
        } else {
            return be.getCulture() == CultureType.NONE && be.getImpliedCulture() == spec.impliesCulture();
        }
    }

    private static CauldronInteraction topUpInteraction(FillSpec spec, boolean isBucket, SoundEvent emptySound) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return InteractionResult.PASS;

            // 1. Check if the substance (Culture/State) matches.
            if (!matchesExistingContents(be, spec)) return InteractionResult.PASS;

            // 2. Exploit Fix: Check if the strength perfectly matches!
            if (spec.hasStrength()) {
                int pouredStrength = CoralineFluidUtils.getStrength(itemStack);
                if (pouredStrength != be.getSolidStrength()) {
                    return InteractionResult.PASS; // Refuse dilution/cheap top-ups!
                }
            }

            int currentWaterLevel = state.getValue(BrewingCauldronBlock.LEVEL);

            if (isBucket) {
                if (currentWaterLevel == 3) return InteractionResult.PASS;
            } else {
                if (currentWaterLevel >= 3) return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                Item poured = itemStack.getItem();
                Item emptyResult = isBucket ? Items.BUCKET : Items.GLASS_BOTTLE;

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(emptyResult)));
                player.awardStat(Stats.FILL_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(poured));

                int newWaterLevel = isBucket ? 3 : currentWaterLevel + 1;
                level.setBlockAndUpdate(pos, state.setValue(BrewingCauldronBlock.LEVEL, newWaterLevel));

                level.playSound(null, pos, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }
}