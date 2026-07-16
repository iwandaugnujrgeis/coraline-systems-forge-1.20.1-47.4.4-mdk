package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.zharok01.coralinesystems.util.BrewingCauldronInteractions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The shared vessel for both Wine and Kombucha. Deliberately stays ONE
 * block/BE instance from "water added" through collection — see the
 * Session 1 planning discussion in cauldron_brewing_coding_roadmap_handoff.md:
 * mid-brew block-swapping (the vanilla CauldronBlock -> WATER_CAULDRON
 * pattern) was considered and rejected as unnecessarily fragile for a
 * system that needs to carry continuous BE state (progress, culture)
 * across the whole brew. Amendments' ModCauldronBlock follows the same
 * extend-AbstractCauldronBlock-and-implement-EntityBlock shape and only
 * ever swaps back to a plain vanilla cauldron on a genuinely empty state,
 * which is the same approach taken here.
 * <p>
 * LEVEL mirrors LayeredCauldronBlock.LEVEL directly (1-5 instead of 1-3)
 * -- tracks solid-ingredient strength (Mulberries or Tea Leaves count).
 * Everything else (culture, brew progress, brew state) lives on
 * {@link BrewingCauldronBlockEntity}.
 * <p>
 * Session 1 scope: registration skeleton, blockstate/BE wiring, and
 * CauldronInteraction dispatch hookup only.
 * <p>
 * Session 2 scope (this class's current state): random-tick brew
 * progress, light-level checks, and the Wine-spoil / Kombucha-
 * stall-or-finish transitions, plus temporary placeholder sounds/
 * particles so the tick logic is observable while testing. Progress
 * accrues in chunks added by {@link #randomTick} rather than tracking
 * elapsed game time directly, matching the "very performance friendly"
 * random-tick accumulated-progress model — actual real-world duration
 * will vary tick-to-tick (that's intentional, not a bug) but is tuned to
 * average out to roughly the design doc's target durations (~1 MC day
 * for Wine, ~half a day for Kombucha at correct light) given vanilla's
 * default random tick rate of 3 attempts per 16x16x16 subchunk per game
 * tick.
 * <p>
 * All sound/particle calls in this class are explicitly PLACEHOLDER —
 * temporary vanilla-asset stand-ins for testing Session 2's logic, not
 * final asset work. Real SFX/particle polish (custom sounds, per-Solid-
 * Level color tinting, an actual ambient sound loop) is Session 3 scope
 * per the roadmap; every placeholder call site below is isolated enough
 * to swap for a real CoralineSounds/particle registration later without
 * touching the surrounding logic.
 */
public class BrewingCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public static final int MIN_SOLID_LEVEL = 1;
    public static final int MAX_SOLID_LEVEL = 5;

    /**
     * Solid-ingredient level, 1-5. Reuses vanilla's LEVEL_CAULDRON property
     * rather than declaring a new one -- LEVEL_CAULDRON's declared range is
     * 1-3, which is too narrow for our 1-5 requirement, so we cannot reuse
     * BlockStateProperties.LEVEL_CAULDRON as-is. Declaring our own ranged
     * IntegerProperty instead.
     */
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", MIN_SOLID_LEVEL, MAX_SOLID_LEVEL);

    // ── Session 2: brewing progress tuning ──────────────────────────────

    /** Wine: darkness required, light 0-6. Light >= 7 spoils the batch. */
    private static final int WINE_MAX_LIGHT = 6;

    /** Kombucha: correct light is 7-15 (base rate, no punishment). */
    private static final int KOMBUCHA_MIN_CORRECT_LIGHT = 7;

    /**
     * Target total progress to finish Wine at correct light, roughly
     * "~1 Minecraft day" per the design doc. Not a hard tick count --
     * see class javadoc.
     */
    private static final long WINE_TARGET_PROGRESS = 24_000L;

    /**
     * Target total progress to finish Kombucha at correct light (7-15),
     * roughly "~half a Minecraft day" per the design doc -- exactly half
     * of Wine's target, per direct instruction, so the two stay in a
     * fixed 2:1 ratio even if WINE_TARGET_PROGRESS is retuned later.
     */
    private static final long KOMBUCHA_TARGET_PROGRESS = WINE_TARGET_PROGRESS / 2;

    /**
     * Expected number of random ticks a single block receives over one
     * Minecraft day (24,000 game ticks) at vanilla's default random tick
     * speed (3 attempts per 16x16x16 subchunk per game tick -> a 3/4096
     * chance per game tick of a specific block being hit). Used only to
     * derive the PROGRESS_PER_TICK constants below -- not consulted at
     * runtime, so a change to the server's actual randomTickSpeed just
     * shifts real-world duration without needing a code change.
     */
    private static final double EXPECTED_RANDOM_TICKS_PER_MC_DAY = 24_000.0 * 3.0 / 4096.0;

    /** Progress added per random tick at correct light, for Wine. */
    private static final long WINE_PROGRESS_PER_TICK =
            Math.round(WINE_TARGET_PROGRESS / EXPECTED_RANDOM_TICKS_PER_MC_DAY);

    /** Progress added per random tick at correct light, for Kombucha. */
    private static final long KOMBUCHA_PROGRESS_PER_TICK =
            Math.round(KOMBUCHA_TARGET_PROGRESS / EXPECTED_RANDOM_TICKS_PER_MC_DAY);

    /**
     * Kombucha's graduated punishment table for light 1-6, per the design
     * doc -- extra progress WITHHELD per random tick hit at that light
     * level (index 0 = light 1, index 5 = light 6). Confirmed direction:
     * closer to correct light (7) = less punishment, so light 1 is
     * harshest (+5000 withheld) and light 6 is mildest (+1000 withheld).
     * Subtracted from the per-tick gain, floored at 0 so a single
     * unlucky low-light tick never goes net-negative.
     */
    private static final long[] KOMBUCHA_LIGHT_PUNISHMENT = {5000L, 5000L, 4000L, 3000L, 2000L, 1000L};

    public BrewingCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, BrewingCauldronInteractions.BREWING);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, MIN_SOLID_LEVEL));
    }

    // ── AbstractCauldronBlock requirements ─────────────────────────────────

    /**
     * "Full" for a vanilla cauldron gates things like stalactite drips
     * refusing to add more. Our solid level caps at MAX_SOLID_LEVEL the
     * same way LayeredCauldronBlock caps at 3 -- reuse that same shape.
     */
    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == MAX_SOLID_LEVEL;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull net.minecraft.world.level.Level level, @NotNull BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    // ── EntityBlock wiring ──────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BrewingCauldronBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    // getTicker() is intentionally left at EntityBlock's default (returns
    // null) -- Session 2 confirmed progress is driven by randomTick, not a
    // BlockEntityTicker. Revisit only if a purely-cosmetic client-side
    // ticker becomes useful later (e.g. smoothing bubble animation).

    // ── Session 2: random-tick brewing ──────────────────────────────────────

    /**
     * Random-ticks unconditionally; the real culture/brewState gate lives
     * in {@link #randomTick} below since isRandomlyTicking only receives
     * BlockState, not the BlockEntity, so it can't see culture/brewState
     * itself. This matches vanilla's own tradeoff for BE-gated random
     * ticks (e.g. CampfireBlock) -- randomTick is cheap to call and
     * immediately no-ops when not actively brewing, but every
     * BrewingCauldronBlock in the world (including idle NONE-culture or
     * already-resolved FINISHED/SPOILED ones) still gets scheduled.
     */
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
            return;
        }

        // Not yet brewing (no culture set) or already resolved -- nothing
        // to do. This is the real gate isRandomlyTicking couldn't express.
        if (be.getCulture() == CultureType.NONE || be.getBrewState() != BrewState.BREWING) {
            return;
        }

        int light = level.getRawBrightness(pos, 0);

        if (be.getCulture() == CultureType.WINE) {
            tickWine(be, level, pos, light);
        } else if (be.getCulture() == CultureType.KOMBUCHA) {
            tickKombucha(be, level, pos, light);
        }
    }

    /**
     * Wine: darkness (light 0-6) required. Light >= 7 is an immediate
     * hard failure -- the batch spoils into Dregs, no partial credit, no
     * second chances. Per the design doc, the Wine itself never comes
     * into existence on this path; the LEVEL/strength the Player set is
     * simply discarded (Dregs carries no strength).
     */
    private void tickWine(BrewingCauldronBlockEntity be, ServerLevel level, BlockPos pos, int light) {
        if (light >= KOMBUCHA_MIN_CORRECT_LIGHT) { // light >= 7, i.e. > WINE_MAX_LIGHT (6)
            be.setBrewState(BrewState.SPOILED);

            // PLACEHOLDER (Session 2, temp for testing). Design doc
            // Section 3: "Triggers a distinct 'something has gone wrong'
            // SFX + particle effect." Reusing vanilla's fire-extinguish
            // hiss and smoke for now -- genuinely distinct spoil audio
            // and particles are Session 3.
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.7F);
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10, 0.3, 0.2, 0.3, 0.02);
            return;
        }

        be.addBrewProgress(WINE_PROGRESS_PER_TICK);
        if (be.getBrewProgress() >= WINE_TARGET_PROGRESS) {
            be.setBrewState(BrewState.FINISHED);
            fireFinishedCue(level, pos);
        }
    }

    /**
     * Kombucha: light 7-15 brews at the base rate. Light 1-6 brews slower,
     * per the graduated punishment table. Light 0 stalls indefinitely --
     * zero progress, zero feedback, matching the design doc's explicit
     * "no penalty, no waste, just stalls" framing. Kombucha never spoils.
     */
    private void tickKombucha(BrewingCauldronBlockEntity be, ServerLevel level, BlockPos pos, int light) {
        if (light <= 0) {
            return; // indefinite stall, exactly as designed
        }

        long gain;
        if (light >= KOMBUCHA_MIN_CORRECT_LIGHT) {
            gain = KOMBUCHA_PROGRESS_PER_TICK;
        } else {
            // light is 1-6 here
            long punishment = KOMBUCHA_LIGHT_PUNISHMENT[light - 1];
            gain = Math.max(0L, KOMBUCHA_PROGRESS_PER_TICK - punishment);
        }

        if (gain <= 0L) {
            return;
        }

        be.addBrewProgress(gain);
        if (be.getBrewProgress() >= KOMBUCHA_TARGET_PROGRESS) {
            be.setBrewState(BrewState.FINISHED);
            fireFinishedCue(level, pos);
        }
    }

    /**
     * PLACEHOLDER (Session 2, temp for testing). Not explicitly specified
     * by the design doc (only the spoil case names a required SFX/particle
     * combo) but useful to distinguish "just finished" from "still
     * brewing" while testing -- shared by both Wine and Kombucha success
     * since the doc doesn't call for differentiated success cues. Session
     * 3 may split these per-drink or drop this entirely if it's redundant
     * with the blockstate/tint changes that session is expected to add.
     */
    private static void fireFinishedCue(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                8, 0.25, 0.2, 0.25, 0.0);
    }

    // ── Session 2: ambient cosmetic feedback ──────────────────────────────

    /**
     * PLACEHOLDER (Session 2, temp for testing). Client-side cosmetic
     * ambient bubbling while actively BREWING -- design doc Section 3:
     * "Cauldron bubbles continuously... with a crackling/fizzing ambient
     * sound loop." Real per-Solid-Level color tinting and a proper sound
     * loop are explicitly Session 3 scope (see roadmap Section 2.4:
     * "animateTick... is the right hook for ambient bubbling/fizzing once
     * that's built (Session 3)") -- this is only enough to visually/audibly
     * confirm "yes, this cauldron is actively brewing" while testing
     * Session 2's tick logic. Called automatically by vanilla's client-
     * side block-tick-effects loop; no manual wiring needed.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
            return;
        }
        if (be.getCulture() == CultureType.NONE || be.getBrewState() != BrewState.BREWING) {
            return;
        }

        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.2 + random.nextDouble() * 0.6,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                    0.0, 0.05, 0.0);
        }
        if (random.nextInt(20) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                    0.3F, 0.6F + random.nextFloat() * 0.2F, false);
        }
    }
}