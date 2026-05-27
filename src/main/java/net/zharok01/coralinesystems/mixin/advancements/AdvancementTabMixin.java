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

    @Unique
    private static final int CS_SLOT_W = 32;

    @Unique
    private static final int CS_SLOT_H = 28;

    /*
     * Direction constants:
     * 0 = right
     * 2 = left
     */
    @Unique
    private static final int CS_DIR_RIGHT = 0;

    @Unique
    private static final int CS_DIR_LEFT = 2;

    @Shadow
    @Final
    private AdvancementWidget root;

    @Shadow
    @Final
    private Map<Advancement, AdvancementWidget> widgets;

    @Shadow
    private int minX;

    @Shadow
    private int maxX;

    @Shadow
    private int minY;

    @Shadow
    private int maxY;

    // -------------------------------------------------------------------------
    // Connectivity rendering
    // -------------------------------------------------------------------------

    @Redirect(
            method = "drawContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;" +
                            "drawConnectivity(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V",
                    ordinal = 0
            )
    )
    private void cs$redirectShadowConnectivity(
            AdvancementWidget root,
            GuiGraphics graphics,
            int x,
            int y,
            boolean shadow
    ) {

        IAdvancementWidgetCS api = (IAdvancementWidgetCS) root;

        api.drawConnectivityCS(graphics, x, y, true, false);
        api.drawConnectivityCS(graphics, x, y, true, true);
    }

    @Redirect(
            method = "drawContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;" +
                            "drawConnectivity(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V",
                    ordinal = 1
            )
    )
    private void cs$redirectColorConnectivity(
            AdvancementWidget root,
            GuiGraphics graphics,
            int x,
            int y,
            boolean shadow
    ) {

        IAdvancementWidgetCS api = (IAdvancementWidgetCS) root;

        api.drawConnectivityCS(graphics, x, y, false, false);
        api.drawConnectivityCS(graphics, x, y, false, true);
    }

    // -------------------------------------------------------------------------
    // Relayout hooks
    // -------------------------------------------------------------------------

    @Inject(
            method = "<init>(Lnet/minecraft/client/Minecraft;" +
                    "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;" +
                    "Lnet/minecraft/client/gui/screens/advancements/AdvancementTabType;" +
                    "ILnet/minecraft/advancements/Advancement;" +
                    "Lnet/minecraft/advancements/DisplayInfo;)V",
            at = @At("TAIL")
    )
    private void cs$afterInit(CallbackInfo ci) {
        cs$relayout();
    }

    @Inject(method = "addWidget", at = @At("TAIL"))
    private void cs$afterAddWidget(
            AdvancementWidget widget,
            Advancement advancement,
            CallbackInfo ci
    ) {
        cs$relayout();
    }

    // -------------------------------------------------------------------------
    // Compact layered layout
    // -------------------------------------------------------------------------

    @Unique
    private void cs$relayout() {

        if (this.root == null) {
            return;
        }

        Map<AdvancementWidget, Integer> subtreeWeights = new HashMap<>();
        cs$computeWeights(this.root, subtreeWeights);

        Map<AdvancementWidget, int[]> positions = new HashMap<>();

        /*
         * Occupancy:
         * key   = grid X
         * value = occupied Y rows
         */
        Map<Integer, Set<Integer>> occupied = new HashMap<>();

        positions.put(this.root, new int[]{0, 0});
        occupied.computeIfAbsent(0, k -> new HashSet<>()).add(0);

        ((IAdvancementWidgetCS) this.root).setArrivalDir(-1);

        List<AdvancementWidget> rootChildren =
                new ArrayList<>(((IAdvancementWidgetCS) this.root).getChildren());

        rootChildren.sort((a, b) ->
                subtreeWeights.getOrDefault(b, 1)
                        - subtreeWeights.getOrDefault(a, 1));

        List<AdvancementWidget> right = new ArrayList<>();
        List<AdvancementWidget> left = new ArrayList<>();

        for (int i = 0; i < rootChildren.size(); i++) {
            (i % 2 == 0 ? right : left).add(rootChildren.get(i));
        }

        int rightBias = -1;

        for (AdvancementWidget child : right) {

            cs$layoutBranch(
                    child,
                    1,
                    rightBias,
                    CS_DIR_RIGHT,
                    positions,
                    occupied,
                    subtreeWeights
            );

            rightBias++;
        }

        int leftBias = 1;

        for (AdvancementWidget child : left) {

            cs$layoutBranch(
                    child,
                    -1,
                    leftBias,
                    CS_DIR_LEFT,
                    positions,
                    occupied,
                    subtreeWeights
            );

            leftBias++;
        }

        int minGridX = positions.values()
                .stream()
                .mapToInt(v -> v[0])
                .min()
                .orElse(0);

        int minGridY = positions.values()
                .stream()
                .mapToInt(v -> v[1])
                .min()
                .orElse(0);

        this.minX = Integer.MAX_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;

        for (Map.Entry<AdvancementWidget, int[]> entry : positions.entrySet()) {

            int gridX = entry.getValue()[0] - minGridX;
            int gridY = entry.getValue()[1] - minGridY;

            int px = gridX * CS_SLOT_W;
            int py = gridY * CS_SLOT_H;

            IAdvancementWidgetCS api = (IAdvancementWidgetCS) entry.getKey();

            api.setX(px);
            api.setY(py);

            this.minX = Math.min(this.minX, px);
            this.maxX = Math.max(this.maxX, px + 28);

            this.minY = Math.min(this.minY, py);
            this.maxY = Math.max(this.maxY, py + 27);
        }
    }

    @Unique
    private void cs$layoutBranch(
            AdvancementWidget node,
            int x,
            int preferredY,
            int direction,
            Map<AdvancementWidget, int[]> positions,
            Map<Integer, Set<Integer>> occupied,
            Map<AdvancementWidget, Integer> subtreeWeights
    ) {

        int y = cs$findNearestFreeY(x, preferredY, occupied);

        occupied.computeIfAbsent(x, k -> new HashSet<>()).add(y);

        positions.put(node, new int[]{x, y});

        ((IAdvancementWidgetCS) node).setArrivalDir(direction);

        List<AdvancementWidget> children =
                new ArrayList<>(((IAdvancementWidgetCS) node).getChildren());

        children.sort((a, b) ->
                subtreeWeights.getOrDefault(b, 1)
                        - subtreeWeights.getOrDefault(a, 1));

        int nextX = direction == CS_DIR_RIGHT ? x + 1 : x - 1;

        int offset = 0;

        for (AdvancementWidget child : children) {

            int preferredChildY;

            if ((offset & 1) == 0) {
                preferredChildY = y + ((offset + 1) / 2);
            } else {
                preferredChildY = y - ((offset + 1) / 2);
            }

            cs$layoutBranch(
                    child,
                    nextX,
                    preferredChildY,
                    direction,
                    positions,
                    occupied,
                    subtreeWeights
            );

            offset++;
        }
    }

    /*
     * Finds the nearest free vertical slot.
     *
     * Search order:
     * preferred
     * +1
     * -1
     * +2
     * -2
     * ...
     */
    @Unique
    private int cs$findNearestFreeY(
            int x,
            int preferredY,
            Map<Integer, Set<Integer>> occupied
    ) {

        Set<Integer> used = occupied.computeIfAbsent(x, k -> new HashSet<>());

        if (!used.contains(preferredY)) {
            return preferredY;
        }

        for (int radius = 1; radius < 2048; radius++) {

            int down = preferredY + radius;

            if (!used.contains(down)) {
                return down;
            }

            int up = preferredY - radius;

            if (!used.contains(up)) {
                return up;
            }
        }

        return preferredY;
    }

    /*
     * Weight = total descendants + self.
     *
     * Larger subtrees are packed first to reduce fragmentation.
     */
    @Unique
    private static int cs$computeWeights(
            AdvancementWidget node,
            Map<AdvancementWidget, Integer> cache
    ) {

        int weight = 1;

        for (AdvancementWidget child :
                ((IAdvancementWidgetCS) node).getChildren()) {

            weight += cs$computeWeights(child, cache);
        }

        cache.put(node, weight);

        return weight;
    }
}