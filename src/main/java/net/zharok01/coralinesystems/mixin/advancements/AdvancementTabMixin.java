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

import java.util.*;

@Mixin(net.minecraft.client.gui.screens.advancements.AdvancementTab.class)
public abstract class AdvancementTabMixin {

    // Pixel distance between adjacent grid cells.
    // 36 px: compact enough to keep large trees visible without excessive dragging,
    // while still leaving 19 px of horizontal run between the junction and each
    // child centre (child centre is 36 px from parent centre; junction is 17 px
    // from parent centre → 36 − 17 = 19 px of visible branch line per sibling).
    @Unique private static final int CS_SLOT_W = 36;
    @Unique private static final int CS_SLOT_H = 36;

    // Cardinal directions: Right=0, Down=1, Left=2, Up=3.
    // The index matches the arrivalDir value stored on each widget and read
    // back by MixinAdvancementWidget.drawConnectivity for junction routing.
    @Unique private static final int[][] theCoralineSystems$DIRS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

    @Shadow @Final private AdvancementWidget root;
    @Final @Shadow private Map<Advancement, AdvancementWidget> widgets;
    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;

    // ──────────────────────────────────────────────────────────────────────────
    // Hooks
    // ──────────────────────────────────────────────────────────────────────────

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

    @Inject(method = "addAdvancement", at = @At("TAIL"))
    private void cs$onAddAdvancement(Advancement advancement, CallbackInfo ci) {
        if (advancement.getDisplay() != null) {
            theCoralineSystems$csRelayout();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Layout — two-pass subtree-isolated placement
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Omnidirectional tree layout with guaranteed no-overlap between sibling
     * subtrees and correct direction metadata for line routing.
     *
     * <h3>Algorithm overview</h3>
     * <b>Pass 1 (DFS)</b> — compute {@code leafCount[node]}: the number of leaf
     * descendants in each subtree (or 1 if the node is itself a leaf).  This
     * value is the exact number of perpendicular grid cells the subtree needs so
     * that no two sibling subtrees ever share a row/column.
     *
     * <b>Pass 2 (recursive DFS)</b> — place subtrees:
     * <ol>
     *   <li>The root's direct children are distributed among the four cardinal
     *       directions (Right, Down, Left, Up) in round-robin order, sorted by
     *       subtree size so the largest subtree gets the "natural" rightward
     *       direction.</li>
     *   <li>Within each direction's subtree, a node's children are all placed
     *       exactly one step forward along the primary axis and spread along the
     *       perpendicular axis in proportion to their leaf counts.  This makes
     *       every sibling appear at the <em>same</em> distance from their parent —
     *       never chained one behind the other.</li>
     *   <li>Each widget's {@code arrivalDir} is set here so that
     *       {@code MixinAdvancementWidget.drawConnectivity} can use the correct
     *       parent edge as the routing junction without having to infer direction
     *       from ambiguous pixel deltas.</li>
     *   <li>Corner collisions between different directional subtrees are resolved
     *       by a small BFS that finds the nearest free cell.</li>
     * </ol>
     *
     * <h3>Why siblings must share the same primary coordinate</h3>
     * If siblings were placed at increasing primary distances (primaryCoord+1,
     * primaryCoord+2, …) they would form a chain visually: each would appear to
     * be "unlocked by" the previous one.  Placing all siblings at the same
     * primary coordinate and spreading them perpendicular guarantees the visual
     * reads as a fan — one parent, many children — not a sequence.
     */
    @Unique
    private void theCoralineSystems$csRelayout() {
        if (this.root == null) return;

        // ── Pass 1: leaf counts ───────────────────────────────────────────────
        Map<AdvancementWidget, Integer> leafCounts = new HashMap<>();
        theCoralineSystems$computeLeafCounts(this.root, leafCounts);

        // ── Pass 2: placement ─────────────────────────────────────────────────
        Map<String, AdvancementWidget> occupied  = new HashMap<>();
        Map<AdvancementWidget, int[]>  positions = new HashMap<>();

        // Root at origin; it has no arrival direction (-1).
        positions.put(this.root, new int[]{0, 0});
        occupied.put("0,0", this.root);
        ((IAdvancementWidgetCS) this.root).setArrivalDir(-1);

        // Sort root children by descending subtree size so the biggest branch
        // gets direction 0 (Right), the next gets 1 (Down), etc.
        List<AdvancementWidget> rootChildren =
                new ArrayList<>(((IAdvancementWidgetCS) this.root).getChildren());
        rootChildren.sort((a, b) ->
                leafCounts.getOrDefault(b, 1) - leafCounts.getOrDefault(a, 1));

        // Each direction tracks how far its perpendicular allocation has grown.
        // This handles the case where more than 4 root children exist: a fifth
        // child is assigned direction 0 again but starts where the first one ended.
        int[] dirPerpOffset = new int[4];

        for (int i = 0; i < rootChildren.size(); i++) {
            AdvancementWidget child = rootChildren.get(i);
            int dir         = i % 4;
            int childLeaves = leafCounts.getOrDefault(child, 1);

            theCoralineSystems$placeSubtree(
                    child, 1,
                    dirPerpOffset[dir], dirPerpOffset[dir] + childLeaves,
                    dir, occupied, positions, leafCounts);

            dirPerpOffset[dir] += childLeaves;
        }

        // ── Normalise: shift all grid coords so minimum is (0,0) ─────────────
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

    // ──────────────────────────────────────────────────────────────────────────
    // Recursive subtree placement
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Places {@code node} and its entire subtree so that the subtree occupies
     * exactly the perpendicular range [{@code perpMin}, {@code perpMax}) — no
     * more, no less.
     *
     * <p>The node itself is placed at the centre of that range.  Its children
     * are then given sub-ranges of [{@code perpMin}, {@code perpMax}) proportional
     * to their own leaf counts, and the recursion continues one step further
     * along the primary axis.  The result is a layout where:
     * <ul>
     *   <li>All siblings are at the <em>same</em> primary distance from their
     *       parent — never chained forward.</li>
     *   <li>No two nodes in the same subtree can share a grid cell.</li>
     *   <li>The arrival direction is recorded on each node for the routing pass.</li>
     * </ul>
     *
     * @param node        widget to place
     * @param primaryCoord distance along the primary axis (e.g. X for Right)
     * @param perpMin     start of this subtree's perpendicular allocation (inclusive)
     * @param perpMax     end of this subtree's perpendicular allocation (exclusive)
     * @param dir         0=Right, 1=Down, 2=Left, 3=Up
     * @param occupied    global cell map (key "x,y" → widget)
     * @param positions   global position map (widget → int[]{gridX, gridY})
     * @param leafCounts  precomputed leaf counts
     */
    @Unique
    private void theCoralineSystems$placeSubtree(
            AdvancementWidget node,
            int primaryCoord,
            int perpMin, int perpMax,
            int dir,
            Map<String, AdvancementWidget> occupied,
            Map<AdvancementWidget, int[]>  positions,
            Map<AdvancementWidget, Integer> leafCounts) {

        // Set direction so the routing pass knows which parent edge to use.
        ((IAdvancementWidgetCS) node).setArrivalDir(dir);

        // Centre this node within its perpendicular allocation.
        int perpCenter = (perpMin + perpMax) / 2;
        int[] pos      = theCoralineSystems$toGrid(primaryCoord, perpCenter, dir);
        int gx = pos[0], gy = pos[1];

        // Resolve collision (corner-overlap between different directional subtrees).
        String key = gx + "," + gy;
        if (occupied.containsKey(key)) {
            int[] free = theCoralineSystems$findFree(gx, gy, occupied);
            gx = free[0]; gy = free[1];
            key = gx + "," + gy;
        }
        occupied.put(key, node);
        positions.put(node, new int[]{gx, gy});

        // Place each child one step further along the primary axis, each in its
        // own sub-range of [perpMin, perpMax) so siblings never overlap.
        List<AdvancementWidget> children =
                ((IAdvancementWidgetCS) node).getChildren();
        int currentPerp = perpMin;
        for (AdvancementWidget child : children) {
            int childLeaves = leafCounts.getOrDefault(child, 1);
            theCoralineSystems$placeSubtree(
                    child,
                    primaryCoord + 1,
                    currentPerp, currentPerp + childLeaves,
                    dir, occupied, positions, leafCounts);
            currentPerp += childLeaves;
        }
    }

    /**
     * Converts (primaryCoord, perpCoord) → 2-D grid position given direction.
     * <pre>
     *   Right (0): x=primary,  y=perp
     *   Down  (1): x=perp,     y=primary
     *   Left  (2): x=−primary, y=perp
     *   Up    (3): x=perp,     y=−primary
     * </pre>
     */
    @Unique
    private static int[] theCoralineSystems$toGrid(int primary, int perp, int dir) {
        switch (dir) {
            case 0: return new int[]{ primary,  perp};
            case 1: return new int[]{ perp,     primary};
            case 2: return new int[]{-primary,  perp};
            case 3: return new int[]{ perp,    -primary};
            default: return new int[]{primary,  perp};
        }
    }

    /**
     * BFS from (startX, startY) to find the nearest free grid cell.
     * Used only as a fallback when different directional subtrees collide at
     * a corner.
     */
    @Unique
    private static int[] theCoralineSystems$findFree(
            int startX, int startY,
            Map<String, AdvancementWidget> occupied) {

        Queue<int[]> queue   = new LinkedList<>();
        Set<String>  visited = new HashSet<>();
        queue.add(new int[]{startX, startY});
        visited.add(startX + "," + startY);

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            for (int[] d : theCoralineSystems$DIRS) {
                int nx = cur[0] + d[0], ny = cur[1] + d[1];
                String k = nx + "," + ny;
                if (!visited.contains(k)) {
                    visited.add(k);
                    if (!occupied.containsKey(k)) return new int[]{nx, ny};
                    queue.add(new int[]{nx, ny});
                }
            }
        }
        return new int[]{startX, startY}; // should never reach here
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Leaf-count computation (DFS, cached)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Recursively computes and caches the leaf count for every node.
     * The leaf count equals the number of leaf descendants, or 1 if the node
     * has no children.  It determines exactly how many perpendicular slots the
     * subtree needs so that no two sibling subtrees can ever overlap.
     */
    @Unique
    private static int theCoralineSystems$computeLeafCounts(
            AdvancementWidget node,
            Map<AdvancementWidget, Integer> cache) {

        List<AdvancementWidget> children =
                ((IAdvancementWidgetCS) node).getChildren();
        if (children.isEmpty()) {
            cache.put(node, 1);
            return 1;
        }
        int sum = 0;
        for (AdvancementWidget child : children) {
            sum += theCoralineSystems$computeLeafCounts(child, cache);
        }
        cache.put(node, sum);
        return sum;
    }
}
