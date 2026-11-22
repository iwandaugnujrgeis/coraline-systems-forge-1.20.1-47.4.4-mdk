package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternBlock.class)
public class LecternBlockMixin {

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void coraline$getShape(BlockState state, BlockGetter level, BlockPos pos,
                                   CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(Shapes.block());
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void coraline$getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                            CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(Shapes.block());
    }

    @Inject(method = "getOcclusionShape", at = @At("HEAD"), cancellable = true)
    private void coraline$getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos,
                                            CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(Shapes.block());
    }
}