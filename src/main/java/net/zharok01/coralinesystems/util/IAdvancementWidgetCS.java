package net.zharok01.coralinesystems.util;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.zharok01.coralinesystems.client.advancements.GridPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Coraline Systems — injected into {@link AdvancementWidget} via
 * {@code @Implements} in {@code AdvancementWidgetMixin}.
 */
public interface IAdvancementWidgetCS {

    void setX(int x);
    void setY(int y);

    List<AdvancementWidget> getChildren();

    @Nullable
    AdvancementProgress getProgress();

    /** * Returns the parent widget instance. Crucial for the Layout Engine's
     * Ancestry Check to ensure lines only merge with true siblings.
     */
    @Nullable
    AdvancementWidget getParentWidget();

    void setArrivalDir(int dir);
    int getArrivalDir();

    void setIncomingRoute(@Nullable List<GridPos> route);

    @Nullable
    List<GridPos> getIncomingRoute();

    void drawConnectivityCS(GuiGraphics guiGraphics, int x, int y,
                            boolean dropShadow, boolean greenOnly);
}