package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuAccessor {
    @Mutable
    @Accessor("containerId")
    void coraline$setContainerId(int containerId);
}
