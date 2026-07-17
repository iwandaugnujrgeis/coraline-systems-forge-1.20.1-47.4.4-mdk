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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.zharok01.coralinesystems.util.BrewingCauldronInteractions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The shared vessel for both Wine and Kombucha.
 * <p>
 * SESSION 2.5 ARCHITECTURE CHANGE ("Phantom Liquid Trap" fix, per Gemini's
 * diagnosis in gemini_additional_input_phantom_liquid_problem.md, confirmed
 * correct on review): {@code LEVEL} now tracks WATER VOLUME (1-3), reusing
 * vanilla's own {@code BlockStateProperties.LEVEL_CAULDRON} directly --
 * mirroring {@link net.minecraft.world.level.block.LayeredCauldronBlock}
 * exactly, so this block can point straight at vanilla's existing
 * water_cauldron_level1/2/3 models with zero custom model JSON for volume.
 * <p>
 * Solid-ingredient strength (previously this LEVEL, 1-5) has MOVED to
 * {@link BrewingCauldronBlockEntity#getSolidStrength()}. Rationale: strength
 * was never a physical-shape concern -- adding a 4th Mulberry never changed
 * the liquid plane's height, only (eventually) its tint. Blockstate should
 * hold shape-relevant data; BE should hold color/data-relevant data. This
 * also means Vanilla's own tintindex:0 content face, present on the
 * template_cauldron_full model, can be resolved by a BlockColor handler
 * that reads the BE directly -- no blockstate involvement needed for tint.
 * That handler itself is still Session 3 scope; this change only makes it
 * possible to build cleanly, it does not implement it.
 * <p>
 * Every previous reference to solid LEVEL via blockstate (isFull,
 * getAnalogOutputSignal, BrewingCauldronInteractions' add-solid/drain/fill
 * paths) has been migrated to read/write the BE's solidStrength instead --
 * see BrewingCauldronBlockEntity and BrewingCauldronInteractions for the
 * corresponding changes. isFull/getAnalogOutputSignal now report on WATER
 * volume, matching vanilla LayeredCauldronBlock's own semantics for these
 * two methods exactly (vanilla's isFull is level==3, its analog signal is
 * the LEVEL value) rather than solid strength, since both are blockstate-facing
 * methods and blockstate is volume now.
 */
public class BrewingCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public static final int MIN_SOLID_LEVEL = 1;
    public static final int MAX_SOLID_LEVEL = 5;

    /**
     * Water volume, 1-3. Reuses vanilla's LEVEL_CAULDRON property directly --
     * same property object LayeredCauldronBlock uses, so vanilla's own
     * water_cauldron_levelN models apply unmodified via blockstate JSON.
     */
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

    // ── Session 2: brewing progress tuning (unchanged) ──────────────────

    private static final int WINE_MAX_LIGHT = 6;
    private static final int KOMBUCHA_MIN_CORRECT_LIGHT = 7;
    private static final long WINE_TARGET_PROGRESS = 24_000L;
    private static final long KOMBUCHA_TARGET_PROGRESS = WINE_TARGET_PROGRESS / 2;
    private static final double EXPECTED_RANDOM_TICKS_PER_MC_DAY = 24_000.0 * 3.0 / 4096.0;
    private static final long WINE_PROGRESS_PER_TICK =
            Math.round(WINE_TARGET_PROGRESS / EXPECTED_RANDOM_TICKS_PER_MC_DAY);
    private static final long KOMBUCHA_PROGRESS_PER_TICK =
            Math.round(KOMBUCHA_TARGET_PROGRESS / EXPECTED_RANDOM_TICKS_PER_MC_DAY);
    private static final long[] KOMBUCHA_LIGHT_PUNISHMENT = {5000L, 5000L, 4000L, 3000L, 2000L, 1000L};

    public BrewingCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, BrewingCauldronInteractions.BREWING);
        // Default state: full water (3), matching how every conversion path
        // (convertToBrewingCauldron, universalFillInteraction) already only
        // ever creates this block from a FULL water source.
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 3));
    }

    // ── AbstractCauldronBlock requirements ─────────────────────────────────

    /**
     * Now reports on WATER volume (matches LayeredCauldronBlock.isFull
     * exactly: level == 3). Previously reported on solid strength == 5;
     * that check has no blockstate-level equivalent anymore since strength
     * moved to the BE -- callers that need "is strength maxed" must ask
     * the BE's solidStrength directly instead (BrewingCauldronInteractions
     * does this already for the add-solid path).
     */
    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 3;
    }

    /**
     * Now reports WATER volume, matching LayeredCauldronBlock's own
     * getAnalogOutputSignal (returns LEVEL directly). Comparators reading
     * this block now see "how full is it," not "how strong is the brew" --
     * consistent with vanilla cauldron comparator behavior.
     */
    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull net.minecraft.world.level.Level level, @NotNull BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    // ── EntityBlock wiring (unchanged) ──────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BrewingCauldronBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Session 2: random-tick brewing (unchanged -- reads BE fields only,
    // never touched solid LEVEL directly, so nothing here needs migration) ──

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
            return;
        }
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

    private void tickWine(BrewingCauldronBlockEntity be, ServerLevel level, BlockPos pos, int light) {
        if (light >= KOMBUCHA_MIN_CORRECT_LIGHT) {
            be.setBrewState(BrewState.SPOILED);
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

    private void tickKombucha(BrewingCauldronBlockEntity be, ServerLevel level, BlockPos pos, int light) {
        if (light <= 0) {
            return;
        }

        long gain;
        if (light >= KOMBUCHA_MIN_CORRECT_LIGHT) {
            gain = KOMBUCHA_PROGRESS_PER_TICK;
        } else {
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

    private static void fireFinishedCue(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                8, 0.25, 0.2, 0.25, 0.0);
    }

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