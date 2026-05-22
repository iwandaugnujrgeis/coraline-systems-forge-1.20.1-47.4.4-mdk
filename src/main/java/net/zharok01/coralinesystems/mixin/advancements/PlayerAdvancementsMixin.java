package net.zharok01.coralinesystems.mixin.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {

    @Final @Shadow private Set<Advancement> visible;

    @Inject(method = "updateTreeVisibility", at = @At("HEAD"), cancellable = true)
    private void cs$forceFullTreeVisibility(Advancement advancement, Set<Advancement> visibleAdvancements, Set<ResourceLocation> invisibleAdvancements, CallbackInfo ci) {
        cs$markVisibleRecursive(advancement, visibleAdvancements);
        ci.cancel();
    }

    @Unique
    private void cs$markVisibleRecursive(Advancement adv, Set<Advancement> visibleAdvancements) {
        if (adv.getDisplay() != null && !adv.getDisplay().isHidden()) {
            if (this.visible.add(adv)) {
                visibleAdvancements.add(adv);
            }
        }
        for (Advancement child : adv.getChildren()) {
            cs$markVisibleRecursive(child, visibleAdvancements);
        }
    }
}