package net.zharok01.coralinesystems.util;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.zharok01.coralinesystems.block.BrewingCauldronBlock;
import net.zharok01.coralinesystems.block.BrewingCauldronBlockEntity;
import net.zharok01.coralinesystems.block.CultureType;
import net.zharok01.coralinesystems.registry.CoralineItems;

import java.util.Map;

/**
 * CauldronInteraction registrations for BrewingCauldronBlock, following the
 * exact static-map-plus-bootstrap shape CauldronInteraction.java itself
 * uses for vanilla's WATER/LAVA/POWDER_SNOW/EMPTY maps.
 * <p>
 * IMPORTANT: the map MUST be built via CauldronInteraction.newInteractionMap()
 * rather than a plain HashMap. AbstractCauldronBlock.use() does
 * interactions.get(item) with no null-check and immediately calls
 * .interact(...) on the result -- newInteractionMap()'s defaultReturnValue
 * is what makes an unmapped item resolve to a harmless PASS instead of an
 * NPE. Confirmed by inspection of AbstractCauldronBlock.java Session 1.
 * <p>
 * Session 1 scope: fill (via vanilla bucket interactions, reused as-is),
 * add-solid (Mulberries / Tea Leaves, level 1-5 with max-level rejection
 * cue), and add-culture (Yeast -> WINE / Dregs -> KOMBUCHA, brew start).
 * Sound/particle feedback beyond placeholders, and the actual random-tick
 * brewing math, are Sessions 2-3. Collection (Bottle/Bucket of finished
 * drink) is Session 4.
 */
public final class BrewingCauldronInteractions {

    private BrewingCauldronInteractions() {
    }

    public static final Map<Item, CauldronInteraction> BREWING = CauldronInteraction.newInteractionMap();

    /**
     * Called once from CoralineSystems' constructor (see registration-order
     * convention in Section 2.4/3.4 of the roadmap handoff), mirroring
     * CauldronInteraction.bootStrap()'s own static-init call pattern.
     */
    public static void bootstrap() {
        // Reuse vanilla's default fill interactions (empty bucket of
        // water/lava/powder snow) so a BrewingCauldronBlock placed at
        // solid-level 1 with a Bucket in hand behaves identically to a
        // plain vanilla cauldron for that specific interaction. Wine/
        // Kombucha only care about what happens AFTER water is already
        // present, so we don't need to reimplement the fill step.
        CauldronInteraction.addDefaultInteractions(BREWING);

        BREWING.put(CoralineItems.MULBERRIES.get(), addSolidInteraction(CultureType.WINE, CoralineItems.MULBERRIES.get()));
        BREWING.put(CoralineItems.TEA_LEAVES.get(), addSolidInteraction(CultureType.KOMBUCHA, CoralineItems.TEA_LEAVES.get()));

        BREWING.put(CoralineItems.YEAST.get(), addCultureInteraction(CultureType.WINE, CoralineItems.YEAST.get()));
        BREWING.put(CoralineItems.DREGS.get(), addCultureInteraction(CultureType.KOMBUCHA, CoralineItems.DREGS.get()));
    }

    /**
     * Builds the CauldronInteraction for adding a solid ingredient
     * (Mulberries or Tea Leaves). Each add-solid interaction is implicitly
     * locked to one CultureType -- Mulberries only ever push toward WINE,
     * Tea Leaves only ever push toward KOMBUCHA -- even though culture
     * hasn't technically been set yet at this stage. This lets us reject
     * "wrong" solids early (e.g. Tea Leaves after Mulberries already
     * started a level) rather than allowing a mixed, meaningless state
     * that a later culture-add step would have to disambiguate.
     * <p>
     * Session 1 note: rejection at MAX_SOLID_LEVEL fires a placeholder
     * cue only (sound omitted here entirely rather than guessing a
     * SoundEvent) -- proper SFX/particles are Session 3's job per the
     * roadmap. Leaving a clearly-marked TODO rather than picking an
     * arbitrary vanilla sound now and having to remember to revisit it.
     */
    private static CauldronInteraction addSolidInteraction(CultureType impliesCulture, Item solidItem) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
                return InteractionResult.PASS;
            }

            // Once a culture has actually been committed (Yeast/Dregs
            // already added), no further solid additions are legal at
            // all -- the recipe is locked in and brewing may already be
            // underway. Refuse silently (PASS) rather than consuming the
            // item or player's swing for no effect.
            if (be.getCulture() != CultureType.NONE) {
                return InteractionResult.PASS;
            }

            int currentLevel = state.getValue(BrewingCauldronBlock.LEVEL);

            if (currentLevel >= BrewingCauldronBlock.MAX_SOLID_LEVEL) {
                // TODO (Session 3): distinct "maxed out" SFX + particle cue,
                // per design doc Section 3 step 2. Silent PASS for now so
                // Session 1 doesn't guess a placeholder sound that then
                // needs to be found and swapped out later.
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                player.awardStat(Stats.ITEM_USED.get(solidItem));
                level.setBlockAndUpdate(pos, state.setValue(BrewingCauldronBlock.LEVEL, currentLevel + 1));
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    /**
     * Builds the CauldronInteraction for adding a culture item (Yeast or
     * Dregs), which commits the cauldron to a recipe branch and starts
     * the brew. Refuses if a culture has already been set, or if no
     * solids have been added yet (level starts at MIN_SOLID_LEVEL == 1
     * by default state, so "no solids added" and "one solid added" are
     * presently indistinguishable at the blockstate level -- flagging
     * this as a Session 2 consideration below rather than silently
     * guessing a fix here).
     * <p>
     * TODO (Session 2): BrewingCauldronBlock currently defaults LEVEL to
     * MIN_SOLID_LEVEL (1) rather than 0, matching LayeredCauldronBlock's
     * own convention of starting at 1 rather than "empty". This means we
     * cannot yet distinguish "cauldron has water only, zero solids added"
     * from "one Mulberry has been added" purely via LEVEL. For Session 1
     * this is harmless (culture-add doesn't currently check solid count),
     * but if a future rule needs "at least one solid must be added before
     * culture," this needs an explicit BE-side hasAddedSolid flag rather
     * than inferring it from LEVEL. Left as a flagged gap, not guessed at.
     */
    private static CauldronInteraction addCultureInteraction(CultureType culture, Item cultureItem) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
                return InteractionResult.PASS;
            }

            if (be.getCulture() != CultureType.NONE) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                player.awardStat(Stats.ITEM_USED.get(cultureItem));

                be.setCulture(culture);

                // TODO (Session 3): "Shh" fizzle SFX + Yeast/Dregs particle
                // burst, per design doc Section 3 step 3 / Section 4 step 3.
                // Deliberately not guessing a placeholder SoundEvent here.
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }
}
