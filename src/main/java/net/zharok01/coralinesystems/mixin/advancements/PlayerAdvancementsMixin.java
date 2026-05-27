package net.zharok01.coralinesystems.mixin.advancements;

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

    @Final @Shadow private Set<Advancement> visible;

    // getOrStartProgress is public in PlayerAdvancements — safe to shadow.
    @Shadow public abstract AdvancementProgress getOrStartProgress(Advancement advancement);

    @Inject(method = "updateTreeVisibility", at = @At("HEAD"), cancellable = true)
    private void cs$forceFullTreeVisibility(
            Advancement advancement,
            Set<Advancement> visibleAdvancements,
            Set<ResourceLocation> invisibleAdvancements,
            CallbackInfo ci) {

        Advancement root = advancement.getRoot();

        // The tab is only shown while its root advancement is completed.
        // This preserves vanilla tab-hide behaviour on revoke.
        if (this.getOrStartProgress(root).isDone()) {
            cs$markVisibleRecursive(root, visibleAdvancements);
        } else {
            cs$markInvisibleRecursive(root, invisibleAdvancements);
        }
        ci.cancel();
    }

    /**
     * Adds every advancement in the subtree to {@code visible}.
     * The {@code isHidden()} flag is intentionally NOT checked here —
     * hidden advancements are shown as locked silhouettes by our draw() override
     * and must reach the client to get a widget.
     */
    @Unique
    private void cs$markVisibleRecursive(
            Advancement adv,
            Set<Advancement> visibleAdvancements) {

        if (adv.getDisplay() != null) {
            if (this.visible.add(adv)) {
                visibleAdvancements.add(adv);
            }
        }
        for (Advancement child : adv.getChildren()) {
            cs$markVisibleRecursive(child, visibleAdvancements);
        }
    }

    /**
     * Removes every advancement in the subtree from {@code visible} and
     * schedules it for removal on the client. Called when the root is revoked.
     */
    @Unique
    private void cs$markInvisibleRecursive(
            Advancement adv,
            Set<ResourceLocation> invisibleAdvancements) {

        if (adv.getDisplay() != null) {
            if (this.visible.remove(adv)) {
                invisibleAdvancements.add(adv.getId());
            }
        }
        for (Advancement child : adv.getChildren()) {
            cs$markInvisibleRecursive(child, invisibleAdvancements);
        }
    }
}