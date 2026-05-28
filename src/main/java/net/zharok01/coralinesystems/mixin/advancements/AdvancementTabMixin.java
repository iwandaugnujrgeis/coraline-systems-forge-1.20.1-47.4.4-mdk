package net.zharok01.coralinesystems.mixin.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.zharok01.coralinesystems.client.advancements.GridPos;
import net.zharok01.coralinesystems.client.advancements.LayoutCandidate;
import net.zharok01.coralinesystems.mixin.accessors.AdvancementWidgetAccessor;
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
    private static final int CS_SLOT_W = 34;

    @Unique
    private static final int CS_SLOT_H = 30;

    @Unique
    private static final int CS_WIDGET_SIZE = 28;

    /*
     * Orthogonal directions:
     *
     * 0 = RIGHT
     * 1 = DOWN
     * 2 = LEFT
     * 3 = UP
     */
    @Unique private static final int theCoralineSystems$RIGHT = 0;
    @Unique private static final int theCoralineSystems$DOWN = 1;
    @Unique private static final int theCoralineSystems$LEFT = 2;
    @Unique private static final int theCoralineSystems$UP = 3;

    @Shadow @Final
    private AdvancementWidget root;

    @Shadow @Final
    private Map<Advancement, AdvancementWidget> widgets;

    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;

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
            AdvancementWidget widget,
            GuiGraphics graphics,
            int x,
            int y,
            boolean shadow
    ) {

        IAdvancementWidgetCS api = (IAdvancementWidgetCS) widget;

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
            AdvancementWidget widget,
            GuiGraphics graphics,
            int x,
            int y,
            boolean shadow
    ) {

        IAdvancementWidgetCS api = (IAdvancementWidgetCS) widget;

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
    // Omnidirectional procedural layout
    // -------------------------------------------------------------------------

    @Unique
    private void cs$relayout() {

        if (this.root == null) {
            return;
        }

        Map<AdvancementWidget, Integer> subtreeWeights = new HashMap<>();
        cs$computeWeights(this.root, subtreeWeights);

        Map<AdvancementWidget, GridPos> positions = new HashMap<>();

        /*
         * Occupied widget cells.
         */
        Set<GridPos> occupied = new HashSet<>();

        /*
         * Reserved connector segments.
         *
         * Each line segment is stored as:
         * x1,y1 -> x2,y2
         */
        Set<String> reservedEdges = new HashSet<>();

        GridPos rootPos = new GridPos(0, 0);

        positions.put(this.root, rootPos);
        occupied.add(rootPos);

        ((IAdvancementWidgetCS) this.root).setArrivalDir(-1);

        List<AdvancementWidget> children =
                new ArrayList<>(((IAdvancementWidgetCS) this.root).getChildren());

        children.sort((a, b) ->
                subtreeWeights.getOrDefault(b, 1)
                        - subtreeWeights.getOrDefault(a, 1));

        for (AdvancementWidget child : children) {

            cs$placeNode(
                    child,
                    this.root,
                    positions,
                    occupied,
                    reservedEdges,
                    subtreeWeights
            );
        }

        int minGridX = positions.values().stream().mapToInt(GridPos::x).min().orElse(0);
        int minGridY = positions.values().stream().mapToInt(GridPos::y).min().orElse(0);

        this.minX = Integer.MAX_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;

        for (Map.Entry<AdvancementWidget, GridPos> entry : positions.entrySet()) {

            int gridX = entry.getValue().x() - minGridX;
            int gridY = entry.getValue().y() - minGridY;

            int px = gridX * CS_SLOT_W;
            int py = gridY * CS_SLOT_H;

            IAdvancementWidgetCS api =
                    (IAdvancementWidgetCS) entry.getKey();

            api.setX(px);
            api.setY(py);

            this.minX = Math.min(this.minX, px);
            this.maxX = Math.max(this.maxX, px + CS_WIDGET_SIZE);

            this.minY = Math.min(this.minY, py);
            this.maxY = Math.max(this.maxY, py + CS_WIDGET_SIZE);
        }
    }

    @Unique
    private void cs$placeNode(
            AdvancementWidget node,
            AdvancementWidget parent,
            Map<AdvancementWidget, GridPos> positions,
            Set<GridPos> occupied,
            Set<String> reservedEdges,
            Map<AdvancementWidget, Integer> subtreeWeights
    ) {

        GridPos parentPos = positions.get(parent);

        Random random =
                new Random(((AdvancementWidgetAccessor) node)
                        .coralineSystems$getAdvancement()
                        .getId()
                        .hashCode());

        List<Integer> directions =
                new ArrayList<>(Arrays.asList(theCoralineSystems$RIGHT, theCoralineSystems$DOWN, theCoralineSystems$LEFT, theCoralineSystems$UP));

        /*
         * Deterministic pseudo-random ordering.
         */
        Collections.shuffle(directions, random);

        LayoutCandidate bestCandidate = null;

        for (int direction : directions) {

            for (int distance = 1; distance <= 6; distance++) {

                int nx = parentPos.x();
                int ny = parentPos.y();

                switch (direction) {

                    case theCoralineSystems$RIGHT -> nx += distance;
                    case theCoralineSystems$LEFT -> nx -= distance;
                    case theCoralineSystems$DOWN -> ny += distance;
                    case theCoralineSystems$UP -> ny -= distance;
                }

                GridPos candidatePos = new GridPos(nx, ny);

                if (occupied.contains(candidatePos)) {
                    continue;
                }

                if (!cs$isEdgeRouteClear(
                        parentPos,
                        candidatePos,
                        reservedEdges
                )) {
                    continue;
                }

                int score =
                        cs$computePlacementScore(
                                parentPos,
                                candidatePos,
                                occupied,
                                subtreeWeights.getOrDefault(node, 1),
                                distance
                        );

                if (bestCandidate == null || score < bestCandidate.score()) {
                    bestCandidate =
                            new LayoutCandidate(candidatePos, direction, score);
                }
            }
        }

        if (bestCandidate == null) {

            /*
             * Emergency fallback:
             * spiral search.
             */
            int radius = 2;

            while (bestCandidate == null) {

                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {

                        GridPos pos =
                                new GridPos(parentPos.x() + dx, parentPos.y() + dy);

                        if (occupied.contains(pos)) {
                            continue;
                        }

                        bestCandidate =
                                new LayoutCandidate(pos, theCoralineSystems$RIGHT, 9999);

                        break;
                    }
                }

                radius++;
            }
        }

        positions.put(node, bestCandidate.pos());
        occupied.add(bestCandidate.pos());

        cs$reserveEdgeRoute(
                parentPos,
                bestCandidate.pos(),
                reservedEdges
        );

        ((IAdvancementWidgetCS) node)
                .setArrivalDir(bestCandidate.direction());

        List<AdvancementWidget> children =
                new ArrayList<>(((IAdvancementWidgetCS) node).getChildren());

        children.sort((a, b) ->
                subtreeWeights.getOrDefault(b, 1)
                        - subtreeWeights.getOrDefault(a, 1));

        for (AdvancementWidget child : children) {

            cs$placeNode(
                    child,
                    node,
                    positions,
                    occupied,
                    reservedEdges,
                    subtreeWeights
            );
        }
    }

    @Unique
    private int cs$computePlacementScore(
            GridPos parent,
            GridPos LayoutCandidate,
            Set<GridPos> occupied,
            int subtreeWeight,
            int distance
    ) {

        int score = 0;

        /*
         * Prefer shorter connectors.
         */
        score += distance * 15;

        /*
         * Penalize crowded areas.
         */
        for (GridPos pos : occupied) {

            int dx = pos.x() - LayoutCandidate.x();
            int dy = pos.y() - LayoutCandidate.y();

            int distSq = dx * dx + dy * dy;

            if (distSq <= 2) {
                score += 60;
            } else if (distSq <= 6) {
                score += 15;
            }
        }

        /*
         * Large subtrees prefer more space.
         */
        score -= subtreeWeight * 2;

        /*
         * Slight preference for horizontal progression.
         */
        score += Math.abs(LayoutCandidate.y()) * 3;

        return score;
    }

    @Unique
    private boolean cs$isEdgeRouteClear(
            GridPos from,
            GridPos to,
            Set<String> reservedEdges
    ) {

        List<String> route = cs$buildOrthogonalRoute(from, to);

        for (String segment : route) {
            if (reservedEdges.contains(segment)) {
                return false;
            }
        }

        return true;
    }

    @Unique
    private void cs$reserveEdgeRoute(
            GridPos from,
            GridPos to,
            Set<String> reservedEdges
    ) {

        reservedEdges.addAll(
                cs$buildOrthogonalRoute(from, to)
        );
    }

    /*
     * Orthogonal connector routing:
     *
     * horizontal first,
     * vertical second.
     */
    @Unique
    private List<String> cs$buildOrthogonalRoute(
            GridPos from,
            GridPos to
    ) {

        List<String> route = new ArrayList<>();

        int x = from.x();
        int y = from.y();

        while (x != to.x()) {

            int nextX = x + Integer.signum(to.x() - x);

            route.add(cs$segmentKey(x, y, nextX, y));

            x = nextX;
        }

        while (y != to.y()) {

            int nextY = y + Integer.signum(to.y() - y);

            route.add(cs$segmentKey(x, y, x, nextY));

            y = nextY;
        }

        return route;
    }

    @Unique
    private String cs$segmentKey(
            int x1,
            int y1,
            int x2,
            int y2
    ) {

        return x1 + "," + y1 + ":" + x2 + "," + y2;
    }

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