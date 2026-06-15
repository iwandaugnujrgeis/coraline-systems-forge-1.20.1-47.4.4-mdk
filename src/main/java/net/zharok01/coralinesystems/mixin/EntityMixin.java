package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract Level level();
    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

    /**
     * @author Gemini
     * @reason Intercepts combination step sounds to isolate and play only the primary
     * top-layer sound (e.g., snow layers), entirely bypassing the underlying floor sound.
     */
    @Inject(method = "playCombinationStepSounds", at = @At("HEAD"), cancellable = true)
    private void coraline_systems$removeSecondaryCombinationStepSound(
            BlockState arg,
            BlockState arg2,
            BlockPos primaryPos,
            BlockPos secondaryPos,
            CallbackInfo ci
    ) {
        // Fetch the sound configuration for the primary top block (e.g., Snow Layer Block)
        SoundType soundType = arg.getSoundType(this.level(), primaryPos, (Entity) (Object) this);

        // Reproduce the vanilla step sound execution for the primary block exclusively
        this.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getPitch());

        // Terminate execution early to prevent this.playMuffledStepSound(...) from executing
        ci.cancel();
    }
}