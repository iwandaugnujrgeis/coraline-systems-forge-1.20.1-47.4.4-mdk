package net.zharok01.coralinesystems.mixin.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.zharok01.coralinesystems.util.IAdvancementWidgetCS;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Coraline Systems — replaces JSON-driven static positions with a
 * Reingold-Tilford-style centered tree layout computed at runtime.
 *
 * <h3>How the layout works</h3>
 * <ol>
 *   <li>{@link #theCoralineSystems$csComputeSubtreeWidth} recurses from a node to find how many
 *       "slots" its entire subtree needs horizontally.</li>
 *   <li>{@link #theCoralineSystems$csAssignPositions} walks the tree top-down, centring every
 *       parent within its allocated slot range and placing children
 *       left-to-right beneath it.</li>
 *   <li>{@link #theCoralineSystems$csRelayout} is the entry point: it runs the pass, recomputes
 *       the AABB, and resets {@code centered} so {@code drawContents}
 *       recentres the viewport on the next frame.</li>
 * </ol>
 *
 * <h3>Why there is no AdvancementTabType import</h3>
 * {@code AdvancementTabType} is a package-private enum and cannot be imported
 * from outside its package. The constructor descriptor string still references
 * it in its JVM form, but the {@code @Inject} callback drops all original
 * parameters (only {@code CallbackInfo} is kept), so no Java-level reference
 * to the type is needed anywhere in this class.
 *
 * <h3>Injection points</h3>
 * <ul>
 *   <li>Constructor TAIL — fires once when the root widget is added.</li>
 *   <li>{@code addAdvancement} TAIL — fires each time a non-root widget
 *       arrives from the server.</li>
 * </ul>
 */
@Mixin(net.minecraft.client.gui.screens.advancements.AdvancementTab.class)
public abstract class MixinAdvancementTab {

    // =========================================================================
    // Layout constants
    // =========================================================================

    /** Horizontal pixels between slot centres (matches vanilla widget width). */
    @Unique
    private static final int CS_SLOT_W      = 28;
    /** Vertical pixels between tree levels (matches vanilla widget height). */
    @Unique
    private static final int CS_SLOT_H      = 27;
    /**
     * Extra blank slots inserted between every pair of adjacent sibling
     * subtrees.  1 = one empty slot of breathing room; 0 = packed tight.
     */
    private static final int CS_SIBLING_GAP = 1;

    // =========================================================================
    // Shadowed vanilla fields
    // =========================================================================

    @Shadow @Final private AdvancementWidget root;
    @Final
    @Shadow private Map<Advancement, AdvancementWidget> widgets;
    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;
    @Shadow private boolean centered;

    // =========================================================================
    // Injection — constructor TAIL
    // =========================================================================

    /**
     * Fires after the primary constructor completes (root widget registered).
     *
     * <p>The full JVM descriptor for the constructor is supplied so Mixin can
     * locate the correct overload. No Java-level reference to
     * {@code AdvancementTabType} is needed in the callback parameters —
     * we only keep {@code CallbackInfo}.
     */
    @Inject(
            method = "<init>(Lnet/minecraft/client/Minecraft;"
                    + "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;"
                    + "Lnet/minecraft/client/gui/screens/advancements/AdvancementTabType;"
                    + "ILnet/minecraft/advancements/Advancement;"
                    + "Lnet/minecraft/advancements/DisplayInfo;)V",
            at = @At("TAIL")
    )
    private void cs$onInit(CallbackInfo ci) {
        theCoralineSystems$csRelayout();
    }

    // =========================================================================
    // Injection — addAdvancement TAIL
    // =========================================================================

    /**
     * Fires after a non-root advancement widget has been created and
     * registered.  Skips relayout when the advancement has no display info,
     * matching vanilla's own guard (no widget is added in that case).
     */
    @Inject(method = "addAdvancement", at = @At("TAIL"))
    private void cs$onAddAdvancement(Advancement advancement, CallbackInfo ci) {
        if (advancement.getDisplay() != null) {
            theCoralineSystems$csRelayout();
        }
    }

    // =========================================================================
    // Layout algorithm
    // =========================================================================

    /**
     * Returns how many "slots" wide the subtree rooted at {@code widget}
     * requires.
     *
     * <ul>
     *   <li>Leaf node  = 1 slot.</li>
     *   <li>Parent     = Σ(children's widths) + {@link #CS_SIBLING_GAP}
     *       between each adjacent pair.</li>
     * </ul>
     */
    @Unique
    private int theCoralineSystems$csComputeSubtreeWidth(AdvancementWidget widget) {
        List<AdvancementWidget> children =
                ((IAdvancementWidgetCS) widget).getChildren();
        if (children.isEmpty()) return 1;

        int total = 0;
        for (int i = 0; i < children.size(); i++) {
            total += theCoralineSystems$csComputeSubtreeWidth(children.get(i));
            if (i < children.size() - 1) total += CS_SIBLING_GAP;
        }
        return Math.max(1, total);
    }

    /**
     * Recursively assigns pixel positions to {@code widget} and all
     * descendants.
     *
     * @param widget    The node being positioned.
     * @param startSlot Leftmost slot index allocated to this subtree.
     * @param depth     Tree depth (root = 0) — drives the Y coordinate.
     */
    @Unique
    private void theCoralineSystems$csAssignPositions(AdvancementWidget widget, int startSlot, int depth) {
        int subtreeWidth = theCoralineSystems$csComputeSubtreeWidth(widget);

        // Centre this node within the slot band it owns.
        int centreSlot = startSlot + subtreeWidth / 2;
        IAdvancementWidgetCS api = (IAdvancementWidgetCS) widget;
        api.setX(centreSlot * CS_SLOT_W);
        api.setY(depth      * CS_SLOT_H);

        // Distribute children left-to-right inside the remaining slots.
        int childSlot = startSlot;
        List<AdvancementWidget> children = api.getChildren();
        for (int i = 0; i < children.size(); i++) {
            int childWidth = theCoralineSystems$csComputeSubtreeWidth(children.get(i));
            theCoralineSystems$csAssignPositions(children.get(i), childSlot, depth + 1);
            childSlot += childWidth;
            if (i < children.size() - 1) childSlot += CS_SIBLING_GAP;
        }
    }

    /**
     * Entry point for the layout pass:
     * <ol>
     *   <li>Positions every widget via {@link #theCoralineSystems$csAssignPositions}.</li>
     *   <li>Recomputes the AABB for scroll clamping.</li>
     *   <li>Resets {@code centered} so the viewport recentres next frame.</li>
     * </ol>
     */
    @Unique
    private void theCoralineSystems$csRelayout() {
        theCoralineSystems$csAssignPositions(this.root, 0, 0);

        this.minX = Integer.MAX_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;

        for (AdvancementWidget w : this.widgets.values()) {
            int wx = w.getX();
            int wy = w.getY();
            this.minX = Math.min(this.minX, wx);
            this.maxX = Math.max(this.maxX, wx + CS_SLOT_W);
            this.minY = Math.min(this.minY, wy);
            this.maxY = Math.max(this.maxY, wy + CS_SLOT_H);
        }

        this.centered = false;
    }
}


