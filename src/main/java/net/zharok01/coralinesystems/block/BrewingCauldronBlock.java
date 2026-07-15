package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
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
 * block/BE instance from "water added" through collection -- see the
 * Session 1 planning discussion in cauldron_brewing_roadmap_handoff.md:
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
 * Everything else (culture, brew progress) lives on
 * {@link BrewingCauldronBlockEntity}.
 * <p>
 * Session 1 scope: registration skeleton, blockstate/BE wiring, and
 * CauldronInteraction dispatch hookup only. Random-tick brewing logic,
 * sounds/particles, and collection interactions are Sessions 2-4.
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
    // null) -- Session 2 drives progress via randomTick, not a
    // BlockEntityTicker, per the roadmap's confirmed direction. Revisit
    // only if a purely-cosmetic client-side ticker becomes useful later
    // (e.g. smoothing bubble animation between random ticks).
}
