package net.zharok01.coralinesystems.util;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
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
 * Session 1 scope: add-solid (Mulberries / Tea Leaves, level 1-5 with
 * max-level rejection cue) and add-culture (Yeast -> WINE / Dregs ->
 * KOMBUCHA, brew start). Sound/particle feedback beyond placeholders, and
 * the actual random-tick brewing math, are Sessions 2-3. Wine/Kombucha
 * finished-drink collection (Bottle/Bucket of a completed brew) is Session
 * 4.
 * <p>
 * SESSION 1.7 ADDITION: Tea collection is implemented here, NOT deferred
 * to Session 4 -- per the confirmed Session 1.7 design decision (see
 * roadmap Section 1e), Tea Leaves + Water is itself the finished Tea, with
 * no culture item or brew-progress step, so its collection interaction
 * ({@link #teaCollectInteraction}) could be wired immediately rather than
 * waiting on Session 2's brew-progress machinery. This also supersedes the
 * original roadmap's "Session 5" (deferred Tea recipe) -- there is no
 * further Tea recipe work pending. See {@link #teaCollectInteraction}'s
 * javadoc for the full mechanics and gating rationale.
 * <p>
 * POST-SESSION-1 FIX: bucket-based fill interactions (WATER_BUCKET,
 * LAVA_BUCKET, POWDER_SNOW_BUCKET) are explicitly registered as REJECT in
 * this map rather than reused from vanilla via addDefaultInteractions --
 * see bootstrap()'s inline comment and REJECT's javadoc below for why
 * (bug #1: a Water Bucket would otherwise silently revert a
 * BrewingCauldronBlock back to a plain vanilla water cauldron, destroying
 * all BE state). A Glass Bottle of Water was never mapped in BREWING to
 * begin with (WATER.put(Items.GLASS_BOTTLE, ...) is vanilla's own
 * WATER_CAULDRON-only entry and was never copied into BREWING), so it
 * already correctly falls through to PASS via the map's default return
 * value -- no separate fix needed for that specific item.
 * <p>
 * CONVERSION ENTRY POINT (added post-Session-1, fixing a Session 1 gap):
 * a player always starts from a plain vanilla WATER_CAULDRON (filled via
 * vanilla's own bucket/bottle interactions) -- BrewingCauldronBlock is
 * never placed directly. That means the very first Mulberry/Tea Leaves
 * right-click actually dispatches through {@code CauldronInteraction.WATER}
 * (vanilla's own map, see CauldronInteraction.java), NOT our BREWING map,
 * since AbstractCauldronBlock.use() reads whichever interactions map the
 * block instance was constructed with. Our BREWING map is unreachable
 * until a BrewingCauldronBlock already exists in the world -- nothing
 * previously created one, so solids silently no-op'd via WATER's default
 * PASS return.
 * <p>
 * Fix: register Mulberries/Tea Leaves into {@code CauldronInteraction.WATER}
 * too (mirroring exactly how vanilla's own DYED_ITEM/BANNER/SHULKER_BOX
 * entries sit in that same map -- see CauldronInteraction.bootStrap()).
 * This entry gates on full water (LEVEL 3, same predicate vanilla's own
 * Bucket-fill interaction uses), swaps the block from WATER_CAULDRON to
 * BrewingCauldronBlock, and performs the level-1 increment inline (it
 * cannot simply re-delegate to addSolidInteraction's lambda, because that
 * lambda expects a BrewingCauldronBlockEntity to already exist at pos --
 * which it won't, until this exact conversion creates one). Every
 * subsequent Mulberry/Tea Leaves addition goes through the normal BREWING
 * map afterward, since the block is BrewingCauldronBlock from that point
 * on.
 */
public final class BrewingCauldronInteractions {

    private BrewingCauldronInteractions() {
    }

    public static final Map<Item, CauldronInteraction> BREWING = CauldronInteraction.newInteractionMap();

    /**
     * Shared no-op interaction that always returns PASS. Used both as an
     * explicit map entry (see the WATER_BUCKET/LAVA_BUCKET/POWDER_SNOW_BUCKET
     * registrations below) and as the return value of every "wrong branch"
     * rejection inside addSolidInteraction/addCultureInteraction.
     * <p>
     * IMPORTANT (bug #4 fix): PASS is not just "no effect" -- it's what
     * suppresses the player's arm-swing animation. AbstractCauldronBlock.use()
     * returns whatever this interact() call returns directly, and
     * InteractionResult.PASS reads to the client as "nothing happened here,
     * let vanilla continue its own logic" (no swing). InteractionResult
     * .sidedSuccess(...) -- used by every ACCEPTED interaction below -- is
     * what triggers the swing. The MAX_SOLID_LEVEL rejection already
     * returned PASS correctly; the fix for Dregs-on-Mulberries /
     * Yeast-on-Tea-Leaves (and the reverse) is simply routing those same
     * wrong-branch cases through PASS instead of falling into the
     * sidedSuccess branch, which is what CultureType-branch gating below
     * now guarantees.
     */
    private static final CauldronInteraction REJECT = (state, level, pos, player, hand, itemStack) -> InteractionResult.PASS;

    /**
     * Called once from CoralineSystems' constructor (see registration-order
     * convention in Section 2.4/3.4 of the roadmap handoff), mirroring
     * CauldronInteraction.bootStrap()'s own static-init call pattern.
     */
    public static void bootstrap() {
        // NOTE: we deliberately do NOT call
        // CauldronInteraction.addDefaultInteractions(BREWING) here anymore.
        // That call seeds Items.WATER_BUCKET -> FILL_WATER (and LAVA_BUCKET/
        // POWDER_SNOW_BUCKET), and FILL_WATER's vanilla implementation
        // unconditionally emptyBucket()'s a hardcoded
        // Blocks.WATER_CAULDRON.defaultBlockState() over whatever block is
        // here -- silently reverting a BrewingCauldronBlock back to a plain
        // vanilla water cauldron and destroying the BE (and all solid/
        // culture/progress state with it) in the process. A brewing
        // cauldron must never be re-fillable via vanilla fluid interactions
        // once conversion has happened, full stop, regardless of solid/
        // culture state -- see class javadoc bug #1.
        //
        // Explicitly registering these three as REJECT (below) rather than
        // just leaving them unmapped is intentional, not just defensive:
        // it documents that this is a deliberate design decision (not an
        // oversight to "fix later"), and it protects us if a future session
        // ever DOES call addDefaultInteractions(BREWING) again by habit --
        // an explicit REJECT entry always wins over one added earlier in
        // the same put() sequence, whereas an absent entry would silently
        // start working (i.e. silently start reverting the cauldron again)
        // the moment addDefaultInteractions was reintroduced.
        BREWING.put(net.minecraft.world.item.Items.WATER_BUCKET, REJECT);
        BREWING.put(net.minecraft.world.item.Items.LAVA_BUCKET, REJECT);
        BREWING.put(net.minecraft.world.item.Items.POWDER_SNOW_BUCKET, REJECT);

        BREWING.put(CoralineItems.MULBERRIES.get(), addSolidInteraction(CultureType.WINE, CoralineItems.MULBERRIES.get()));
        BREWING.put(CoralineItems.TEA_LEAVES.get(), addSolidInteraction(CultureType.KOMBUCHA, CoralineItems.TEA_LEAVES.get()));

        BREWING.put(CoralineItems.YEAST.get(), addCultureInteraction(CultureType.WINE, CoralineItems.YEAST.get()));
        BREWING.put(CoralineItems.DREGS.get(), addCultureInteraction(CultureType.KOMBUCHA, CoralineItems.DREGS.get()));

        // Session 1.7: Tea collection. Tea Leaves + Water IS finished Tea --
        // no culture item, no brew-progress wait (see class-level Session
        // 1.7 note below and roadmap Section 1e). Gated inside
        // teaCollectInteraction() to only fire on the Tea-Leaves branch
        // (impliedCulture == KOMBUCHA) with no culture committed yet
        // (culture == NONE) -- see that method's javadoc for why the
        // second check matters.
        BREWING.put(net.minecraft.world.item.Items.GLASS_BOTTLE,
                teaCollectInteraction(CoralineItems.TEA_BOTTLE.get(), net.minecraft.world.item.Items.GLASS_BOTTLE, net.minecraft.sounds.SoundEvents.BOTTLE_FILL));
        BREWING.put(net.minecraft.world.item.Items.BUCKET,
                teaCollectInteraction(CoralineItems.TEA_BUCKET.get(), net.minecraft.world.item.Items.BUCKET, net.minecraft.sounds.SoundEvents.BUCKET_FILL));

        // Conversion entry point -- see class javadoc "CONVERSION ENTRY
        // POINT" section above for why this is required. Registered into
        // vanilla's own WATER map (not BREWING), since that's what a
        // plain WATER_CAULDRON actually dispatches through. Safe to
        // mutate here: CauldronInteraction.WATER is a plain static Map
        // populated by CauldronInteraction.bootStrap() at game bootstrap
        // (well before FMLCommonSetupEvent, when this bootstrap() runs),
        // and vanilla itself populates it the same way (WATER.put(...)
        // calls in CauldronInteraction.bootStrap()) -- we're not
        // replacing or racing anything, just adding two more entries
        // after vanilla's own are already in place.
        CauldronInteraction.WATER.put(CoralineItems.MULBERRIES.get(), convertToBrewingCauldron(CultureType.WINE, CoralineItems.MULBERRIES.get()));
        CauldronInteraction.WATER.put(CoralineItems.TEA_LEAVES.get(), convertToBrewingCauldron(CultureType.KOMBUCHA, CoralineItems.TEA_LEAVES.get()));
    }

    /**
     * Builds the one-shot conversion interaction that turns a full vanilla
     * WATER_CAULDRON into a BrewingCauldronBlock at solid-level
     * MIN_SOLID_LEVEL, consuming the triggering Mulberry/Tea Leaves item
     * as that first solid addition in the same interaction. Registered
     * into {@code CauldronInteraction.WATER}, NOT {@code BREWING} -- see
     * class javadoc.
     * <p>
     * Gate mirrors vanilla's own full-cauldron predicate (the same
     * {@code LEVEL == 3} check WATER's Bucket-fill entry uses) rather
     * than allowing partial fills, per the confirmed design decision to
     * treat "full" the same way vanilla treats "enough water to matter."
     * <p>
     * impliesCulture locks this cauldron's solid-ingredient branch the
     * moment it's created -- this IS the first solid addition, so it's
     * exactly the right place to set impliedCulture, mirroring what
     * addSolidInteraction now does for every subsequent addition. Fixes
     * bugs #2/#3: without this, a freshly-converted cauldron had no record
     * of which solid started it, so nothing could reject a mismatched
     * second solid or a mismatched culture item later.
     */
    private static CauldronInteraction convertToBrewingCauldron(CultureType impliesCulture, Item solidItem) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!state.is(Blocks.WATER_CAULDRON)) {
                return InteractionResult.PASS;
            }

            if (state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                player.awardStat(Stats.ITEM_USED.get(solidItem));

                // BrewingCauldronBlock.registerDefaultState() already sets
                // LEVEL to MIN_SOLID_LEVEL (1) -- confirmed in
                // BrewingCauldronBlock.java's constructor -- so this
                // single setBlockAndUpdate both performs the block swap
                // AND represents the level-1 solid addition in one step;
                // no separate .setValue(LEVEL, ...) call is needed here.
                level.setBlockAndUpdate(pos, net.zharok01.coralinesystems.registry.CoralineBlocks.BREWING_CAULDRON.get().defaultBlockState());

                // setBlockAndUpdate above causes EntityBlock.newBlockEntity()
                // to run, creating a fresh BrewingCauldronBlockEntity at pos.
                // We DO need to touch it now (unlike the old Session 1
                // comment claimed) to record which branch this cauldron just
                // committed to -- culture itself correctly stays NONE (no
                // Yeast/Dregs has been added), but impliedCulture must be
                // set here or the very first solid addition would be
                // unrecorded and bugs #2/#3 would remain exploitable via
                // the conversion path specifically.
                if (level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be) {
                    be.setImpliedCulture(impliesCulture);
                }

                // TODO (Session 3): same "first solid added" SFX/particle
                // cue addSolidInteraction will eventually get -- left
                // silent for now rather than guessing a placeholder.
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        };
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

            // Bug #2 fix: reject a solid that doesn't match the branch this
            // cauldron already committed to. impliedCulture is always
            // non-NONE by this point in practice (the conversion entry
            // point sets it the instant the block becomes a
            // BrewingCauldronBlock -- there's no code path where a
            // BrewingCauldronBlockEntity exists with impliedCulture still
            // NONE), but we check defensively rather than assume: if it
            // somehow is NONE, we set it here instead of rejecting, so a
            // legitimate first addition is never accidentally blocked.
            CultureType existingBranch = be.getImpliedCulture();
            if (existingBranch != CultureType.NONE && existingBranch != impliesCulture) {
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
                if (existingBranch == CultureType.NONE) {
                    be.setImpliedCulture(impliesCulture);
                }
                level.setBlockAndUpdate(pos, state.setValue(BrewingCauldronBlock.LEVEL, currentLevel + 1));
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    /**
     * Builds the CauldronInteraction for adding a culture item (Yeast or
     * Dregs), which commits the cauldron to a recipe branch and starts
     * the brew. Refuses if a culture has already been set, or if this
     * culture item doesn't match the solid-ingredient branch already
     * committed to (Yeast requires impliedCulture == WINE, i.e. a
     * Mulberries cauldron; Dregs requires impliedCulture == KOMBUCHA, i.e.
     * a Tea Leaves cauldron) -- fixes bug #3.
     * <p>
     * The old Session 1 TODO here about "no solids added yet" being
     * indistinguishable from "one solid added" (via LEVEL alone) is now
     * moot: a BrewingCauldronBlockEntity is only ever created by the
     * conversion entry point, which sets impliedCulture in the very same
     * setBlockAndUpdate call that creates the BE (see
     * convertToBrewingCauldron) -- there is no reachable state where a
     * BrewingCauldronBlockEntity exists with impliedCulture still NONE.
     * "At least one solid must already be present before culture can be
     * added" is therefore guaranteed structurally, with no separate flag
     * needed.
     */
    private static CauldronInteraction addCultureInteraction(CultureType culture, Item cultureItem) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
                return InteractionResult.PASS;
            }

            if (be.getCulture() != CultureType.NONE) {
                return InteractionResult.PASS;
            }

            // Bug #3 fix: Yeast is only valid on a Mulberry (WINE) branch,
            // Dregs only valid on a Tea Leaves (KOMBUCHA) branch. Falling
            // through to PASS here is also what fixes bug #4 for this
            // interaction -- PASS is what suppresses the arm swing (see
            // REJECT's javadoc above); previously this branch mismatch was
            // never checked at all, so execution always reached
            // sidedSuccess regardless of which solid was actually present.
            if (be.getImpliedCulture() != culture) {
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

    /**
     * Session 1.7. Builds the CauldronInteraction for collecting Tea via an
     * empty Glass Bottle or Bucket. Per the confirmed Session 1.7 design
     * decision (roadmap Section 1e), Tea Leaves + Water is itself the
     * finished product -- no culture item, no brew-progress wait. This
     * supersedes the original roadmap's "Session 5" plan for a separate,
     * more involved Tea recipe; there is no further Tea recipe design work
     * pending.
     * <p>
     * Gating (see roadmap Section 1e.1 for the full rationale): fires only
     * when {@code impliedCulture == KOMBUCHA} (this cauldron's committed
     * solid branch is Tea Leaves, not Mulberries) AND {@code culture ==
     * NONE} (Dregs has not been added -- no real Kombucha brew is
     * underway). The second check is required because impliedCulture does
     * NOT change when Dregs converts a cauldron into an actual Kombucha
     * brew (it is set once at first-solid and only cleared by reset()) --
     * without checking culture too, a player could keep draining free Tea
     * out of a cauldron that's mid-Kombucha-ferment.
     * <p>
     * Mechanics: stamps the resulting container with
     * CoralineFluidUtils.setStrength(stack, currentLevel) using the
     * cauldron's LEVEL at the moment of collection, then decrements LEVEL
     * by 1 -- mirroring LayeredCauldronBlock.lowerFillLevel's progressive-
     * drain pattern rather than resetting the whole cauldron in one pull.
     * BrewingCauldronBlock.LEVEL's declared range is 1-5 with no valid 0
     * state, so collecting from LEVEL == MIN_SOLID_LEVEL (1) instead
     * reverts the block fully to a full vanilla Blocks.WATER_CAULDRON
     * (LEVEL 3) -- exactly mirroring how lowerFillLevel reverts
     * Blocks.CAULDRON (empty) at underflow. This destroys the BE (standard
     * vanilla behavior on a block swap away from an EntityBlock), which
     * implicitly clears impliedCulture/culture/brewProgress since there's
     * nothing left to explicitly reset.
     * <p>
     * Sound reuse: SoundEvents.BOTTLE_FILL/BUCKET_FILL, the same sounds
     * vanilla's own WATER map uses for plain water -- filling a container
     * from a cauldron is conceptually identical regardless of contents, so
     * this is a permanent choice, not a Session-3 placeholder.
     *
     * @param resultItem the Tea Bottle or Tea Bucket item to hand back
     * @param emptyItem  the triggering empty container (Items.GLASS_BOTTLE
     *                   or Items.BUCKET), used both to build the fallback
     *                   "no room in inventory" empty stack via
     *                   ItemUtils.createFilledResult and for the
     *                   ITEM_USED stat
     * @param fillSound  the vanilla fill sound to reuse (BOTTLE_FILL or
     *                   BUCKET_FILL)
     */
    private static CauldronInteraction teaCollectInteraction(Item resultItem, Item emptyItem, net.minecraft.sounds.SoundEvent fillSound) {
        return (state, level, pos, player, hand, itemStack) -> {
            if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
                return InteractionResult.PASS;
            }

            if (be.getImpliedCulture() != CultureType.KOMBUCHA) {
                return InteractionResult.PASS;
            }

            if (be.getCulture() != CultureType.NONE) {
                return InteractionResult.PASS;
            }

            int currentLevel = state.getValue(BrewingCauldronBlock.LEVEL);

            if (!level.isClientSide) {
                net.minecraft.world.item.ItemStack filledStack = net.zharok01.coralinesystems.item.CoralineFluidUtils.setStrength(
                        new net.minecraft.world.item.ItemStack(resultItem), currentLevel);

                player.setItemInHand(hand, net.minecraft.world.item.ItemUtils.createFilledResult(itemStack, player, filledStack));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(emptyItem));

                if (currentLevel <= BrewingCauldronBlock.MIN_SOLID_LEVEL) {
                    // Underflow -- mirror LayeredCauldronBlock.lowerFillLevel's
                    // revert-to-base-block pattern. Destroys the BE, which
                    // implicitly clears impliedCulture/culture/brewProgress.
                    level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
                } else {
                    level.setBlockAndUpdate(pos, state.setValue(BrewingCauldronBlock.LEVEL, currentLevel - 1));
                }

                level.playSound(null, pos, fillSound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, net.minecraft.world.level.gameevent.GameEvent.FLUID_PICKUP, pos);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }
}