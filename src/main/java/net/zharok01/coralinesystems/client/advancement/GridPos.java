package net.zharok01.coralinesystems.client.advancement;

public record GridPos(int x, int y) {

    public static final int CELL_WIDTH = 34;
    public static final int CELL_HEIGHT = 30;

    public GridPos step(GridDirection direction, int distance) {
        return new GridPos(x + direction.dx * distance, y + direction.dy * distance);
    }
}