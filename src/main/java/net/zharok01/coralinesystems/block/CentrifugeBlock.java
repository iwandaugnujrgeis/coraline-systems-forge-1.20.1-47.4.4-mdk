package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CentrifugeBlock extends BaseEntityBlock {

    /** Drives the idle/activated blockstate and texture swap. */
    public static final BooleanProperty ACTIVATED =
            BooleanProperty.create("activated");

    public CentrifugeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        // Default state: not activated.
        registerDefaultState(stateDefinition.any().setValue(ACTIVATED, false));
    }

    // ── Blockstate ────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(ACTIVATED);
    }

    // ── BlockEntity wiring ────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CentrifugeBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Interaction ───────────────────────────────────────────────────────

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof CentrifugeBlockEntity centrifuge) {
            boolean activated = centrifuge.tryActivate(player, hand);
            return activated ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    // ── Client-side ambient effects (Furnace pattern) ─────────────────────

    /**
     * Called on the client only, roughly once per frame while the chunk is
     * loaded. We gate everything behind the ACTIVATED property so effects
     * only appear when the Centrifuge is running.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos,
                            RandomSource random) {
        if (!state.getValue(ACTIVATED)) {
            return;
        }

        double cx = pos.getX() + 0.5;
        double cy = pos.getY();
        double cz = pos.getZ() + 0.5;

        // Looping hum: ~10% chance per tick, same pattern as the Furnace crackle.
        // Swap SoundEvents.BEACON_AMBIENT for your custom SoundEvent later.
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(
                    cx, cy, cz,
                    SoundEvents.BEACON_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5f,   // volume — quiet background hum
                    1.0f,
                    false
            );
        }

        // Ambient particles: a gentle scatter of enchantment glints above the block.
        for (int i = 0; i < 2; i++) {
            double ox = (random.nextDouble() - 0.5) * 0.8;
            double oz = (random.nextDouble() - 0.5) * 0.8;
            double oy = random.nextDouble() * 0.4 + 0.5;

            // ENCHANT gives a nice "magical energy" feel; swap freely.
            level.addParticle(
                    ParticleTypes.ENCHANT,
                    cx + ox, cy + oy, cz + oz,
                    0.0, 0.05, 0.0   // slight upward drift
            );
        }

        // Occasional END_ROD particle for extra visual punch.
        if (random.nextDouble() < 0.3) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    cx + (random.nextDouble() - 0.5) * 0.6,
                    cy + random.nextDouble() * 0.6 + 0.8,
                    cz + (random.nextDouble() - 0.5) * 0.6,
                    0.0, 0.02, 0.0
            );
        }
    }
}