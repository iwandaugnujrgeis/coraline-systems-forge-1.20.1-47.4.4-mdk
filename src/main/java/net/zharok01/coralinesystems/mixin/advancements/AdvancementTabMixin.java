package net.zharok01.coralinesystems.mixin.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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

    @Unique private static final int CS_SLOT_W      = 34;
    @Unique private static final int CS_SLOT_H      = 30;
    @Unique private static final int CS_WIDGET_SIZE = 28;

    // Procedural Textures
    @Unique private static final ResourceLocation CS_TEX_BEDROCK  = new ResourceLocation("textures/block/bedrock.png");
    @Unique private static final ResourceLocation CS_TEX_DIRT     = new ResourceLocation("textures/block/dirt.png");
    @Unique private static final ResourceLocation CS_TEX_STONE    = new ResourceLocation("textures/block/stone.png");
    @Unique private static final ResourceLocation CS_TEX_COAL     = new ResourceLocation("textures/block/coal_ore.png");
    @Unique private static final ResourceLocation CS_TEX_IRON     = new ResourceLocation("textures/block/iron_ore.png");
    @Unique private static final ResourceLocation CS_TEX_GOLD     = new ResourceLocation("textures/block/gold_ore.png");
    @Unique private static final ResourceLocation CS_TEX_LAPIS    = new ResourceLocation("textures/block/lapis_ore.png");
    @Unique private static final ResourceLocation CS_TEX_REDSTONE = new ResourceLocation("textures/block/redstone_ore.png");
    @Unique private static final ResourceLocation CS_TEX_DIAMOND  = new ResourceLocation("textures/block/diamond_ore.png");
    @Unique private static final ResourceLocation CS_TEX_DEEPSLATE = new ResourceLocation("textures/block/deepslate.png");
    @Unique private static final ResourceLocation CS_TEX_GRANITE  = new ResourceLocation("textures/block/granite.png");
    @Unique private static final ResourceLocation CS_TEX_ANDESITE = new ResourceLocation("textures/block/andesite.png");
    @Unique private static final ResourceLocation CS_TEX_GRAVEL   = new ResourceLocation("textures/block/gravel.png");

    @Shadow @Final private AdvancementWidget root;
    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;
    @Shadow private double scrollX;
    @Shadow private double scrollY;

    @Unique private boolean cs$layoutDirty = false;

    // ── Coordinate Bit-Scrambler ──────────────────────────────────────────────

    /**
     * High-entropy 2D coordinate mixer to fully scatter linear bits and
     * eradicate diagonal banding/Marsaglia hyperplane patterns during world-gen simulation.
     */
    @Unique
    private long cs$mixCoordinates(int x, int y) {
        long hash = ((long) x * 312547891L) ^ ((long) y * 87541243L);
        hash = (hash ^ (hash >>> 25)) * 0x7a5bc2d3L;
        hash = (hash ^ (hash >>> 17)) * 0x14f5b2a3L;
        return hash ^ (hash >>> 13);
    }

    // ── Procedural Background Rework ──────────────────────────────────────────

    @Redirect(
            method = "drawContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"
            )
    )
    private void cs$proceduralBackground(
            GuiGraphics guiGraphics, ResourceLocation originalTexture,
            int x, int y, float uOffset, float vOffset,
            int width, int height, int textureWidth, int textureHeight) {

        int gridX = (x - Mth.floor(this.scrollX)) / 16;
        int gridY = (y - Mth.floor(this.scrollY)) / 16;

        int topBoundTile = 0;
        int bottomBoundTile = (this.maxY / 16);

        // Push the deepslate transition down to the lower 1/3 of the grid
        int deepslateBoundTile = topBoundTile + (int)((bottomBoundTile - topBoundTile) * 0.66f);

        // Generate independent jagged noise for all three stratum transitions
        long colSeed = cs$mixCoordinates(gridX, 0) + 57321L;
        Random colRandom = new Random(colSeed);
        int topOffset = colRandom.nextInt(3) - 1;
        int deepslateOffset = colRandom.nextInt(3) - 1;
        int bottomOffset = colRandom.nextInt(3) - 1;

        int noisyTop = topBoundTile + 1 + topOffset;
        int noisyDeepslate = deepslateBoundTile + deepslateOffset;
        int noisyBottom = bottomBoundTile + bottomOffset;

        ResourceLocation textureToDraw;

        if (gridY <= noisyTop) {
            textureToDraw = CS_TEX_DIRT;
        } else if (gridY >= noisyBottom) {
            textureToDraw = CS_TEX_BEDROCK;
        } else {
            float depthRatio = Mth.clamp((float) (gridY - noisyTop) / (float) (noisyBottom - noisyTop), 0.0f, 1.0f);

            // Establish the base layer: Stone at the top, Deepslate starting at the 66% mark
            ResourceLocation stoneBase = (gridY >= noisyDeepslate) ? CS_TEX_DEEPSLATE : CS_TEX_STONE;

            // 3a. Generate Simplified 5x5 Stone Blobs (Granite, Andesite, Gravel)
            int blobGridSize = 6;
            int regionX = Math.floorDiv(gridX, blobGridSize);
            int regionY = Math.floorDiv(gridY, blobGridSize);

            for (int rx = -1; rx <= 1; rx++) {
                for (int ry = -1; ry <= 1; ry++) {
                    int checkRegX = regionX + rx;
                    int checkRegY = regionY + ry;

                    long blobSeed = cs$mixCoordinates(checkRegX, checkRegY) + 987654321L;
                    Random blobRand = new Random(blobSeed);

                    if (blobRand.nextFloat() < 0.25f) {
                        ResourceLocation candidateBlobType;
                        float typeRoll = blobRand.nextFloat();
                        if (typeRoll < 0.35f) {
                            candidateBlobType = CS_TEX_GRANITE;
                        } else if (typeRoll < 0.70f) {
                            candidateBlobType = CS_TEX_ANDESITE;
                        } else {
                            candidateBlobType = CS_TEX_GRAVEL;
                        }

                        int centerX = checkRegX * blobGridSize + blobRand.nextInt(blobGridSize);
                        int centerY = checkRegY * blobGridSize + blobRand.nextInt(blobGridSize);

                        int dx = Math.abs(gridX - centerX);
                        int dy = Math.abs(gridY - centerY);

                        if (dx <= 2 && dy <= 2) {
                            if (!(dx == 2 && dy == 2)) {
                                long noiseSeed = cs$mixCoordinates(gridX, gridY) + blobSeed;
                                Random noiseRand = new Random(noiseSeed);

                                if (noiseRand.nextFloat() < 0.80f) {
                                    stoneBase = candidateBlobType;
                                }
                            }
                        }
                    }
                }
            }

            // 3b. Scatter Ore Minerals
            long cellSeed = cs$mixCoordinates(gridX, gridY) + 123456789L;
            Random random = new Random(cellSeed);
            int chance = random.nextInt(1000);

            if (chance < 5) {
                textureToDraw = (depthRatio >= 0.8f) ? CS_TEX_DIAMOND : stoneBase;
            } else if (chance < 12) {
                textureToDraw = (depthRatio >= 0.6f) ? CS_TEX_REDSTONE : stoneBase;
            } else if (chance < 20) {
                textureToDraw = (depthRatio >= 0.5f) ? CS_TEX_LAPIS : stoneBase;
            } else if (chance < 32) {
                textureToDraw = (depthRatio >= 0.4f) ? CS_TEX_GOLD : stoneBase;
            } else if (chance < 44) {
                textureToDraw = (depthRatio >= 0.2f) ? CS_TEX_IRON : stoneBase;
            } else if (chance < 68) {
                if (random.nextFloat() >= depthRatio) {
                    textureToDraw = CS_TEX_COAL;
                } else {
                    textureToDraw = stoneBase;
                }
            } else {
                textureToDraw = stoneBase;
            }
        }

        // 4. Dynamic Gradient Map
        float gradientRatio = Mth.clamp((float) (gridY - topBoundTile) / (float) (bottomBoundTile - topBoundTile), 0.0f, 1.0f);
        float brightness = 0.7f - (0.2f * gradientRatio);

        guiGraphics.setColor(brightness, brightness, brightness, 1.0F);
        guiGraphics.blit(textureToDraw, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

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

    @Inject(
            method = "<init>(Lnet/minecraft/client/Minecraft;" +
                    "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;" +
                    "Lnet/minecraft/client/gui/screens/advancements/AdvancementTabType;" +
                    "ILnet/minecraft/advancements/Advancement;" +
                    "Lnet/minecraft/advancements/DisplayInfo;)V",
            at = @At("TAIL")
    )
    private void cs$afterInit(CallbackInfo ci) {
        cs$layoutDirty = true;
    }

    @Inject(method = "addWidget", at = @At("TAIL"))
    private void cs$afterAddWidget(
            AdvancementWidget widget, Advancement advancement, CallbackInfo ci) {
        cs$layoutDirty = true;
    }

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
        Set<GridPos> occupied = new HashSet<>();
        Map<AdvancementWidget, List<GridPos>> committedRoutes = new LinkedHashMap<>();

        GridPos rootPos = new GridPos(0, 0);
        positions.put(this.root, rootPos);
        occupied.add(rootPos);

        IAdvancementWidgetCS rootApi = (IAdvancementWidgetCS) this.root;
        rootApi.setArrivalDir(-1);
        rootApi.setIncomingRoute(null);

        List<AdvancementWidget> children =
                new ArrayList<>(((IAdvancementWidgetCS) this.root).getChildren());
        children.sort((a, b) ->
                subtreeWeights.getOrDefault(b, 1) - subtreeWeights.getOrDefault(a, 1));

        for (AdvancementWidget child : children) {
            cs$placeNode(child, this.root, positions, occupied,
                    committedRoutes, subtreeWeights);
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

            List<GridPos> rawRoute = api.getIncomingRoute();
            if (rawRoute != null) {
                List<GridPos> normalizedRoute = new ArrayList<>(rawRoute.size());
                for (GridPos cell : rawRoute) {
                    normalizedRoute.add(new GridPos(cell.x() - minGridX, cell.y() - minGridY));
                }
                api.setIncomingRoute(normalizedRoute);
            }

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
            Map<AdvancementWidget, List<GridPos>> committedRoutes,
            Map<AdvancementWidget, Integer> subtreeWeights) {

        GridPos parentPos = positions.get(parent);

        LayoutCandidate bestCandidate = null;
        List<GridPos>   bestRoute     = null;

        // ── Phase 1: Normal placement ─────────────────────────────────────────
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

                List<GridPos> route = cs$findRoute(parentPos, candidatePos, occupied);
                if (route == null) continue;

                int score = cs$computePlacementScore(
                        parentPos, candidatePos, occupied,
                        subtreeWeights.getOrDefault(node, 1),
                        route.size());

                if (bestCandidate == null || score < bestCandidate.score()) {
                    bestCandidate = new LayoutCandidate(candidatePos, direction, score);
                    bestRoute     = route;
                }
            }
        }

        // ── Phase 2: Branch-off fallback ──────────────────────────────────────
        if (bestCandidate == null) {
            bestCandidate = null;
            bestRoute     = null;

            for (Map.Entry<AdvancementWidget, List<GridPos>> entry : committedRoutes.entrySet()) {
                AdvancementWidget sibling = entry.getKey();

                if (((IAdvancementWidgetCS) sibling).getParentWidget() != parent) {
                    continue;
                }

                List<GridPos> siblingRoute = entry.getValue();

                for (int idx = 0; idx < siblingRoute.size() - 1; idx++) {
                    GridPos branchCell = siblingRoute.get(idx);

                    for (int direction : List.of(
                            theCoralineSystems$RIGHT, theCoralineSystems$DOWN,
                            theCoralineSystems$LEFT,  theCoralineSystems$UP)) {

                        for (int distance = 1; distance <= 8; distance++) {
                            int nx = branchCell.x();
                            int ny = branchCell.y();
                            switch (direction) {
                                case theCoralineSystems$RIGHT -> nx += distance;
                                case theCoralineSystems$LEFT  -> nx -= distance;
                                case theCoralineSystems$DOWN  -> ny += distance;
                                case theCoralineSystems$UP    -> ny -= distance;
                            }

                            GridPos candidatePos = new GridPos(nx, ny);
                            if (occupied.contains(candidatePos)) continue;

                            List<GridPos> tailRoute =
                                    cs$findRoute(branchCell, candidatePos, occupied);
                            if (tailRoute == null) continue;

                            List<GridPos> fullRoute = new ArrayList<>();
                            for (int s = 0; s <= idx; s++) {
                                fullRoute.add(siblingRoute.get(s));
                            }
                            fullRoute.addAll(tailRoute);

                            int score = cs$computePlacementScore(
                                    parentPos, candidatePos, occupied,
                                    subtreeWeights.getOrDefault(node, 1),
                                    fullRoute.size());

                            score += (idx * 8);

                            if (bestCandidate == null || score < bestCandidate.score()) {
                                bestCandidate = new LayoutCandidate(
                                        candidatePos, direction, score);
                                bestRoute = fullRoute;
                            }
                        }
                    }
                }
            }
        }

        if (bestCandidate == null) return;

        // ── Commit ────────────────────────────────────────────────────────────
        positions.put(node, bestCandidate.pos());
        occupied.addAll(bestRoute);

        IAdvancementWidgetCS nodeApi = (IAdvancementWidgetCS) node;
        nodeApi.setArrivalDir(bestCandidate.direction());
        nodeApi.setIncomingRoute(bestRoute);

        committedRoutes.put(node, bestRoute);

        List<AdvancementWidget> children =
                new ArrayList<>(((IAdvancementWidgetCS) node).getChildren());
        children.sort((a, b) ->
                subtreeWeights.getOrDefault(b, 1) - subtreeWeights.getOrDefault(a, 1));

        for (AdvancementWidget child : children) {
            cs$placeNode(child, node, positions, occupied,
                    committedRoutes, subtreeWeights);
        }
    }

    // ── BFS Pathfinding ───────────────────────────────────────────────────────

    @Unique
    private List<GridPos> cs$findRoute(
            GridPos from, GridPos to, Set<GridPos> occupied) {

        int minX = Math.min(from.x(), to.x()) - 3;
        int maxX = Math.max(from.x(), to.x()) + 3;
        int minY = Math.min(from.y(), to.y()) - 3;
        int maxY = Math.max(from.y(), to.y()) + 3;

        RouteNode start  = new RouteNode(from.x(), from.y());
        RouteNode target = new RouteNode(to.x(),   to.y());

        Queue<RouteNode>            open      = new LinkedList<>();
        Map<RouteNode, RouteNode>   parentMap = new HashMap<>();

        open.add(start);
        parentMap.put(start, null);

        while (!open.isEmpty()) {
            RouteNode current = open.poll();
            if (current.equals(target)) break;

            for (RouteNode next : List.of(
                    new RouteNode(current.x() + 1, current.y()),
                    new RouteNode(current.x() - 1, current.y()),
                    new RouteNode(current.x(),      current.y() + 1),
                    new RouteNode(current.x(),      current.y() - 1))) {

                if (parentMap.containsKey(next)) continue;

                if (next.x() < minX || next.x() > maxX ||
                        next.y() < minY || next.y() > maxY) continue;

                GridPos nextPos = new GridPos(next.x(), next.y());
                boolean isDestination = next.equals(target);
                if (!isDestination && occupied.contains(nextPos)) continue;

                parentMap.put(next, current);
                open.add(next);
            }
        }

        if (!parentMap.containsKey(target)) return null;

        List<GridPos> route = new ArrayList<>();
        RouteNode current = target;
        while (parentMap.get(current) != null) {
            route.add(new GridPos(current.x(), current.y()));
            current = parentMap.get(current);
        }
        Collections.reverse(route);
        return route;
    }

    // ── Placement Scoring ─────────────────────────────────────────────────────

    @Unique
    private int cs$computePlacementScore(
            GridPos parentPos, GridPos candidate, Set<GridPos> occupied,
            int subtreeWeight, int routeLength) {

        int score = routeLength * 12;

        for (GridPos pos : occupied) {
            int dx     = pos.x() - candidate.x();
            int dy     = pos.y() - candidate.y();
            int distSq = dx * dx + dy * dy;
            if      (distSq <= 2) score += 60;
            else if (distSq <= 6) score += 15;
        }

        int pdx = candidate.x() - parentPos.x();
        int pdy = candidate.y() - parentPos.y();
        score += (pdx * pdx + pdy * pdy) * 5;

        score -= subtreeWeight * 2;
        score += Math.abs(candidate.y()) * 3;
        return score;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

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