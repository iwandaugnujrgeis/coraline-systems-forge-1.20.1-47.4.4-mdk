package net.zharok01.coralinesystems.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.zharok01.coralinesystems.content.block.LimitedLightBlock;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StructurePiece.class)
public class StructurePieceMixin {

    @WrapOperation(
            method = "placeBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    )
    private boolean coraline$makeAllStructureTorchesBurned(WorldGenLevel level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {

        if (state.is(Blocks.WALL_TORCH)) {
            state = CoralineBlocks.WALL_TORCH.get().defaultBlockState()
                    .setValue(WallTorchBlock.FACING, state.getValue(WallTorchBlock.FACING))
                    .setValue(LimitedLightBlock.BURN, 15);
        }

        else if (state.is(Blocks.TORCH)) {
            state = CoralineBlocks.TORCH.get().defaultBlockState()
                    .setValue(LimitedLightBlock.BURN, 15);
        }

        return original.call(level, pos, state, flags);
    }
}