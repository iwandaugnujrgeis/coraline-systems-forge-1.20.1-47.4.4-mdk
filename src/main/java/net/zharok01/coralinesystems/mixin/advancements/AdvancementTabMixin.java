package net.zharok01.coralinesystems.mixin.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.zharok01.coralinesystems.client.advancements.GridPos;
import net.zharok01.coralinesystems.client.advancements.LayoutCandidate;
import net.zharok01.coralinesystems.client.advancements.RouteNode;
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

@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin {

    @Unique private static final int theCoralineSystems$RIGHT = 0;
    @Unique private static final int theCoralineSystems$DOWN  = 1;
    @Unique private static final int theCoralineSystems$LEFT  = 2;
    @Unique private static final int theCoralineSystems$UP    = 3;

    @Unique private static final int CS_SLOT_W    = 34;
    @Unique private static final int CS_SLOT_H    = 30;
    @Unique private static final int CS_WIDGET_SIZE = 28;

    @Shadow @Final private AdvancementWidget root;
    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;

    // ── FIX 1: dirty flag ────────────────────────────────────────────────────
    // Layout is expensive (full BFS graph traversal). We must never run it more
    // than once per "batch" of widget additions. The flag is set whenever the
    // tree changes and cleared the moment drawContents actually needs the result.
    @Unique private boolean cs$layoutDirty = false;

    // ── Connectivity Rendering ────────────────────────────────────────────────

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
            AdvancementWidget widget, GuiGraphics graphics, int x, int y, boolean shadow) {
        IAdvancementWidgetCS api = (IAdvancementWidgetCS) widget;
        api.drawConnectivityCS(graphics, x, y, true,  false);
        api.drawConnectivityCS(graphics, x, y, true,  true);
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
            AdvancementWidget widget, GuiGraphics graphics, int x, int y, boolean shadow) {
        IAdvancementWidgetCS api = (IAdvancementWidgetCS) widget;
        api.drawConnectivityCS(graphics, x, y, false, false);
        api.drawConnectivityCS(graphics, x, y, false, true);
    }

    // ── Layout Hooks ─────────────────────────────────────────────────────────

    /**
     * Fired at the TAIL of the 6-arg constructor (the one called from
     * {@link AdvancementTab#create}). The constructor itself calls addWidget()
     * for the root, so cs$afterAddWidget always fires first; this inject is kept
     * as a safety net for any future subclass paths.
     */
    @Inject(
            method = "<init>(Lnet/minecraft/client/Minecraft;" +
                    "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;" +
                    "Lnet/minecraft/client/gui/screens/advancements/AdvancementTabType;" +
                    "ILnet/minecraft/advancements/Advancement;" +
                    "Lnet/minecraft/advancements/DisplayInfo;)V",
            at = @At("TAIL")
    )
    private void cs$afterInit(CallbackInfo ci) {
        // Just mark dirty. The actual layout runs in cs$flushLayoutIfDirty,
        // deferred until drawContents is called for the first time.
        cs$layoutDirty = true;
    }

    /**
     * Fired every time a widget is added to the tab. With the dirty flag,
     * this is now O(1) — it no longer triggers a full graph traversal.
     */
    @Inject(method = "addWidget", at = @At("TAIL"))
    private void cs$afterAddWidget(
            AdvancementWidget widget, Advancement advancement, CallbackInfo ci) {
        cs$layoutDirty = true;
    }

    /**
     * Deferred layout flush — injected at the very top of drawContents so the
     * layout always runs before any rendering begins, but only runs at all when
     * the tree has actually changed since the last render.
     *
     * <p>This is the key performance fix: a tab with N advancements previously
     * triggered N full layout runs (one per addWidget call). Now it triggers
     * exactly one, on the frame the window first appears.
     */
    @Inject(method = "drawContents", at = @At("HEAD"))
    private void cs$flushLayoutIfDirty(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        if (cs$layoutDirty) {
            cs$layoutDirty = false;
            cs$relayout();
        }
    }

    // ── Main Layout ───────────────────────────────────────────────────────────

    @Unique
    private void cs$relayout() {
        if (this.root == null) return;

        Map<AdvancementWidget, Integer> subtreeWeights = new HashMap<>();
        cs$computeWeights(this.root, subtreeWeights);

        Map<AdvancementWidget, GridPos> positions = new HashMap<>();
        Set<GridPos>  occupied      = new HashSet<>();
        Set<String>   reservedEdges = new HashSet<>();

        GridPos rootPos = new GridPos(0, 0);
        positions.put(this.root, rootPos);
        occupied.add(rootPos);
        ((IAdvancementWidgetCS) this.root).setArrivalDir(-1);

        List<AdvancementWidget> children =
                new ArrayList<>(((IAdvancementWidgetCS) this.root).getChildren());
        children.sort((a, b) ->
                subtreeWeights.getOrDefault(b, 1) - subtreeWeights.getOrDefault(a, 1));

        for (AdvancementWidget child : children) {
            cs$placeNode(child, this.root, positions, occupied, reservedEdges, subtreeWeights);
        }

        int minGridX = positions.values().stream().mapToInt(GridPos::x).min().orElse(0);
        int minGridY = positions.values().stream().mapToInt(GridPos::y).min().orElse(0);

        this.minX = Integer.MAX_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;

        for (Map.Entry<AdvancementWidget, GridPos> entry : positions.entrySet()) {
            int px = (entry.getValue().x() - minGridX) * CS_SLOT_W;
            int py = (entry.getValue().y() - minGridY) * CS_SLOT_H;

            IAdvancementWidgetCS api = (IAdvancementWidgetCS) entry.getKey();
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
            Map<AdvancementWidget, Integer> subtreeWeights) {

        GridPos parentPos = positions.get(parent);

        LayoutCandidate bestCandidate = null;
        // FIX 2: cache the winning route from the scoring pass.
        // Previously cs$findRoute was called twice for the winner — once to score
        // it and once again after the loop to record it. For a tab with N nodes,
        // each with up to 32 candidates, that was up to 32 redundant BFS runs
        // per node. Now the winning route is retained and used directly.
        List<String> bestRoute = null;

        for (int direction : List.of(
                theCoralineSystems$RIGHT, theCoralineSystems$DOWN,
                theCoralineSystems$LEFT,  theCoralineSystems$UP)) {

            for (int distance = 1; distance <= 8; distance++) {

                int nx = parentPos.x();
                int ny = parentPos.y();
                switch (direction) {
                    case theCoralineSystems$RIGHT -> nx += distance;
                    case theCoralineSystems$LEFT  -> nx -= distance;
                    case theCoralineSystems$DOWN  -> ny += distance;
                    case theCoralineSystems$UP    -> ny -= distance;
                }

                GridPos candidatePos = new GridPos(nx, ny);
                if (occupied.contains(candidatePos)) continue;

                List<String> route = cs$findRoute(parentPos, candidatePos, reservedEdges);
                if (route == null) continue;

                int score = cs$computePlacementScore(
                        candidatePos, occupied,
                        subtreeWeights.getOrDefault(node, 1),
                        route.size());

                if (bestCandidate == null || score < bestCandidate.score()) {
                    bestCandidate = new LayoutCandidate(candidatePos, direction, score);
                    bestRoute = route; // ← retained; no second BFS needed
                }
            }
        }

        if (bestCandidate == null) return;

        // Commit the winner — reuse the cached route, no second cs$findRoute call.
        positions.put(node, bestCandidate.pos());
        occupied.add(bestCandidate.pos());
        reservedEdges.addAll(bestRoute);
        ((IAdvancementWidgetCS) node).setArrivalDir(bestCandidate.direction());

        List<AdvancementWidget> children =
                new ArrayList<>(((IAdvancementWidgetCS) node).getChildren());
        children.sort((a, b) ->
                subtreeWeights.getOrDefault(b, 1) - subtreeWeights.getOrDefault(a, 1));

        for (AdvancementWidget child : children) {
            cs$placeNode(child, node, positions, occupied, reservedEdges, subtreeWeights);
        }
    }

    // ── BFS Pathfinding ───────────────────────────────────────────────────────

    @Unique
    private List<String> cs$findRoute(
            GridPos from, GridPos to, Set<String> reservedEdges) {

        Queue<RouteNode> open = new LinkedList<>();
        Map<RouteNode, RouteNode> parentMap = new HashMap<>();

        RouteNode start  = new RouteNode(from.x(), from.y());
        RouteNode target = new RouteNode(to.x(),   to.y());

        open.add(start);
        parentMap.put(start, null);

        final int maxRadius = 32;

        while (!open.isEmpty()) {
            RouteNode current = open.poll();
            if (current.equals(target)) break;

            for (RouteNode next : List.of(
                    new RouteNode(current.x() + 1, current.y()),
                    new RouteNode(current.x() - 1, current.y()),
                    new RouteNode(current.x(),      current.y() + 1),
                    new RouteNode(current.x(),      current.y() - 1))) {

                if (parentMap.containsKey(next)) continue;
                if (Math.abs(next.x() - start.x()) > maxRadius ||
                        Math.abs(next.y() - start.y()) > maxRadius) continue;

                String segment = cs$segmentKey(
                        current.x(), current.y(), next.x(), next.y());
                if (reservedEdges.contains(segment)) continue;

                parentMap.put(next, current);
                open.add(next);
            }
        }

        if (!parentMap.containsKey(target)) return null;

        List<String> route = new ArrayList<>();
        RouteNode current = target;
        while (parentMap.get(current) != null) {
            RouteNode previous = parentMap.get(current);
            route.add(cs$segmentKey(previous.x(), previous.y(), current.x(), current.y()));
            current = previous;
        }
        Collections.reverse(route);
        return route;
    }

    // ── Placement Scoring ─────────────────────────────────────────────────────

    @Unique
    private int cs$computePlacementScore(
            GridPos candidate, Set<GridPos> occupied,
            int subtreeWeight, int routeLength) {

        int score = routeLength * 12;

        for (GridPos pos : occupied) {
            int dx    = pos.x() - candidate.x();
            int dy    = pos.y() - candidate.y();
            int distSq = dx * dx + dy * dy;
            if      (distSq <= 2) score += 60;
            else if (distSq <= 6) score += 15;
        }

        score -= subtreeWeight * 2;
        score += Math.abs(candidate.y()) * 3;
        return score;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    @Unique
    private String cs$segmentKey(int x1, int y1, int x2, int y2) {
        return x1 + "," + y1 + ":" + x2 + "," + y2;
    }

    @Unique
    private static int cs$computeWeights(
            AdvancementWidget node, Map<AdvancementWidget, Integer> cache) {
        int weight = 1;
        for (AdvancementWidget child : ((IAdvancementWidgetCS) node).getChildren()) {
            weight += cs$computeWeights(child, cache);
        }
        cache.put(node, weight);
        return weight;
    }
}