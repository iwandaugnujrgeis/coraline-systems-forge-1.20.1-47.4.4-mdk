package net.zharok01.coralinesystems.mixin.compat.passable_foliage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zharok01.coralinesystems.util.helper.PassableFoliageHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockStateBase.class)
public class BlockStateMixin {

    @Unique
    private BlockState coraline$self() {
        return (BlockState) (Object) this;
    }

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$getCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context,
                                            CallbackInfoReturnable<VoxelShape> ci) {
        if (!PassableFoliageHelper.isPassable(coraline$self())) {
            return;
        }

        Entity entity = context instanceof EntityCollisionContext entityContext ? entityContext.getEntity() : null;
        if (entity == null
                || (entity instanceof LivingEntity living
                && (PassableFoliageHelper.hasLeafWalker(living) || PassableFoliageHelper.isLeafPercher(living)))) {
            return;
        }

        ci.setReturnValue(PassableFoliageHelper.SUNKEN_SHAPE);
    }

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void coraline$entityInside(Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (PassableFoliageHelper.isPassable(coraline$self())) {
            PassableFoliageHelper.slowSinkingEntity(pos, entity);
        }
    }
}