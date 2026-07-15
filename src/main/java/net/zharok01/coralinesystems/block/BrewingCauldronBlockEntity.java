package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import org.jetbrains.annotations.NotNull;

/**
 * Holds all state for an in-progress brew (Wine or Kombucha) beyond what
 * lives in the blockstate.
 * <p>
 * Session 1 scope: field skeleton + NBT persistence only. Per the roadmap
 * handoff (cauldron_brewing_roadmap_handoff.md, Section 2.3), the random-tick
 * progress-accumulation math is explicitly Session 2's job — there is no
 * in-house precedent for it in this codebase (Centrifuge uses an externally
 * -owned session model via TimeAccelerationManager, not per-block ticking,
 * which does NOT apply here). This BE owns its own progress directly instead,
 * following the CropBlock.randomTick / AGE-property style of state advance.
 * <p>
 * Solid-ingredient level (1-5) is intentionally NOT duplicated here — it
 * lives on the block as a BlockState IntegerProperty (BrewingCauldronBlock.LEVEL),
 * mirroring LayeredCauldronBlock.LEVEL directly, per the roadmap's stated
 * leaning. Everything that can't be a small fixed-range blockstate value
 * (culture choice, accumulated progress ticks, future light-tracking
 * bookkeeping) lives here on the BE instead.
 */
public class BrewingCauldronBlockEntity extends BlockEntity {

    private static final String TAG_CULTURE = "Culture";
    private static final String TAG_PROGRESS = "BrewProgress";

    /** Which recipe branch (if any) this cauldron is committed to. */
    private CultureType culture = CultureType.NONE;

    /**
     * Accumulated brew progress, in ticks. Advances via random tick once a
     * culture has been added (Session 2). Target thresholds (subject to
     * playtesting tuning per the design doc):
     *   - Wine: ~24,000 ticks (~1 Minecraft day), light 0-6 required.
     *   - Kombucha: 12,000 base ticks at light 7-15, plus graduated
     *     punishment ticks at light 1-6, indefinite stall at light 0.
     * Stored as a long so a very-delayed Kombucha brew (heavy punishment
     * stacking, or a batch left alone for a long time) can never overflow
     * an int the way repeated +5000 accumulation theoretically could on
     * extreme edge cases -- costs us nothing and removes a whole class of
     * long-running-batch bug up front.
     */
    private long brewProgress = 0L;

    public BrewingCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.BREWING_CAULDRON.get(), pos, state);
    }

    // ── Accessors ────────────────────────────────────────────────────────

    public CultureType getCulture() {
        return culture;
    }

    /**
     * Sets the culture for this brew and marks the BE dirty so the change
     * persists. Does not perform any validation (e.g. "culture already
     * set") -- that belongs to the CauldronInteraction that calls this,
     * which has the full context (item used, current level, etc.) needed
     * to decide whether the attempt is even legal in the first place.
     */
    public void setCulture(CultureType culture) {
        this.culture = culture;
        setChanged();
    }

    public long getBrewProgress() {
        return brewProgress;
    }

    public void setBrewProgress(long brewProgress) {
        this.brewProgress = brewProgress;
        setChanged();
    }

    /**
     * Convenience for Session 2's random-tick accumulator -- adds to
     * progress rather than requiring the caller to read-modify-write
     * manually every time.
     */
    public void addBrewProgress(long amount) {
        this.brewProgress += amount;
        setChanged();
    }

    /**
     * Resets this BE back to its pre-brew state (no culture, zero
     * progress). Used both by a successful collection (Session 4) and by
     * a Wine spoil transition (Session 2/3) -- in the spoil case, the
     * cauldron's contents become Dregs, which is a fresh, un-brewed item
     * sitting in the cauldron, not a continuation of the old brew's state.
     */
    public void reset() {
        this.culture = CultureType.NONE;
        this.brewProgress = 0L;
        setChanged();
    }

    // ── Persistence ──────────────────────────────────────────────────────

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_CULTURE)) {
            try {
                culture = CultureType.valueOf(tag.getString(TAG_CULTURE));
            } catch (IllegalArgumentException e) {
                // Unknown/corrupted culture name (e.g. from a future version
                // downgrade) -- fail safe to NONE rather than crash the
                // chunk load. A brew silently resetting is far preferable
                // to an unloadable world.
                culture = CultureType.NONE;
            }
        }
        if (tag.contains(TAG_PROGRESS)) {
            brewProgress = tag.getLong(TAG_PROGRESS);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_CULTURE, culture.name());
        tag.putLong(TAG_PROGRESS, brewProgress);
    }
}
