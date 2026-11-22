package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabLayoutAccessor {

    @Accessor("row")
    @Mutable
    void coraline$setRow(CreativeModeTab.Row row);

    @Accessor("column")
    @Mutable
    void coraline$setColumn(int column);
}
