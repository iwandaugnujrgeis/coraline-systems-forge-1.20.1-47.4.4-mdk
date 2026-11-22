package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.CoralineSystems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Unique
    private static final TagKey<Block> LAVA_PROOF =
            TagKey.create(Registries.BLOCK, CoralineSystems.of("lava_proof"));

    @Inject(method = "ignitedByLava", at = @At("HEAD"), cancellable = true)
    private void coraline$preventLavaIgnition(CallbackInfoReturnable<Boolean> cir) {
        if (((BlockState) (Object) this).is(LAVA_PROOF)) {
            cir.setReturnValue(false);
        }
    }
}