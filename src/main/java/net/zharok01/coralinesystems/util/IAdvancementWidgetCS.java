package net.zharok01.coralinesystems.util;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Coraline Systems — injected into {@link AdvancementWidget} via
 * {@code @Implements} in {@code AdvancementWidgetMixin}.
 *
 * <p><b>Naming convention:</b> method names here carry NO {@code cs$} prefix.
 * The prefix lives only on the mixin-side implementations.  Mixin strips it
 * when matching against this interface, so {@code cs$setX} → {@code setX}.
 *
 * <p>External callers cast to this interface and use the plain names:
 * <pre>{@code ((IAdvancementWidgetCS) widget).setX(42); }</pre>
 */
public interface IAdvancementWidgetCS {

    /** Overrides the widget's pixel-space X coordinate (set during layout). */
    void setX(int x);

    /** Overrides the widget's pixel-space Y coordinate (set during layout). */
    void setY(int y);

    /**
     * Returns the direct children of this widget in the advancement tree.
     * The list is the live {@code children} field of the widget instance.
     */
    List<AdvancementWidget> getChildren();

    /**
     * Returns the current {@link AdvancementProgress} for this widget,
     * or {@code null} if the server has not yet sent any progress data.
     * Used by the connectivity pass to colour the line green when a parent
     * has been completed.
     */
    @Nullable
    AdvancementProgress getProgress();

    /**
     * Stores the cardinal direction this widget's subtree travels in.
     * Set by {@code AdvancementTabMixin} during the layout pass; consumed by
     * {@code AdvancementWidgetMixin} when drawing connector lines.
     *
     * <ul>
     *   <li>−1 = root (no parent edge drawn)</li>
     *   <li> 0 = Right</li>
     *   <li> 1 = Down</li>
     *   <li> 2 = Left</li>
     *   <li> 3 = Up</li>
     * </ul>
     */
    void setArrivalDir(int dir);

    /**
     * Returns the direction stored by {@link #setArrivalDir(int)}.
     * Defaults to {@code −1} (root / unknown) until layout has run.
     */
    int getArrivalDir();

    /**
     * Draws connectivity lines for this widget and all its descendants,
     * filtered to either the "green" or the "dark" colour category.
     *
     * <p>Called by {@code AdvancementTabMixin} in four ordered passes so that
     * green lines and their shadows always render on top of dark ones:
     * <ol>
     *   <li>Dark shadows &nbsp;({@code dropShadow=true,  greenOnly=false})</li>
     *   <li>Green shadows ({@code dropShadow=true,  greenOnly=true})</li>
     *   <li>Dark lines &nbsp;&nbsp;&nbsp;({@code dropShadow=false, greenOnly=false})</li>
     *   <li>Green lines &nbsp;&nbsp;({@code dropShadow=false, greenOnly=true})</li>
     * </ol>
     *
     * <p>A "green" edge is one where the parent has been completed
     * ({@code AdvancementProgress.isDone()}) and the child is not obscured
     * (i.e. the child's own parent is done — it is reachable in one step).
     * All other edges are "dark".
     *
     * @param guiGraphics the current graphics context
     * @param x           scroll-adjusted X origin (from the tab's draw call)
     * @param y           scroll-adjusted Y origin
     * @param dropShadow  {@code true} for the shadow pass, {@code false} for colour
     * @param greenOnly   {@code true} → draw only green edges; {@code false} → dark only
     */
    void drawConnectivityCS(GuiGraphics guiGraphics, int x, int y,
                            boolean dropShadow, boolean greenOnly);
}