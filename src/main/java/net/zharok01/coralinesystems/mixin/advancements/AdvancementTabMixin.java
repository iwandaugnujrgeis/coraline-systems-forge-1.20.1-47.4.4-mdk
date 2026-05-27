package net.zharok01.coralinesystems.mixin.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.zharok01.coralinesystems.util.IAdvancementWidgetCS;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(net.minecraft.client.gui.screens.advancements.AdvancementTab.class)
public abstract class AdvancementTabMixin {

    // Pixel distance between adjacent grid cells.
    // 36 px: compact enough to keep large trees visible without excessive
    // dragging, while leaving 19 px of visible branch line per sibling
    // (child centre is 36 px from parent centre; junction is 17 px from
    // parent centre → 36 − 17 = 19 px of run per sibling).
    @Unique private static final int CS_SLOT_W = 36;
    @Unique private static final int CS_SLOT_H = 36;

    // Cardinal directions used by the BFS collision resolver.
    // Right=0, Down=1, Left=2, Up=3 — matches the arrivalDir values.
    //@Unique private static final int[][] CS_DIRS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

    @Shadow @Final private AdvancementWidget root;
    @Final @Shadow private Map<Advancement, AdvancementWidget> widgets;
    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;

    // ──────────────────────────────────────────────────────────────────────────
    // 4-pass connectivity rendering  (green lines always on top of dark ones)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Replaces the vanilla shadow-pass {@code drawConnectivity} call inside
     * {@code drawContents} with two ordered passes:
     * <ol>
     *   <li>Dark shadow lines — drawn first, sit below everything.</li>
     *   <li>Green shadow lines — drawn second, always rendered on top.</li>
     * </ol>
     */
    @Redirect(
            method = "drawContents",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;"
                            + "drawConnectivity(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V",
                    ordinal = 0))
    private void cs$redirectShadowConnectivity(
            AdvancementWidget root, GuiGraphics g, int x, int y, boolean shadow) {
        IAdvancementWidgetCS api = (IAdvancementWidgetCS) root;
        api.drawConnectivityCS(g, x, y, true, false); // dark shadows first
        api.drawConnectivityCS(g, x, y, true, true);  // green shadows on top
    }

    /**
     * Replaces the vanilla colour-pass {@code drawConnectivity} call inside
     * {@code drawContents} with two ordered passes:
     * <ol>
     *   <li>Dark colour lines — drawn first, sit below.</li>
     *   <li>Green colour lines — drawn second, always rendered on top.</li>
     * </ol>
     */
    @Redirect(
            method = "drawContents",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;"
                            + "drawConnectivity(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V",
                    ordinal = 1))
    private void cs$redirectColorConnectivity(
            AdvancementWidget root, GuiGraphics g, int x, int y, boolean shadow) {
        IAdvancementWidgetCS api = (IAdvancementWidgetCS) root;
        api.drawConnectivityCS(g, x, y, false, false); // dark lines first
        api.drawConnectivityCS(g, x, y, false, true);  // green lines on top
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Layout hooks
    // ──────────────────────────────────────────────────────────────────────────

    @Inject(
            method = "<init>(Lnet/minecraft/client/Minecraft;"
                    + "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;"
                    + "Lnet/minecraft/client/gui/screens/advancements/AdvancementTabType;"
                    + "ILnet/minecraft/advancements/Advancement;"
                    + "Lnet/minecraft/advancements/DisplayInfo;)V",
            at = @At("TAIL"))
    private void cs$onInit(CallbackInfo ci) {
        cs$relayout();
    }

    @Inject(method = "addAdvancement", at = @At("TAIL"))
    private void cs$onAddAdvancement(Advancement advancement, CallbackInfo ci) {
        if (advancement.getDisplay() != null) {
            cs$relayout();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Layout — two-pass subtree-isolated omnidirectional placement
    // ──────────────────────────────────────────────────────────────────────────

    @Unique
    private void cs$relayout() {
        if (this.root == null) return;

        // Pass 1: leaf counts
        Map<AdvancementWidget, Integer> leafCounts = new HashMap<>();
        cs$computeLeafCounts(this.root, leafCounts);

        // Pass 2: placement
        Map<AdvancementWidget, int[]> positions = new HashMap<>();
        positions.put(this.root, new int[]{0, 0});
        ((IAdvancementWidgetCS) this.root).setArrivalDir(-1);

        // Split root children into right and left groups.
        // Sort descending by size so the largest branch goes right.
        List<AdvancementWidget> rootChildren =
                new ArrayList<>(((IAdvancementWidgetCS) this.root).getChildren());
        rootChildren.sort((a, b) ->
                leafCounts.getOrDefault(b, 1) - leafCounts.getOrDefault(a, 1));

        List<AdvancementWidget> rightGroup = new ArrayList<>();
        List<AdvancementWidget> leftGroup  = new ArrayList<>();
        for (int i = 0; i < rootChildren.size(); i++) {
            (i % 2 == 0 ? rightGroup : leftGroup).add(rootChildren.get(i));
        }

        int rightPerp = 0;
        for (AdvancementWidget child : rightGroup) {
            int leaves = leafCounts.getOrDefault(child, 1);
            cs$placeSubtree(child, 1, rightPerp, rightPerp + leaves,
                    0, positions, leafCounts);
            rightPerp += leaves;
        }

        int leftPerp = 0;
        for (AdvancementWidget child : leftGroup) {
            int leaves = leafCounts.getOrDefault(child, 1);
            cs$placeSubtree(child, 1, leftPerp, leftPerp + leaves,
                    2, positions, leafCounts);
            leftPerp += leaves;
        }

        // Normalise: shift so minimum grid coords are (0, 0).
        int minGridX = positions.values().stream().mapToInt(p -> p[0]).min().orElse(0);
        int minGridY = positions.values().stream().mapToInt(p -> p[1]).min().orElse(0);

        this.minX = Integer.MAX_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;

        for (Map.Entry<AdvancementWidget, int[]> entry : positions.entrySet()) {
            IAdvancementWidgetCS api = (IAdvancementWidgetCS) entry.getKey();
            int px = (entry.getValue()[0] - minGridX) * CS_SLOT_W;
            int py = (entry.getValue()[1] - minGridY) * CS_SLOT_H;

            api.setX(px);
            api.setY(py);

            this.minX = Math.min(this.minX, px);
            this.maxX = Math.max(this.maxX, px + 28);
            this.minY = Math.min(this.minY, py);
            this.maxY = Math.max(this.maxY, py + 27);
        }
    }

    @Unique
    private void cs$placeSubtree(
            AdvancementWidget node,
            int depth,
            int perpMin, int perpMax,
            int dir,
            Map<AdvancementWidget, int[]> positions,
            Map<AdvancementWidget, Integer> leafCounts) {

        ((IAdvancementWidgetCS) node).setArrivalDir(dir);

        int perpCenter = (perpMin + perpMax) / 2;
        // Right (0): x = +depth,  Left (2): x = -depth
        positions.put(node, new int[]{dir == 0 ? depth : -depth, perpCenter});

        List<AdvancementWidget> children = ((IAdvancementWidgetCS) node).getChildren();
        int currentPerp = perpMin;
        for (AdvancementWidget child : children) {
            int childLeaves = leafCounts.getOrDefault(child, 1);
            cs$placeSubtree(child, depth + 1,
                    currentPerp, currentPerp + childLeaves,
                    dir, positions, leafCounts);
            currentPerp += childLeaves;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Leaf-count computation (DFS, cached)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Recursively computes and caches the leaf count for every node.
     * Leaf count = number of leaf descendants, or 1 for a childless node.
     * This determines how many perpendicular grid slots the subtree needs so
     * that no two sibling subtrees can ever share a cell.
     */
    @Unique
    private static int cs$computeLeafCounts(
            AdvancementWidget node,
            Map<AdvancementWidget, Integer> cache) {

        List<AdvancementWidget> children = ((IAdvancementWidgetCS) node).getChildren();
        if (children.isEmpty()) {
            cache.put(node, 1);
            return 1;
        }
        int sum = 0;
        for (AdvancementWidget child : children)
            sum += cs$computeLeafCounts(child, cache);
        cache.put(node, sum);
        return sum;
    }
}