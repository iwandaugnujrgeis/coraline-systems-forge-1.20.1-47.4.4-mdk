package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Blaze.class)
public interface BlazeAccessor {

    @Invoker("setCharged")
    void coraline$setCharged(boolean charged);

    @Invoker("isCharged")
    boolean coraline$isCharged();
}