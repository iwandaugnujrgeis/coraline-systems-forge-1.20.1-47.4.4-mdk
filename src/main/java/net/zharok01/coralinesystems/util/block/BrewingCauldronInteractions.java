package net.zharok01.coralinesystems.util.block;

import com.teamabnormals.upgrade_aquatic.core.registry.UAItems;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.zharok01.coralinesystems.block.BrewState;
import net.zharok01.coralinesystems.block.BrewingCauldronBlock;
import net.zharok01.coralinesystems.block.BrewingCauldronBlockEntity;
import net.zharok01.coralinesystems.block.BrewingCauldronColors;
import net.zharok01.coralinesystems.block.CultureType;
import net.zharok01.coralinesystems.item.CoralineFluidUtils;
import net.zharok01.coralinesystems.registry.*;

import java.util.Map;

public final class BrewingCauldronInteractions {

    private BrewingCauldronInteractions() {
    }

    public static final Map<Item, CauldronInteraction> BREWING = CauldronInteraction.newInteractionMap();

    private static final CauldronInteraction REJECT = (state, level, pos, player, hand, itemStack) -> InteractionResult.PASS;

    public static void bootstrap() {
        BREWING.put(Items.WATER_BUCKET, REJECT);
        BREWING.put(Items.LAVA_BUCKET, REJECT);
        BREWING.put(Items.POWDER_SNOW_BUCKET, REJECT);

        BREWING.put(UAItems.MULBERRY.get(), addSolidInteraction(CultureType.WINE, UAItems.MULBERRY.get()));
        BREWING.put(CoralineItems.TEA_LEAVES.get(), addSolidInteraction(CultureType.KOMBUCHA, CoralineItems.TEA_LEAVES.get()));

        BREWING.put(Items.FERMENTED_SPIDER_EYE, fermentedSpiderEyeCultureInteraction());

        BREWING.put(Items.GLASS_BOTTLE, universalCollectBottleInteraction(SoundEvents.BOTTLE_FILL));

        CauldronInteraction.WATER.put(UAItems.MULBERRY.get(), convertToBrewingCauldron(CultureType.WINE, UAItems.MULBERRY.get()));
        CauldronInteraction.WATER.put(CoralineItems.TEA_LEAVES.get(), convertToBrewingCauldron(CultureType.KOMBUCHA, CoralineItems.TEA_LEAVES.get()));

        registerFillInteractions();
        registerTopUpInteractions();
    }

