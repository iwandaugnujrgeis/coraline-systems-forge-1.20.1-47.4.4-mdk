package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces vanilla huge-mushroom-growth-on-bonemeal with a grass-bonemeal-style
 * scatter of the small mushroom block itself.
 *
 * Applies uniformly to brown_mushroom, red_mushroom, and Coraline's white_mushroom,
 * since all three share this single vanilla MushroomBlock class.
 *
 * isValidBonemealTarget / isBonemealSuccess are intentionally left untouched:
 * bonemeal is still usable on mushrooms and still succeeds at the vanilla 40% rate.
 * Only what happens ON success changes here.
 */
@Mixin(MushroomBlock.class)
public abstract class MushroomBlockMixin {

    /**
     * Redirects the growMushroom(...) call that vanilla's performBonemeal makes
     * on success, replacing the huge-mushroom-feature placement with a scatter
     * of more instances of the same small mushroom around the source position.
     *
     * This intentionally targets the growMushroom invocation rather than rewriting
     * performBonemeal wholesale via @Overwrite, so any future Forge/Mixin patch to
     * performBonemeal's surrounding logic (event firing, etc.) is preserved untouched.
     */
    @Redirect(
            method = "performBonemeal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/MushroomBlock;growMushroom(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Z"
            )
    )
    private boolean coraline$scatterInsteadOfGrow(
            MushroomBlock mushroomBlock, ServerLevel level, BlockPos pos, BlockState state, RandomSource random
    ) {
        coraline$scatterMushrooms(level, random, pos, state);
        // Original growMushroom's return value fed nothing back into performBonemeal
        // (performBonemeal discards it), so the redirected return value is likewise unused.
        return false;
    }

    /**
     * Grass-bonemeal-style scatter, adapted for mushrooms:
     * - 12 attempts (down from grass's 128), tuned to place 1-2 mushrooms
     *   in typical conditions. The canSurvive gate filters most attempts
     *   naturally in lit areas, keeping counts low even on ideal surfaces.
     * - Inner walk threshold scaled proportionally from grass's i/16 to i/2,
     *   preserving the same ~1/8 ratio of "no-walk" early iterations to
     *   "walking" later ones. Without this scaling, the inner loop would never
     *   fire within 12 iterations and every attempt would try pos itself
     *   (which always fails isEmptyBlock), producing zero placements.
     * - Every intermediate walk step is gated by the mushroom's own full
     *   canSurvive check (light level included), exactly mirroring how grass's
     *   walk gate requires each step to stand on grass-like ground.
     * - On reaching a landing spot, the same canSurvive check doubles as the
     *   final placement gate (no separate check needed, since the last walk
     *   step already passed canSurvive at that exact position).
     * - Lands only on air; places a duplicate of the source mushroom's BlockState.
     */

    @Unique private static void coraline$scatterMushrooms(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        label:
        for (int i = 0; i < 12; i++) {
            BlockPos walkPos = pos;

            for (int j = 0; j < i / 2; j++) {
                walkPos = walkPos.offset(
                        random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1
                );

                if (!state.canSurvive(level, walkPos)) {
                    continue label;
                }
            }

            if (level.isEmptyBlock(walkPos) && state.canSurvive(level, walkPos)) {
                level.setBlock(walkPos, state, 2);
            }
        }
    }
}