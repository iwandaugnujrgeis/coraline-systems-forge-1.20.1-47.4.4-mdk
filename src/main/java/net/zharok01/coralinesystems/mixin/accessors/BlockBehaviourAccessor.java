package net.zharok01.coralinesystems.mixin.accessors;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {

    @Accessor("soundType")
    @Mutable
    void setSoundType(SoundType soundType);
}