    private static CauldronInteraction fermentedSpiderEyeCultureInteraction() {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return InteractionResult.PASS;
            if (be.getCulture() != CultureType.NONE) return InteractionResult.PASS;

            CultureType implied = be.getImpliedCulture();
            if (implied != CultureType.WINE && implied != CultureType.KOMBUCHA) return InteractionResult.PASS;

            if (level instanceof ServerLevel serverLevel) {
                be.setCulture(implied);

                if (!player.getAbilities().instabuild) itemStack.shrink(1);
                player.awardStat(Stats.ITEM_USED.get(Items.FERMENTED_SPIDER_EYE));

                if (implied == CultureType.KOMBUCHA && player instanceof ServerPlayer serverPlayer) {
                    CoralineTriggers.KOMBUCHA_CULTURE.trigger(serverPlayer);
                }

                serverLevel.playSound(null, pos, CoralineSounds.CAULDRON_ADD_CULTURE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                CoralinePacketHandler.broadcastCauldronSplash(
                        pos, BrewingCauldronColors.getSplashColor(level.getBlockState(pos)), serverLevel);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    private static CauldronInteraction convertToBrewingCauldron(CultureType impliesCulture, Item solidItem) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!state.is(Blocks.WATER_CAULDRON)) return InteractionResult.PASS;

            if (level instanceof ServerLevel serverLevel) {
                BlockState brewingState = BrewingCauldronBlock.coraline$withDisplay(
                        CoralineBlocks.BREWING_CAULDRON.get().defaultBlockState()
                                .setValue(BrewingCauldronBlock.LEVEL, state.getValue(LayeredCauldronBlock.LEVEL)),
                        CultureType.NONE, impliesCulture,
                        BrewState.BREWING, BrewingCauldronBlockEntity.MIN_SOLID_STRENGTH);
                level.setBlockAndUpdate(pos, brewingState);

                if (level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be) {
                    be.initializeVisualsSilently(
                            CultureType.NONE, impliesCulture,
                            BrewingCauldronBlockEntity.MIN_SOLID_STRENGTH,
                            BrewState.BREWING, 0L);
                    be.syncToClient();
                }

                if (!player.getAbilities().instabuild) itemStack.shrink(1);
                player.awardStat(Stats.ITEM_USED.get(solidItem));

                if (solidItem == UAItems.MULBERRY.get() && player instanceof ServerPlayer serverPlayer) {
                    CoralineTriggers.MULBERRY_WATER_CAULDRON.trigger(serverPlayer);
                }

                serverLevel.playSound(null, pos, CoralineSounds.CAULDRON_ADD_SOLID.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                CoralinePacketHandler.broadcastCauldronSplash(pos, BrewingCauldronColors.getSplashColor(brewingState), serverLevel);
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

            if (level instanceof ServerLevel serverLevel) {
                be.initializeVisualsSilently(be.getCulture(), impliesCulture, currentStrength + 1,
                        be.getBrewState(), be.getBrewProgress());
                be.syncToClient();

                if (!player.getAbilities().instabuild) itemStack.shrink(1);
                player.awardStat(Stats.ITEM_USED.get(solidItem));

                serverLevel.playSound(null, pos, CoralineSounds.CAULDRON_ADD_SOLID.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                CoralinePacketHandler.broadcastCauldronSplash(pos, BrewingCauldronColors.getSplashColor(level.getBlockState(pos)), serverLevel);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

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

            if (level instanceof ServerLevel serverLevel) {
                int strength = be.getSolidStrength();

                int newWaterLevel = currentWaterLevel - 1;
                if (newWaterLevel <= 0) {
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                } else {
                    level.setBlockAndUpdate(pos, state.setValue(BrewingCauldronBlock.LEVEL, newWaterLevel));
                }

                ItemStack filledStack = new ItemStack(resultItem);
                if (appliesStrength) {
                    CoralineFluidUtils.setStrength(filledStack, strength);
                }

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, filledStack));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));

                serverLevel.playSound(null, pos, fillSound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                CoralinePacketHandler.broadcastCauldronSplash(pos, BrewingCauldronColors.getSplashColor(state), serverLevel);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    private record FillSpec(CultureType impliesCulture, boolean hasStrength,
                            CultureType resultCulture, BrewState resultState) {
    }

    private static void registerFillInteractions() {
        registerFillBottle(CoralineItems.MULBERRY_JUICE_BOTTLE.get(), new FillSpec(CultureType.WINE, true, null, BrewState.BREWING));
        registerFillBottle(CoralineItems.TEA_BOTTLE.get(), new FillSpec(CultureType.KOMBUCHA, true, null, BrewState.BREWING));
        registerFillBottle(CoralineItems.WINE_BOTTLE.get(), new FillSpec(CultureType.WINE, true, CultureType.WINE, BrewState.FINISHED));
        registerFillBottle(CoralineItems.KOMBUCHA_BOTTLE.get(), new FillSpec(CultureType.KOMBUCHA, false, CultureType.KOMBUCHA, BrewState.FINISHED));
        registerFillBottle(CoralineItems.DREGS_BOTTLE.get(), new FillSpec(CultureType.WINE, false, CultureType.WINE, BrewState.SPOILED));
    }

    private static void registerFillBottle(Item bottleItem, FillSpec spec) {
        CauldronInteraction.EMPTY.put(bottleItem, universalFillInteraction(spec, SoundEvents.BOTTLE_EMPTY));
    }

    private static CauldronInteraction universalFillInteraction(FillSpec spec, SoundEvent emptySound) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (level instanceof ServerLevel serverLevel) {
                Item poured = itemStack.getItem();
                int strength = spec.hasStrength() ? CoralineFluidUtils.getStrength(itemStack) : BrewingCauldronBlockEntity.MIN_SOLID_STRENGTH;
                int clampedStrength = Math.max(BrewingCauldronBlockEntity.MIN_SOLID_STRENGTH,
                        Math.min(BrewingCauldronBlockEntity.MAX_SOLID_STRENGTH, strength));

                CultureType finalCulture = spec.resultCulture() != null ? spec.resultCulture() : CultureType.NONE;
                BrewState finalState = spec.resultCulture() != null ? spec.resultState() : BrewState.BREWING;

                BlockState brewingState = BrewingCauldronBlock.coraline$withDisplay(
                        CoralineBlocks.BREWING_CAULDRON.get().defaultBlockState()
                                .setValue(BrewingCauldronBlock.LEVEL, 1),
                        finalCulture, spec.impliesCulture(), finalState, clampedStrength);
                level.setBlockAndUpdate(pos, brewingState);

                if (level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be) {
                    be.initializeVisualsSilently(finalCulture, spec.impliesCulture(), clampedStrength, finalState, 0L);
                    be.syncToClient();
                }

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.FILL_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(poured));

                serverLevel.playSound(null, pos, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                CoralinePacketHandler.broadcastCauldronSplash(pos, BrewingCauldronColors.getSplashColor(brewingState), serverLevel);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    private static void registerTopUpInteractions() {
        registerTopUpBottle(CoralineItems.MULBERRY_JUICE_BOTTLE.get(), new FillSpec(CultureType.WINE, true, null, BrewState.BREWING));
        registerTopUpBottle(CoralineItems.TEA_BOTTLE.get(), new FillSpec(CultureType.KOMBUCHA, true, null, BrewState.BREWING));
        registerTopUpBottle(CoralineItems.WINE_BOTTLE.get(), new FillSpec(CultureType.WINE, true, CultureType.WINE, BrewState.FINISHED));
        registerTopUpBottle(CoralineItems.KOMBUCHA_BOTTLE.get(), new FillSpec(CultureType.KOMBUCHA, false, CultureType.KOMBUCHA, BrewState.FINISHED));
        registerTopUpBottle(CoralineItems.DREGS_BOTTLE.get(), new FillSpec(CultureType.WINE, false, CultureType.WINE, BrewState.SPOILED));
    }

    private static void registerTopUpBottle(Item bottleItem, FillSpec spec) {
        BREWING.put(bottleItem, topUpInteraction(spec, SoundEvents.BOTTLE_EMPTY));
    }

    private static boolean matchesExistingContents(BrewingCauldronBlockEntity be, FillSpec spec) {
        if (spec.resultCulture() != null) {
            return be.getCulture() == spec.resultCulture() && be.getBrewState() == spec.resultState();
        } else {
            return be.getCulture() == CultureType.NONE && be.getImpliedCulture() == spec.impliesCulture();
        }
    }

    private static CauldronInteraction topUpInteraction(FillSpec spec, SoundEvent emptySound) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return InteractionResult.PASS;
            if (!matchesExistingContents(be, spec)) return InteractionResult.PASS;

            if (spec.hasStrength()) {
                if (CoralineFluidUtils.getStrength(itemStack) != be.getSolidStrength()) return InteractionResult.PASS;
            }

            int currentWaterLevel = state.getValue(BrewingCauldronBlock.LEVEL);
            if (currentWaterLevel >= 3) return InteractionResult.PASS;

            if (level instanceof ServerLevel serverLevel) {
                Item poured = itemStack.getItem();
                level.setBlockAndUpdate(pos, state.setValue(BrewingCauldronBlock.LEVEL, currentWaterLevel + 1));

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.FILL_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(poured));

                serverLevel.playSound(null, pos, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                CoralinePacketHandler.broadcastCauldronSplash(pos, BrewingCauldronColors.getSplashColor(state), serverLevel);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }
}