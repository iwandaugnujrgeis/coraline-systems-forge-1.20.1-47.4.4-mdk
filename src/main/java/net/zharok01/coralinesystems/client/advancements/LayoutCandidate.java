package net.zharok01.coralinesystems.client.advancements;

public record LayoutCandidate(
        GridPos pos,
        int direction,
        int score
) {
}