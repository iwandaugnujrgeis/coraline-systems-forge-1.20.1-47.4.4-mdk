package net.zharok01.coralinesystems.client.advancement;

import net.minecraft.client.gui.screens.advancements.AdvancementWidget;

import java.util.Set;

public class PlacementContext {
    public AdvancementWidget parent;
    public GridPos parentPos;
    public GridPos rootPos;
    public Set<GridPos> nodeCells;
    public Set<GridPos> blocked;

    public int[] bounds;
    public int subtreeWeight;
    public LayoutCandidate best;
}