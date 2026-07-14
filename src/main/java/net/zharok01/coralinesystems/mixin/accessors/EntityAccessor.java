package net.zharok01.coralinesystems.mixin.accessors;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Invoker("getBlockJumpFactor")
    float coraline$invokeGetBlockJumpFactor();

    @Invoker("getBlockSpeedFactor")
    float coraline$invokeGetBlockSpeedFactor();
}