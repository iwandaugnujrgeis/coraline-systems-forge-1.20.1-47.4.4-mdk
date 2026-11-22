package net.zharok01.coralinesystems.client.gui.recipe_book;

import java.util.List;

public interface RecipePageNavigation {
    void coraline$collectSnapTargets(List<int[]> targets);

    boolean coraline$isOverVariantCell(double mouseX, double mouseY);

    boolean coraline$isStripOpen();

    void coraline$stepStrip(int direction);
}