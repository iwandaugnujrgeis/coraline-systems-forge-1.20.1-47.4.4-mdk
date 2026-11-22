package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {

    @Inject(method = "setInstrument", at = @At("HEAD"), cancellable = true)
    private void coraline$onlyHeadInstruments(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir) {
        NoteBlockInstrument above = level.getBlockState(pos.above()).instrument();

        if (above.worksAboveNoteBlock()) {
            cir.setReturnValue(state.setValue(NoteBlock.INSTRUMENT, above));
        } else if (state.getValue(NoteBlock.INSTRUMENT).worksAboveNoteBlock()) {
            cir.setReturnValue(state.setValue(NoteBlock.INSTRUMENT, NoteBlockInstrument.HARP));
        } else {
            cir.setReturnValue(state);
        }
    }

    @Redirect(method = "playNote", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean coraline$ignoreBlockAbove(BlockState blockState) {
        return true;
    }
}