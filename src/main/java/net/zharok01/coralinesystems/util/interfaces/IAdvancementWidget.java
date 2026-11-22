package net.zharok01.coralinesystems.util.interfaces;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.zharok01.coralinesystems.client.advancement.GridPos;

import javax.annotation.Nullable;
import java.util.List;

public interface IAdvancementWidget {

    void setX(int x);

    void setY(int y);

    List<AdvancementWidget> getChildren();

    @Nullable
    AdvancementProgress getProgress();

    @Nullable
    AdvancementWidget getParentWidget();

    void setIncomingRoute(List<GridPos> route);

    void drawEdges(GuiGraphics guiGraphics, int x, int y, boolean dropShadow, boolean completedPass);
}