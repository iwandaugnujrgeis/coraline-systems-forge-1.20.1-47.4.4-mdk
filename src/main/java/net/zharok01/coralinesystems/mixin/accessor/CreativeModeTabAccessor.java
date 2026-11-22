package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {

    @Accessor("displayName")
    Component coraline$getDisplayName();

    @Accessor("displayItemsGenerator")
    CreativeModeTab.DisplayItemsGenerator coraline$getDisplayItemsGenerator();

    @Accessor("displayItemsGenerator")
    @Mutable
    void coraline$setDisplayItemsGenerator(CreativeModeTab.DisplayItemsGenerator generator);
}