package net.zharok01.coralinesystems.mixin.compat.supplementaries;

import net.mehvahdjukaar.supplementaries.common.block.blocks.SackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SackBlock.class)
public abstract class SackBlockMixin {

    @Inject(method = "m_5940_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$makeSackFullBlock(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(Shapes.block());
    }

    @Inject(method = "m_5573_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$preventWaterloggedPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        SackBlock sackBlock = (SackBlock) (Object) this;
        cir.setReturnValue(sackBlock.defaultBlockState().setValue(SackBlock.WATERLOGGED, false));
    }

    @Inject(method = "m_5888_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$forceEmptyFluidState(BlockState state, CallbackInfoReturnable<FluidState> cir) {
        cir.setReturnValue(Fluids.EMPTY.defaultFluidState());
    }
}