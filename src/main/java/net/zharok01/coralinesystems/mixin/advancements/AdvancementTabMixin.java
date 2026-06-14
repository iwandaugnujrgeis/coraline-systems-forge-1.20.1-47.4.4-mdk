package net.zharok01.coralinesystems.mixin.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
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

    // Procedural Textures (Now pointing to Atlas Sprite Names)
    @Unique private static final ResourceLocation CS_SPRITE_BEDROCK  = new ResourceLocation("minecraft", "block/bedrock");
    @Unique private static final ResourceLocation CS_SPRITE_DIRT     = new ResourceLocation("minecraft", "block/dirt");
    @Unique private static final ResourceLocation CS_SPRITE_STONE    = new ResourceLocation("minecraft", "block/stone");
    @Unique private static final ResourceLocation CS_SPRITE_COAL     = new ResourceLocation("minecraft", "block/coal_ore");
    @Unique private static final ResourceLocation CS_SPRITE_IRON     = new ResourceLocation("minecraft", "block/iron_ore");
    @Unique private static final ResourceLocation CS_SPRITE_GOLD     = new ResourceLocation("minecraft", "block/gold_ore");
    @Unique private static final ResourceLocation CS_SPRITE_LAPIS    = new ResourceLocation("minecraft", "block/lapis_ore");
    @Unique private static final ResourceLocation CS_SPRITE_REDSTONE = new ResourceLocation("minecraft", "block/redstone_ore");
    @Unique private static final ResourceLocation CS_SPRITE_DIAMOND  = new ResourceLocation("minecraft", "block/diamond_ore");
    @Unique private static final ResourceLocation CS_SPRITE_DEEPSLATE = new ResourceLocation("minecraft", "block/deepslate");
    @Unique private static final ResourceLocation CS_SPRITE_GRANITE  = new ResourceLocation("minecraft", "block/granite");
    @Unique private static final ResourceLocation CS_SPRITE_ANDESITE = new ResourceLocation("minecraft", "block/andesite");
    @Unique private static final ResourceLocation CS_SPRITE_GRAVEL    = new ResourceLocation("minecraft", "block/gravel");
    @Unique private static final ResourceLocation CS_SPRITE_TUFF      = new ResourceLocation("minecraft", "block/tuff");
    @Unique private static final ResourceLocation CS_SPRITE_LIMESTONE = new ResourceLocation("quark", "block/limestone");
    @Unique private static final ResourceLocation CS_SPRITE_RUBY = new ResourceLocation("rediscovered", "block/ruby_ore");

    // Animated Textures
    @Unique private static final ResourceLocation CS_SPRITE_MAGMA    = new ResourceLocation("minecraft", "block/magma");
    @Unique private static final ResourceLocation CS_SPRITE_SUS      = new ResourceLocation("gamma", "block/debug2");

    @Shadow @Final private AdvancementWidget root;
    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;
    @Shadow private double scrollX;
    @Shadow private double scrollY;

    @Shadow private boolean centered;

    @Unique private static double cs$savedScrollX = Double.MAX_VALUE;
    @Unique private static double cs$savedScrollY = Double.MAX_VALUE;

    @Unique private boolean cs$layoutDirty = false;

    // ── Coordinate Bit-Scrambler ──────────────────────────────────────────────

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

        int deepslateBoundTile = topBoundTile + (int)((bottomBoundTile - topBoundTile) * 0.66f);

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
            textureToDraw = CS_SPRITE_DIRT;
        } else if (gridY >= noisyBottom - 1) {
            textureToDraw = CS_SPRITE_BEDROCK;
        } else {
            float depthRatio = Mth.clamp((float) (gridY - noisyTop) / (float) (noisyBottom - noisyTop), 0.0f, 1.0f);
            ResourceLocation stoneBase = (gridY >= noisyDeepslate) ? CS_SPRITE_DEEPSLATE : CS_SPRITE_STONE;

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
                        if (typeRoll < 0.28f) {
                            candidateBlobType = CS_SPRITE_GRAVEL;
                        } else if (typeRoll < 0.53f) {
                            candidateBlobType = CS_SPRITE_ANDESITE;
                        } else if (typeRoll < 0.73f) {
                            candidateBlobType = CS_SPRITE_TUFF;
                        } else if (typeRoll < 0.87f) {
                            candidateBlobType = CS_SPRITE_GRANITE;
                        } else {
                            candidateBlobType = CS_SPRITE_LIMESTONE;
                        }

                        int centerX = checkRegX * blobGridSize + blobRand.nextInt(blobGridSize);
                        int centerY = checkRegY * blobGridSize + blobRand.nextInt(blobGridSize);

                        if (centerY < topBoundTile + 3) continue;

                        int dx = Math.abs(gridX - centerX);
                        int dy = Math.abs(gridY - centerY);

                        if (dx <= 2 && dy <= 2 && !(dx == 2 && dy == 2)) {
                            long noiseSeed = cs$mixCoordinates(gridX, gridY) + blobSeed;
                            if (new Random(noiseSeed).nextFloat() < 0.80f) {
                                stoneBase = candidateBlobType;
                            }
                        }
                    }
                }
            }

            long cellSeed = cs$mixCoordinates(gridX, gridY) + 123456789L;
            Random random = new Random(cellSeed);
            int chance = random.nextInt(1000);

            if (chance < 2) {
                textureToDraw = (depthRatio >= 0.5f) ? CS_SPRITE_SUS : stoneBase;
            } else if (chance < 12) {
                textureToDraw = (depthRatio >= 0.8f) ? CS_SPRITE_DIAMOND : stoneBase;
            } else if (chance < 18) {
                textureToDraw = (depthRatio >= 0.6f) ? CS_SPRITE_REDSTONE : stoneBase;
            } else if (chance < 20) {
                textureToDraw = CS_SPRITE_RUBY;
            } else if (chance < 24) {
                textureToDraw = (depthRatio >= 0.5f) ? CS_SPRITE_LAPIS : stoneBase;
            } else if (chance < 32) {
                textureToDraw = (depthRatio >= 0.4f) ? CS_SPRITE_GOLD : stoneBase;
            } else if (chance < 44) {
                textureToDraw = (depthRatio >= 0.2f) ? CS_SPRITE_IRON : stoneBase;
            } else if (chance < 68) {
                textureToDraw = (random.nextFloat() >= depthRatio) ? CS_SPRITE_COAL : stoneBase;
            } else {
                textureToDraw = stoneBase;
            }
        }

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(textureToDraw);

        float gradientRatio = Mth.clamp((float) (gridY - topBoundTile) / (float) (bottomBoundTile - topBoundTile), 0.0f, 1.0f);
        float brightness = 0.7f - (0.2f * gradientRatio);

        guiGraphics.setColor(brightness, brightness, brightness, 1.0F);
        guiGraphics.blit(x, y, 0, width, height, sprite);
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

    // ── Layout flush + scroll restore (HEAD) ──────────────────────────────────

    @Inject(method = "drawContents", at = @At("HEAD"))
    private void cs$flushLayoutIfDirty(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        if (cs$layoutDirty) {
            cs$layoutDirty = false;
            cs$relayout();
        }

        if (!this.centered && cs$savedScrollX != Double.MAX_VALUE) {
            this.scrollX = cs$savedScrollX;
            this.scrollY = cs$savedScrollY;
            this.centered = true;
        }
    }

    // ── Scroll persistence (TAIL) ─────────────────────────────────────────────

    @Inject(method = "drawContents", at = @At("TAIL"))
    private void cs$persistScrollPosition(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        cs$savedScrollX = this.scrollX;
        cs$savedScrollY = this.scrollY;
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

            // FIX: Always pull from committedRoutes to get physically spliced maps,
            // bypassing the stale routes natively cached inside the widget.
            List<GridPos> rawRoute = committedRoutes.get(entry.getKey());
            if (rawRoute != null) {
                List<GridPos> normalizedRoute = new ArrayList<>(rawRoute.size());
                for (GridPos cell : rawRoute) {
                    normalizedRoute.add(new GridPos(cell.x() - minGridX, cell.y() - minGridY));
                }
                api.setIncomingRoute(normalizedRoute);
            } else {
                api.setIncomingRoute(null);
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

        LayoutCandidate bestCandidate = null;
        List<GridPos>   bestRoute     = null;

        int nodeWeight    = subtreeWeights.getOrDefault(node, 1);
        int blossomRadius = cs$blossomRadius(nodeWeight);

        int retries = 0;

        // The Retry Loop - guarantees placement by splicing the grid on failure
        while (bestCandidate == null) {
            // Must fetch dynamically in case parent was moved during a splice
            GridPos parentPos = positions.get(parent);

            List<Integer> directions = new ArrayList<>(List.of(
                    theCoralineSystems$RIGHT, theCoralineSystems$DOWN,
                    theCoralineSystems$LEFT,  theCoralineSystems$UP));
            directions.sort((a, b) ->
                    cs$countOpenCone(b, parentPos, blossomRadius, occupied)
                            - cs$countOpenCone(a, parentPos, blossomRadius, occupied));

            // ── Phase 1: Normal placement ─────────────────────────────────────────
            for (int direction : directions) {
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
                            parentPos, candidatePos, occupied, nodeWeight, route.size());

                    if (bestCandidate == null || score < bestCandidate.score()) {
                        bestCandidate = new LayoutCandidate(candidatePos, direction, score);
                        bestRoute     = route;
                    }
                }
            }

            // ── Phase 2: Branch-off fallback ──────────────────────────────────────
            if (bestCandidate == null) {
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
                                        parentPos, candidatePos, occupied, nodeWeight, fullRoute.size());
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

            if (bestCandidate != null) break; // Found a valid layout, exit loop.

            // ── Phase 3: Grid Splicing (Dynamic Expansion) ─────────────────────────
            // If we are boxed in, physically split the grid along the parent's axis
            // to inject an empty lane and try Phase 1 & 2 again.
            int spliceDir = directions.get(retries % directions.size());
            cs$spliceGrid(positions, committedRoutes, parentPos, spliceDir);

            // Rebuild occupancy map based on the newly expanded coordinates
            occupied.clear();
            occupied.addAll(positions.values());
            for (List<GridPos> route : committedRoutes.values()) {
                occupied.addAll(route);
            }

            retries++;
            if (retries > 10) {
                // Hard-failsafe to prevent server lockups in extreme edge cases
                // (Should mathematically never be reached due to splicing)
                bestCandidate = new LayoutCandidate(new GridPos(parentPos.x(), parentPos.y() + 1), theCoralineSystems$DOWN, 0);
                bestRoute = List.of(parentPos, bestCandidate.pos());
                break;
            }
        }

        // ── Commit ────────────────────────────────────────────────────────────
        positions.put(node, bestCandidate.pos());
        occupied.addAll(bestRoute);

        IAdvancementWidgetCS nodeApi = (IAdvancementWidgetCS) node;
        nodeApi.setArrivalDir(bestCandidate.direction());
        nodeApi.setIncomingRoute(bestRoute);

        committedRoutes.put(node, bestRoute);

        List<AdvancementWidget> nodeChildren =
                new ArrayList<>(((IAdvancementWidgetCS) node).getChildren());
        nodeChildren.sort((a, b) ->
                subtreeWeights.getOrDefault(b, 1) - subtreeWeights.getOrDefault(a, 1));

        for (AdvancementWidget child : nodeChildren) {
            cs$placeNode(child, node, positions, occupied,
                    committedRoutes, subtreeWeights);
        }
    }

    // ── Phase 3: Splicing Logic ───────────────────────────────────────────────

    @Unique
    private void cs$spliceGrid(
            Map<AdvancementWidget, GridPos> positions,
            Map<AdvancementWidget, List<GridPos>> committedRoutes,
            GridPos origin, int spliceDir) {

        // 1. Shift Nodes
        for (Map.Entry<AdvancementWidget, GridPos> entry : positions.entrySet()) {
            entry.setValue(cs$shiftGridPos(entry.getValue(), origin, spliceDir));
        }

        // 2. Shift and Stretch Routes
        for (Map.Entry<AdvancementWidget, List<GridPos>> entry : committedRoutes.entrySet()) {
            AdvancementWidget widget = entry.getKey();
            List<GridPos> oldRoute = entry.getValue();
            if (oldRoute == null || oldRoute.isEmpty()) continue;

            List<GridPos> newRoute = new ArrayList<>();

            // 2a. FIX: Check if the grid splice tore the implicit gap between the parent and the first route node
            AdvancementWidget parent = ((IAdvancementWidgetCS) widget).getParentWidget();
            if (parent != null) {
                GridPos parentPos = positions.get(parent); // This is safely already shifted from Step 1
                if (parentPos != null) {
                    GridPos firstShifted = cs$shiftGridPos(oldRoute.get(0), origin, spliceDir);
                    int dx = Math.abs(parentPos.x() - firstShifted.x());
                    int dy = Math.abs(parentPos.y() - firstShifted.y());

                    if (dx + dy > 1) {
                        int fillerX = (parentPos.x() + firstShifted.x()) / 2;
                        int fillerY = (parentPos.y() + firstShifted.y()) / 2;
                        newRoute.add(new GridPos(fillerX, fillerY));
                    }
                }
            }

            // 2b. Shift the existing line path and patch tears between segments
            for (int i = 0; i < oldRoute.size(); i++) {
                GridPos p1 = oldRoute.get(i);
                GridPos shiftedP1 = cs$shiftGridPos(p1, origin, spliceDir);
                newRoute.add(shiftedP1);

                if (i + 1 < oldRoute.size()) {
                    GridPos p2 = oldRoute.get(i + 1);
                    GridPos shiftedP2 = cs$shiftGridPos(p2, origin, spliceDir);

                    int dx = Math.abs(shiftedP1.x() - shiftedP2.x());
                    int dy = Math.abs(shiftedP1.y() - shiftedP2.y());

                    if (dx + dy > 1) {
                        int fillerX = (shiftedP1.x() + shiftedP2.x()) / 2;
                        int fillerY = (shiftedP1.y() + shiftedP2.y()) / 2;
                        newRoute.add(new GridPos(fillerX, fillerY));
                    }
                }
            }
            entry.setValue(newRoute);
        }
    }

    @Unique
    private GridPos cs$shiftGridPos(GridPos pos, GridPos origin, int dir) {
        int x = pos.x();
        int y = pos.y();
        if (dir == theCoralineSystems$RIGHT && x > origin.x()) return new GridPos(x + 1, y);
        if (dir == theCoralineSystems$LEFT  && x < origin.x()) return new GridPos(x - 1, y);
        if (dir == theCoralineSystems$DOWN  && y > origin.y()) return new GridPos(x, y + 1);
        if (dir == theCoralineSystems$UP    && y < origin.y()) return new GridPos(x, y - 1);
        return pos;
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

    // ── Blossom helpers ───────────────────────────────────────────────────────

    @Unique
    private static int cs$blossomRadius(int weight) {
        return Math.max(1, (int) Math.ceil(Math.sqrt(weight)));
    }

    @Unique
    private static int cs$countOpenCone(
            int direction, GridPos origin, int radius, Set<GridPos> occupied) {

        int free = 0;
        for (int along = 1; along <= radius; along++) {
            for (int perp = -radius; perp <= radius; perp++) {
                int cx, cy;
                switch (direction) {
                    case 0 -> { cx = origin.x() + along; cy = origin.y() + perp; } // RIGHT
                    case 1 -> { cx = origin.x() + perp;  cy = origin.y() + along; } // DOWN
                    case 2 -> { cx = origin.x() - along; cy = origin.y() + perp; } // LEFT
                    default -> { cx = origin.x() + perp;  cy = origin.y() - along; } // UP
                }
                if (!occupied.contains(new GridPos(cx, cy))) free++;
            }
        }
        return free;
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