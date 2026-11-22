package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.zharok01.coralinesystems.client.gui.CoralineDragEnd;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin implements CoralineDragEnd {

    @Shadow private boolean isScrolling;

    @Override
    public void coraline$endDrag() {
        this.isScrolling = false;
    }
}
