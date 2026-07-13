package net.zharok01.coralinesystems.mixin.accessors;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityGrizzlyBear.class, remap = false)
public interface EntityGrizzlyBearAccessor {

    @Accessor("honeyedTime")
    void coraline$setHoneyedTime(int time);
}