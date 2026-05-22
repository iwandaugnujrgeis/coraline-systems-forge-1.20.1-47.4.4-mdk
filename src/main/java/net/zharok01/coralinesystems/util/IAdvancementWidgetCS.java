package net.zharok01.coralinesystems.util;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Coraline Systems — injected into {@link AdvancementWidget} via
 * {@code @Implements} in MixinAdvancementWidget.
 *
 * <p><b>Naming convention:</b> method names here have NO {@code cs$} prefix.
 * The prefix exists only on the mixin-side implementation methods.
 * Mixin strips the prefix before matching against this interface, so
 * mixin method {@code cs$setX} → interface method {@code setX}, etc.
 *
 * <p>External callers cast to this interface and use the plain names:
 * <pre>{@code ((IAdvancementWidgetCS) widget).setX(42); }</pre>
 */
public interface IAdvancementWidgetCS {

    /** Overrides the widget's pixel-space X coordinate (used by the layout pass). */
    void setX(int x);

    /** Overrides the widget's pixel-space Y coordinate (used by the layout pass). */
    void setY(int y);

    /**
     * Returns the direct children of this widget in the advancement tree.
     * The list is the live {@code children} field from the widget instance.
     */
    List<AdvancementWidget> getChildren();

    /**
     * Returns the current {@link AdvancementProgress} for this widget,
     * or {@code null} if the server has not yet sent any progress data.
     * Used to colour connecting lines green when a parent is completed.
     */
    @Nullable
    AdvancementProgress getProgress();
}
