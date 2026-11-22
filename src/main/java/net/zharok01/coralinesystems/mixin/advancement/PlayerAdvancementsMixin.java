package net.zharok01.coralinesystems.mixin.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
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

    @Shadow @Final private Set<Advancement> visible;

    @Shadow public abstract AdvancementProgress getOrStartProgress(Advancement advancement);

    @Inject(method = "updateTreeVisibility", at = @At("HEAD"), cancellable = true)
    private void coraline$showWholeTree(
            Advancement advancement,
            Set<Advancement> visibleAdvancements,
            Set<ResourceLocation> invisibleAdvancements,
            CallbackInfo ci) {

        Advancement root = advancement.getRoot();

        if (this.getOrStartProgress(root).isDone()) {
            coraline$markVisible(root, visibleAdvancements);
        } else {
            coraline$markInvisible(root, invisibleAdvancements);
        }
        ci.cancel();
    }

    @Unique
    private void coraline$markVisible(Advancement advancement, Set<Advancement> visibleAdvancements) {
        if (advancement.getDisplay() != null && this.visible.add(advancement)) {
            visibleAdvancements.add(advancement);
        }
        for (Advancement child : advancement.getChildren()) {
            coraline$markVisible(child, visibleAdvancements);
        }
    }

    @Unique
    private void coraline$markInvisible(Advancement advancement, Set<ResourceLocation> invisibleAdvancements) {
        if (advancement.getDisplay() != null && this.visible.remove(advancement)) {
            invisibleAdvancements.add(advancement.getId());
        }
        for (Advancement child : advancement.getChildren()) {
            coraline$markInvisible(child, invisibleAdvancements);
        }
    }
}