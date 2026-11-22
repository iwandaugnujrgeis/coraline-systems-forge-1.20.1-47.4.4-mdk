package net.zharok01.coralinesystems.client.advancement;

public enum GridDirection {
    RIGHT(1, 0),
    DOWN(0, 1),
    LEFT(-1, 0),
    UP(0, -1);

    public final int dx;
    public final int dy;

    GridDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int along(GridPos pos) {
        return pos.x() * dx + pos.y() * dy;
    }
}