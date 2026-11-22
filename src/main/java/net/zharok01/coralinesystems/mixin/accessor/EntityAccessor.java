package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Invoker("getBlockJumpFactor")
    float coraline$invokeGetBlockJumpFactor();

    @Invoker("getBlockSpeedFactor")
    float coraline$invokeGetBlockSpeedFactor();

    @Accessor("random")
    RandomSource coraline$getRandom();
}