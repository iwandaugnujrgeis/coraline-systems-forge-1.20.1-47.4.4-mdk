package net.zharok01.coralinesystems.client.advancements;

/**
 * Describes a point on an already-committed route where a new overflow child
 * may branch off.
 *
 * @param cell        the grid cell that will act as the T-junction
 * @param cellIndex   the index of {@code cell} within the sibling's stored
 *                    {@code incomingRoute} list (used to split the route for
 *                    the renderer anchor)
 * @param ownerRoute  a reference to the sibling's full route list (used only
 *                    during placement scoring — not stored long-term)
 */
public record BranchPoint(
        GridPos cell,
        int     cellIndex,
        java.util.List<GridPos> ownerRoute
) {}