package net.zharok01.coralinesystems.mixin.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.zharok01.coralinesystems.client.advancement.AdvancementBackdrop;
import net.zharok01.coralinesystems.client.advancement.GridDirection;
import net.zharok01.coralinesystems.client.advancement.GridPos;
import net.zharok01.coralinesystems.client.advancement.LayoutCandidate;
import net.zharok01.coralinesystems.client.advancement.PlacementContext;
import net.zharok01.coralinesystems.util.interfaces.IAdvancementWidget;
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

    @Unique private static final int MIN_HOP = 2;
    @Unique private static final int OUTLET_DEPTH = 3;

    @Unique private static final int WIDGET_SPAN = 28;
    @Unique private static final int VIEW_WIDTH = 234;
    @Unique private static final int VIEW_HEIGHT = 113;

    @Unique private static final float BACKDROP_TOP_BRIGHTNESS = 0.5F;
    @Unique private static final float BACKDROP_BOTTOM_BRIGHTNESS = 0.3F;

    @Unique private static final GridDirection[] ALL_DIRECTIONS = GridDirection.values();
    @Unique private static final GridDirection[] BFS_ORDER = {
            GridDirection.RIGHT, GridDirection.LEFT, GridDirection.DOWN, GridDirection.UP};

    @Unique private static final Map<ResourceLocation, double[]> coraline$savedScroll = new HashMap<>();

    @Shadow @Final private Advancement advancement;
    @Shadow @Final private AdvancementWidget root;
    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;
    @Shadow private double scrollX;
    @Shadow private double scrollY;
    @Shadow private boolean centered;

    @Unique private boolean coraline$layoutDirty = true;

    @Unique private final Map<AdvancementWidget, List<GridDirection>> coraline$reservedOutlets = new HashMap<>();
    @Unique private final Map<AdvancementWidget, GridDirection> coraline$arrivalDirections = new HashMap<>();

    @Redirect(
            method = "drawContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"
            )
    )
    private void coraline$drawProceduralTile(
            GuiGraphics guiGraphics, ResourceLocation atlasLocation,
            int x, int y, float uOffset, float vOffset,
            int width, int height, int textureWidth, int textureHeight) {

        int gridX = (x - Mth.floor(this.scrollX)) / 16;
        int gridY = (y - Mth.floor(this.scrollY)) / 16;
        int bottomTile = this.maxY / 16;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(AdvancementBackdrop.pickTileSprite(gridX, gridY, bottomTile));

        float depth = Mth.clamp(gridY / (float) bottomTile, 0.0F, 1.0F);
        float brightness = Mth.lerp(depth, BACKDROP_TOP_BRIGHTNESS, BACKDROP_BOTTOM_BRIGHTNESS);

        guiGraphics.setColor(brightness, brightness, brightness, 1.0F);
        guiGraphics.blit(x, y, 0, width, height, sprite);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Inject(method = "addWidget", at = @At("TAIL"))
    private void coraline$markLayoutDirty(AdvancementWidget widget, Advancement advancement, CallbackInfo ci) {
        this.coraline$layoutDirty = true;
    }

    @Inject(method = "drawContents", at = @At("HEAD"))
    private void coraline$prepareContents(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        if (this.coraline$layoutDirty) {
            this.coraline$layoutDirty = false;
            this.coraline$relayout();
        }

        double[] saved = coraline$savedScroll.get(this.advancement.getId());
        if (!this.centered && saved != null) {
            boolean overflowsX = this.maxX - this.minX > VIEW_WIDTH;
            boolean overflowsY = this.maxY - this.minY > VIEW_HEIGHT;

            this.scrollX = overflowsX
                    ? Mth.clamp(saved[0], -(this.maxX - VIEW_WIDTH), 0.0)
                    : VIEW_WIDTH / 2 - (this.maxX + this.minX) / 2;
            this.scrollY = overflowsY
                    ? Mth.clamp(saved[1], -(this.maxY - VIEW_HEIGHT), 0.0)
                    : VIEW_HEIGHT / 2 - (this.maxY + this.minY) / 2;
            this.centered = true;
        }
    }

    @Inject(method = "drawContents", at = @At("TAIL"))
    private void coraline$rememberScroll(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        double[] saved = coraline$savedScroll.computeIfAbsent(this.advancement.getId(), id -> new double[2]);
        saved[0] = this.scrollX;
        saved[1] = this.scrollY;
    }

    @Unique
    private void coraline$relayout() {
        Map<AdvancementWidget, Integer> subtreeWeights = new HashMap<>();
        coraline$computeWeights(this.root, subtreeWeights);

        Map<AdvancementWidget, GridPos> positions = new HashMap<>();
        Set<GridPos> occupied = new HashSet<>();
        Map<AdvancementWidget, List<GridPos>> committedRoutes = new LinkedHashMap<>();

        GridPos rootPos = new GridPos(0, 0);
        positions.put(this.root, rootPos);
        occupied.add(rootPos);

        this.coraline$arrivalDirections.clear();
        this.coraline$reservedOutlets.clear();
        this.coraline$reserveOutlets(this.root, positions, occupied);
        this.coraline$placeChildren(this.root, positions, occupied, committedRoutes, subtreeWeights);

        this.coraline$applyLayout(positions, committedRoutes);
    }

    @Unique
    private void coraline$applyLayout(
            Map<AdvancementWidget, GridPos> positions,
            Map<AdvancementWidget, List<GridPos>> committedRoutes) {

        int originX = positions.values().stream().mapToInt(GridPos::x).min().getAsInt();
        int originY = positions.values().stream().mapToInt(GridPos::y).min().getAsInt();

        this.minX = Integer.MAX_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;

        for (Map.Entry<AdvancementWidget, GridPos> entry : positions.entrySet()) {
            IAdvancementWidget widget = (IAdvancementWidget) entry.getKey();
            int pixelX = (entry.getValue().x() - originX) * GridPos.CELL_WIDTH;
            int pixelY = (entry.getValue().y() - originY) * GridPos.CELL_HEIGHT;
            widget.setX(pixelX);
            widget.setY(pixelY);

            List<GridPos> route = committedRoutes.get(entry.getKey());
            if (route != null) {
                widget.setIncomingRoute(route.stream()
                        .map(cell -> new GridPos(cell.x() - originX, cell.y() - originY))
                        .toList());
            }

            this.minX = Math.min(this.minX, pixelX);
            this.maxX = Math.max(this.maxX, pixelX + WIDGET_SPAN);
            this.minY = Math.min(this.minY, pixelY);
            this.maxY = Math.max(this.maxY, pixelY + WIDGET_SPAN);
        }
    }

    @Unique
    private void coraline$placeChildren(
            AdvancementWidget parent,
            Map<AdvancementWidget, GridPos> positions,
            Set<GridPos> occupied,
            Map<AdvancementWidget, List<GridPos>> committedRoutes,
            Map<AdvancementWidget, Integer> subtreeWeights) {

        List<AdvancementWidget> children = new ArrayList<>(((IAdvancementWidget) parent).getChildren());
        children.sort((a, b) -> Integer.compare(subtreeWeights.get(b), subtreeWeights.get(a)));

        for (AdvancementWidget child : children) {
            this.coraline$placeNode(child, parent, positions, occupied, committedRoutes, subtreeWeights);
        }
        this.coraline$reservedOutlets.remove(parent);

        for (AdvancementWidget child : children) {
            this.coraline$placeChildren(child, positions, occupied, committedRoutes, subtreeWeights);
        }
    }

    @Unique
    private void coraline$placeNode(
            AdvancementWidget node,
            AdvancementWidget parent,
            Map<AdvancementWidget, GridPos> positions,
            Set<GridPos> occupied,
            Map<AdvancementWidget, List<GridPos>> committedRoutes,
            Map<AdvancementWidget, Integer> subtreeWeights) {

        PlacementContext context = new PlacementContext();
        context.parent = parent;
        context.subtreeWeight = subtreeWeights.get(node);
        int blossomRadius = coraline$blossomRadius(context.subtreeWeight);

        int retries = 0;
        while (true) {
            context.parentPos = positions.get(parent);
            context.rootPos = positions.get(this.root);
            context.nodeCells = new HashSet<>(positions.values());
            context.bounds = coraline$boundsOf(occupied);

            List<GridDirection> directions = new ArrayList<>(Arrays.asList(ALL_DIRECTIONS));
            directions.sort(Comparator
                    .comparingInt((GridDirection direction) -> coraline$countOpenCone(direction, context.parentPos, blossomRadius, occupied))
                    .reversed());

            this.coraline$searchCandidates(context, directions, positions, occupied, committedRoutes);
            if (context.best != null) break;

            if (retries > 10) {
                GridPos fallback = context.parentPos.step(GridDirection.DOWN, 1);
                context.best = new LayoutCandidate(fallback, List.of(fallback), 0);
                break;
            }

            GridDirection spliceDirection = retries == 0
                    ? Collections.min(directions, Comparator.comparingInt((GridDirection direction) ->
                    coraline$countStretchedLinks(context.parentPos, direction, positions, committedRoutes)))
                    : directions.get(retries % directions.size());
            coraline$spliceGrid(positions, committedRoutes, context.parentPos, spliceDirection);

            occupied.clear();
            occupied.addAll(positions.values());
            committedRoutes.values().forEach(occupied::addAll);
            retries++;
        }

        positions.put(node, context.best.pos());
        occupied.addAll(context.best.route());
        committedRoutes.put(node, context.best.route());
        this.coraline$arrivalDirections.put(node, coraline$arrivalDirection(context.parentPos, context.best.route()));
        this.coraline$reserveOutlets(node, positions, occupied);
    }

    @Unique
    private void coraline$searchCandidates(
            PlacementContext context,
            List<GridDirection> directions,
            Map<AdvancementWidget, GridPos> positions,
            Set<GridPos> occupied,
            Map<AdvancementWidget, List<GridPos>> committedRoutes) {

        for (int pass = 0; pass < 2 && context.best == null; pass++) {
            context.blocked = new HashSet<>(occupied);
            if (pass == 0) {
                context.blocked.addAll(this.coraline$reservedCellsExcept(context.parent, positions));
            } else if (this.coraline$reservedOutlets.isEmpty()) {
                return;
            }

            coraline$tryHops(context);
            if (context.best == null) coraline$tryBranchOffSiblings(context, committedRoutes);
            if (context.best == null) coraline$tryDetours(context, directions);
        }
    }

    @Unique
    private void coraline$tryHops(PlacementContext context) {
        GridPos parentPos = context.parentPos;

        for (int dx = 3; dx >= -3; dx--) {
            for (int dy = 3; dy >= -3; dy--) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) < MIN_HOP || Math.abs(dx) + Math.abs(dy) > 4) continue;

                GridPos candidatePos = new GridPos(parentPos.x() + dx, parentPos.y() + dy);
                if (context.blocked.contains(candidatePos)) continue;

                for (boolean horizontalFirst : new boolean[]{true, false}) {
                    List<GridPos> route = coraline$findElbowRoute(parentPos, candidatePos, context.blocked, horizontalFirst);
                    if (route != null) coraline$offer(context, candidatePos, route, 0);
                }
            }
        }
    }

    @Unique
    private void coraline$tryBranchOffSiblings(
            PlacementContext context, Map<AdvancementWidget, List<GridPos>> committedRoutes) {

        for (Map.Entry<AdvancementWidget, List<GridPos>> entry : committedRoutes.entrySet()) {
            if (((IAdvancementWidget) entry.getKey()).getParentWidget() != context.parent) continue;

            List<GridPos> siblingRoute = entry.getValue();

            for (int index = 0; index < siblingRoute.size() - 1; index++) {
                GridPos branchCell = siblingRoute.get(index);

                for (GridDirection direction : ALL_DIRECTIONS) {
                    for (int distance = MIN_HOP; distance <= 4; distance++) {
                        GridPos candidatePos = branchCell.step(direction, distance);
                        if (context.blocked.contains(candidatePos)) continue;

                        List<GridPos> tailRoute = coraline$findRoute(branchCell, candidatePos, context.blocked);
                        if (tailRoute == null) continue;

                        List<GridPos> fullRoute = new ArrayList<>(siblingRoute.subList(0, index + 1));
                        fullRoute.addAll(tailRoute);

                        coraline$offer(context, candidatePos, fullRoute, index * 8);
                    }
                }
            }
        }
    }

    @Unique
    private void coraline$tryDetours(PlacementContext context, List<GridDirection> directions) {
        for (GridDirection direction : directions) {
            for (int distance = MIN_HOP; distance <= 4; distance++) {
                GridPos candidatePos = context.parentPos.step(direction, distance);
                if (context.blocked.contains(candidatePos)) continue;

                List<GridPos> route = coraline$findRoute(context.parentPos, candidatePos, context.blocked);
                if (route != null) coraline$offer(context, candidatePos, route, 0);
            }
        }
    }

    @Unique
    private void coraline$offer(PlacementContext context, GridPos candidatePos, List<GridPos> route, int extraCost) {
        int score = coraline$scorePlacement(context, candidatePos, route) + extraCost;
        if (context.best == null || score < context.best.score()) {
            context.best = new LayoutCandidate(candidatePos, route, score);
        }
    }

    @Unique
    private void coraline$reserveOutlets(
            AdvancementWidget node, Map<AdvancementWidget, GridPos> positions, Set<GridPos> occupied) {

        int childCount = ((IAdvancementWidget) node).getChildren().size();
        GridPos pos = positions.get(node);
        Set<GridPos> taken = this.coraline$reservedCellsExcept(node, positions);
        GridDirection arrival = this.coraline$arrivalDirections.get(node);

        List<GridDirection> openDirections = new ArrayList<>();
        for (GridDirection direction : ALL_DIRECTIONS) {
            boolean open = true;
            for (int distance = 1; distance <= OUTLET_DEPTH && open; distance++) {
                GridPos cell = pos.step(direction, distance);
                open = !occupied.contains(cell) && !taken.contains(cell);
            }
            if (open) openDirections.add(direction);
        }

        int radius = coraline$blossomRadius(childCount + 1);
        openDirections.sort(Comparator
                .comparingInt((GridDirection direction) ->
                        coraline$countOpenCone(direction, pos, radius, occupied) + (direction == arrival ? 3 : 0))
                .reversed());

        int outletCount = Math.min(openDirections.size(), Math.min(childCount, 3));
        if (outletCount > 0) {
            this.coraline$reservedOutlets.put(node, List.copyOf(openDirections.subList(0, outletCount)));
        }
    }

    @Unique
    private Set<GridPos> coraline$reservedCellsExcept(
            AdvancementWidget except, Map<AdvancementWidget, GridPos> positions) {

        Set<GridPos> cells = new HashSet<>();
        for (Map.Entry<AdvancementWidget, List<GridDirection>> entry : this.coraline$reservedOutlets.entrySet()) {
            if (entry.getKey() == except) continue;

            GridPos pos = positions.get(entry.getKey());
            for (GridDirection direction : entry.getValue()) {
                for (int distance = 1; distance <= OUTLET_DEPTH; distance++) {
                    cells.add(pos.step(direction, distance));
                }
            }
        }
        return cells;
    }

    @Unique
    private static int coraline$countStretchedLinks(
            GridPos origin, GridDirection direction,
            Map<AdvancementWidget, GridPos> positions,
            Map<AdvancementWidget, List<GridPos>> committedRoutes) {

        int stretched = 0;
        int originAlong = direction.along(origin);
        for (Map.Entry<AdvancementWidget, List<GridPos>> entry : committedRoutes.entrySet()) {
            GridPos previous = positions.get(((IAdvancementWidget) entry.getKey()).getParentWidget());
            for (GridPos cell : entry.getValue()) {
                int from = direction.along(previous);
                int to = direction.along(cell);
                if (Math.min(from, to) <= originAlong && Math.max(from, to) > originAlong) stretched++;
                previous = cell;
            }
        }
        return stretched;
    }

    @Unique
    private static void coraline$spliceGrid(
            Map<AdvancementWidget, GridPos> positions,
            Map<AdvancementWidget, List<GridPos>> committedRoutes,
            GridPos origin, GridDirection direction) {

        positions.replaceAll((widget, pos) -> coraline$shiftPastOrigin(pos, origin, direction));

        for (Map.Entry<AdvancementWidget, List<GridPos>> entry : committedRoutes.entrySet()) {
            GridPos previous = positions.get(((IAdvancementWidget) entry.getKey()).getParentWidget());
            List<GridPos> newRoute = new ArrayList<>();

            for (GridPos oldCell : entry.getValue()) {
                GridPos shifted = coraline$shiftPastOrigin(oldCell, origin, direction);

                if (Math.abs(previous.x() - shifted.x()) + Math.abs(previous.y() - shifted.y()) > 1) {
                    newRoute.add(new GridPos((previous.x() + shifted.x()) / 2, (previous.y() + shifted.y()) / 2));
                }
                newRoute.add(shifted);
                previous = shifted;
            }
            entry.setValue(newRoute);
        }
    }

    @Unique
    private static GridPos coraline$shiftPastOrigin(GridPos pos, GridPos origin, GridDirection direction) {
        return direction.along(pos) > direction.along(origin) ? pos.step(direction, 1) : pos;
    }

    @Unique
    private static List<GridPos> coraline$findRoute(GridPos from, GridPos to, Set<GridPos> blocked) {
        int minX = Math.min(from.x(), to.x()) - 3;
        int maxX = Math.max(from.x(), to.x()) + 3;
        int minY = Math.min(from.y(), to.y()) - 3;
        int maxY = Math.max(from.y(), to.y()) + 3;

        Queue<GridPos> open = new ArrayDeque<>();
        Map<GridPos, GridPos> cameFrom = new HashMap<>();
        open.add(from);
        cameFrom.put(from, null);

        while (!open.isEmpty()) {
            GridPos current = open.poll();
            if (current.equals(to)) break;

            for (GridDirection direction : BFS_ORDER) {
                GridPos next = current.step(direction, 1);
                if (cameFrom.containsKey(next)) continue;
                if (next.x() < minX || next.x() > maxX || next.y() < minY || next.y() > maxY) continue;
                if (!next.equals(to) && blocked.contains(next)) continue;

                cameFrom.put(next, current);
                open.add(next);
            }
        }

        if (!cameFrom.containsKey(to)) return null;

        List<GridPos> route = new ArrayList<>();
        for (GridPos cell = to; cameFrom.get(cell) != null; cell = cameFrom.get(cell)) {
            route.add(cell);
        }
        Collections.reverse(route);
        return route;
    }

    @Unique
    private static List<GridPos> coraline$findElbowRoute(
            GridPos from, GridPos to, Set<GridPos> blocked, boolean horizontalFirst) {

        int firstLeg = horizontalFirst ? Math.abs(to.x() - from.x()) : Math.abs(to.y() - from.y());
        if (firstLeg < MIN_HOP) return null;

        List<GridPos> route = new ArrayList<>();
        int x = from.x();
        int y = from.y();
        int stepX = Integer.signum(to.x() - from.x());
        int stepY = Integer.signum(to.y() - from.y());

        for (int leg = 0; leg < 2; leg++) {
            boolean horizontalLeg = (leg == 0) == horizontalFirst;
            while (horizontalLeg ? x != to.x() : y != to.y()) {
                if (horizontalLeg) {
                    x += stepX;
                } else {
                    y += stepY;
                }
                GridPos cell = new GridPos(x, y);
                if (blocked.contains(cell)) return null;
                route.add(cell);
            }
        }
        return route;
    }

    @Unique
    private int coraline$scorePlacement(PlacementContext context, GridPos candidate, List<GridPos> route) {
        GridPos parentPos = context.parentPos;
        int score = route.size() * 10;

        int turns = coraline$countTurns(parentPos, route);
        score += turns == 1 ? -4 : turns * 14;

        for (GridPos pos : context.nodeCells) {
            if (pos.equals(parentPos)) continue;
            int dx = pos.x() - candidate.x();
            int dy = pos.y() - candidate.y();
            int distanceSquared = dx * dx + dy * dy;
            if (distanceSquared <= 2) {
                score += 80;
            } else if (distanceSquared <= 4) {
                score += 25;
            } else if (distanceSquared <= 8) {
                score += 8;
            }
        }

        for (int i = 0; i < route.size() - 1; i++) {
            for (GridDirection direction : ALL_DIRECTIONS) {
                GridPos neighbour = route.get(i).step(direction, 1);
                if (context.nodeCells.contains(neighbour) && !neighbour.equals(parentPos)) score += 14;
            }
        }

        if (context.parent != this.root) {
            int rootDistance = Math.max(
                    Math.abs(candidate.x() - context.rootPos.x()),
                    Math.abs(candidate.y() - context.rootPos.y()));
            if (rootDistance <= 2) score += 60;
        }

        int roomRadius = Math.min(3, coraline$blossomRadius(context.subtreeWeight) + 1);
        int crowdedCells = 0;
        for (int rx = -roomRadius; rx <= roomRadius; rx++) {
            for (int ry = -roomRadius; ry <= roomRadius; ry++) {
                if (context.blocked.contains(new GridPos(candidate.x() + rx, candidate.y() + ry))) crowdedCells++;
            }
        }
        score += crowdedCells * 2;

        int[] bounds = context.bounds;
        int growX = Math.max(0, bounds[0] - candidate.x()) + Math.max(0, candidate.x() - bounds[1]);
        int growY = Math.max(0, bounds[2] - candidate.y()) + Math.max(0, candidate.y() - bounds[3]);
        score += growX * 13 + growY * 12;

        GridDirection parentArrival = this.coraline$arrivalDirections.get(context.parent);
        if (parentArrival != null) {
            int progress = parentArrival.along(candidate) - parentArrival.along(parentPos);
            score -= Math.max(0, progress) * 3;
        }
        return score;
    }

    @Unique
    private static int coraline$countTurns(GridPos start, List<GridPos> route) {
        int turns = 0;
        int previousDx = 0;
        int previousDy = 0;
        GridPos previous = start;

        for (GridPos cell : route) {
            int dx = cell.x() - previous.x();
            int dy = cell.y() - previous.y();
            if ((previousDx != 0 || previousDy != 0) && (dx != previousDx || dy != previousDy)) {
                turns++;
            }
            previousDx = dx;
            previousDy = dy;
            previous = cell;
        }
        return turns;
    }

    @Unique
    private static GridDirection coraline$arrivalDirection(GridPos start, List<GridPos> route) {
        GridPos last = route.get(route.size() - 1);
        GridPos beforeLast = route.size() > 1 ? route.get(route.size() - 2) : start;

        if (last.x() > beforeLast.x()) return GridDirection.RIGHT;
        if (last.x() < beforeLast.x()) return GridDirection.LEFT;
        return last.y() > beforeLast.y() ? GridDirection.DOWN : GridDirection.UP;
    }

    @Unique
    private static int[] coraline$boundsOf(Set<GridPos> cells) {
        int[] bounds = {Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (GridPos cell : cells) {
            bounds[0] = Math.min(bounds[0], cell.x());
            bounds[1] = Math.max(bounds[1], cell.x());
            bounds[2] = Math.min(bounds[2], cell.y());
            bounds[3] = Math.max(bounds[3], cell.y());
        }
        return bounds;
    }

    @Unique
    private static int coraline$blossomRadius(int weight) {
        return (int) Math.ceil(Math.sqrt(weight));
    }

    @Unique
    private static int coraline$countOpenCone(
            GridDirection direction, GridPos origin, int radius, Set<GridPos> occupied) {

        boolean horizontal = direction.dx != 0;
        int free = 0;
        for (int along = 1; along <= radius; along++) {
            for (int sideways = -radius; sideways <= radius; sideways++) {
                int cellX = horizontal ? origin.x() + along * direction.dx : origin.x() + sideways;
                int cellY = horizontal ? origin.y() + sideways : origin.y() + along * direction.dy;
                if (!occupied.contains(new GridPos(cellX, cellY))) free++;
            }
        }
        return free;
    }

    @Unique
    private static int coraline$computeWeights(AdvancementWidget node, Map<AdvancementWidget, Integer> weights) {
        int weight = 1;
        for (AdvancementWidget child : ((IAdvancementWidget) node).getChildren()) {
            weight += coraline$computeWeights(child, weights);
        }
        weights.put(node, weight);
        return weight;
    }
}