package net.zharok01.coralinesystems.mixin.accessors;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementWidget.class)
public interface AdvancementWidgetAccessor {

    @Accessor("advancement")
    Advancement coralineSystems$getAdvancement();
}