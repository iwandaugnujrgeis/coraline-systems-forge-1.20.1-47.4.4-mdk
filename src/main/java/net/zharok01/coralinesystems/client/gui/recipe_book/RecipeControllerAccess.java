package net.zharok01.coralinesystems.client.gui.recipe_book;

import java.util.List;

public interface RecipeControllerAccess {
    List<int[]> coraline$collectSnapTargets();

    boolean coraline$isOverVariantCell(double mouseX, double mouseY);

    boolean coraline$isStripOpen();

    void coraline$stepStrip(int direction);

    void coraline$closeStrip();
}