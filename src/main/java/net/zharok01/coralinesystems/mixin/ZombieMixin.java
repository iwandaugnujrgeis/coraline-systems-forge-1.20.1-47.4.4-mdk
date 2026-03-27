package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Zombie.class)
public class ZombieMixin {

    /**
     * Intercepts the boolean parameter passed into setCanBreakDoors.
     * Whether the game tries to pass 'false' from a spawn roll or an old save file,
     * this will overwrite it and force the Zombie to true.
     */
    @ModifyVariable(method = "setCanBreakDoors", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean coraline$forceBreakDoors(boolean original) {
        return true;
    }
}