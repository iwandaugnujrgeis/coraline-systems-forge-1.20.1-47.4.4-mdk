package net.zharok01.coralinesystems.client.advancement;

import java.util.List;

public record LayoutCandidate(GridPos pos, List<GridPos> route, int score) {
}