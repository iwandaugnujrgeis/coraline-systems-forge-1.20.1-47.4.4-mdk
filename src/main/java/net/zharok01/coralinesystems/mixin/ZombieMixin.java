package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Zombie.class)
public class ZombieMixin {
    @ModifyConstant(method = "finalizeSpawn", constant = @Constant(floatValue = 0.1F))
    private float coraline$setBreakDoorChance(float constant) {
        return 1.0F; // 100% of zombies get the goal assigned
    }

    /**
     * @author
     * @reason
     */

    @Overwrite
    public boolean canBreakDoors() {
        return true; // Bypasses internal checks
    }
}