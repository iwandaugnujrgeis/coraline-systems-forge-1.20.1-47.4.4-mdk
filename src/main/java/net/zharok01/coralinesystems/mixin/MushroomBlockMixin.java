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

@Mixin(MushroomBlock.class)
public abstract class MushroomBlockMixin {

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
        return false;
    }

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