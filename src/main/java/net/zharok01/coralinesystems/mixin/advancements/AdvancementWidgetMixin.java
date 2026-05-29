package net.zharok01.coralinesystems.mixin.advancements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.zharok01.coralinesystems.util.IAdvancementWidgetCS;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(AdvancementWidget.class)
@Implements(@Interface(iface = IAdvancementWidgetCS.class, prefix = "cs$"))
public abstract class AdvancementWidgetMixin {

    @Mutable @Final @Shadow private int x;
    @Mutable @Final @Shadow private int y;

    @Shadow @Final private Advancement advancement;
    @Shadow @Final private AdvancementTab tab;
    @Shadow @Final private DisplayInfo display;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private FormattedCharSequence title;
    @Shadow @Final private int width;
    @Shadow @Final private List<FormattedCharSequence> description;
    @Shadow @Final private List<AdvancementWidget> children;

    @Shadow @Nullable private AdvancementWidget parent;
    @Shadow @Nullable private AdvancementProgress progress;

    // The cardinal direction this widget's subtree travels in, set by layout.
    // −1 = root/unknown, 0 = Right, 1 = Down, 2 = Left, 3 = Up.
    @Unique private int cs_arrivedDir = -1;

    @Unique private static final ResourceLocation WIDGETS_LOCATION =
            new ResourceLocation("textures/gui/advancements/widgets.png");

    // Frame tint: 20 % brightness preserves rounded-corner transparency while
    // making locked advancements clearly dark.
    @Unique private static final float CS_FRAME_DIM = 0.20f;

    // Exactly two line colours: green for completed-parent edges, dark for all
    // others. No white / vanilla fallback — the UI uses only these two.
    @Unique private static final int CS_GREEN_LINE   = 0xFF55FF55;
    @Unique private static final int CS_GREEN_SHADOW = 0xFF004000;
    @Unique private static final int CS_DARK_LINE    = 0xFF404040;
    @Unique private static final int CS_DARK_SHADOW  = 0xFF101010;

    // ── Frame geometry ────────────────────────────────────────────────────────
    // The advancement frame sprite is blitted at (widget.x + 3, widget.y),
    // size 26 × 26. All anchor helpers below use these constants.
    @Unique private static final int FRAME_INSET_X = 3;   // left inset from widget origin
    @Unique private static final int FRAME_SIZE    = 26;  // width and height of the frame
    @Unique private static final int FRAME_HALF    = 13;  // FRAME_SIZE / 2

    // ── IAdvancementWidgetCS  (Mixin strips the "cs$" prefix automatically) ──

    public void cs$setX(int x)                      { this.x = x; }
    public void cs$setY(int y)                      { this.y = y; }
    public List<AdvancementWidget> cs$getChildren() { return this.children; }
    @Nullable
    public AdvancementProgress cs$getProgress()     { return this.progress; }
    public void cs$setArrivalDir(int dir)           { this.cs_arrivedDir = dir; }
    public int  cs$getArrivalDir()                  { return this.cs_arrivedDir; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} when this advancement should appear "locked":
     * <ul>
     * <li>Completed advancements are never locked.</li>
     * <li>Advancements flagged {@code "hidden": true} in JSON are always
     * locked until completed.</li>
     * <li>Any advancement whose direct parent has not yet been completed is
     * locked (the player is at least two steps away).</li>
     * </ul>
     */
    @Unique
    private boolean theCoralineSystems$isObscured() {
        if (this.progress != null && this.progress.isDone()) return false;
        if (this.display.isHidden()) return true;
        Advancement parentAdv = this.advancement.getParent();
        if (parentAdv != null) {
            AdvancementWidget pw = this.tab.getWidget(parentAdv);
            if (pw != null) {
                AdvancementProgress pp = ((IAdvancementWidgetCS) pw).getProgress();
                return pp == null || !pp.isDone();
            }
            return true; // parent widget missing → treat as locked
        }
        return false; // root advancement is never locked
    }

    /**
     * Returns {@code true} when the edge from {@link #parent} to {@code this}
     * should be drawn in green — i.e. the parent has been completed and this
     * child is itself visible (not obscured).
     */
    @Unique
    private boolean theCoralineSystems$isGreenEdge() {
        assert this.parent != null;
        AdvancementProgress pp = ((IAdvancementWidgetCS) this.parent).getProgress();
        return pp != null && pp.isDone() && !this.theCoralineSystems$isObscured();
    }

    /**
     * Returns the appropriate ARGB colour for an edge given the shadow/green
     * flags.
     */
    @Unique
    private static int theCoralineSystems$lineColor(boolean dropShadow, boolean green) {
        if (green) return dropShadow ? CS_GREEN_SHADOW : CS_GREEN_LINE;
        else       return dropShadow ? CS_DARK_SHADOW  : CS_DARK_LINE;
    }

    /**
     * Draws the L-shaped connector between {@link #parent} and {@code this},
     * using direction-aware anchor points so that lines always exit and enter
     * from the correct face of each frame.
     *
     * <pre>
     * Direction  Parent exits   Child enters   Junction axis
     * ─────────  ────────────   ────────────   ─────────────
     * RIGHT (0)  right  edge    left   edge    mid-column X
     * DOWN  (1)  bottom edge    top    edge    mid-row    Y
     * LEFT  (2)  left   edge    right  edge    mid-column X
     * UP    (3)  top    edge    bottom edge    mid-row    Y
     * </pre>
     *
     * For each axis the junction is placed at the midpoint between the two
     * facing edges, producing a symmetric gap on both sides regardless of
     * how many grid slots apart the widgets are.
     */
    @Unique
    private void theCoralineSystems$drawEdgeLines(GuiGraphics g, int ox, int oy,
                                                  boolean dropShadow, int color) {
        assert this.parent != null;

        // Shadow offset: the shadow copy is drawn 1 px down-right.
        final int d = dropShadow ? 1 : 0;

        // ── Parent frame edges ────────────────────────────────────────────────
        final int pLeft   = ox + this.parent.getX() + FRAME_INSET_X;
        final int pRight  = pLeft  + FRAME_SIZE;          // x + 3 + 26 = x + 29
        final int pTop    = oy + this.parent.getY();
        final int pBottom = pTop   + FRAME_SIZE;
        final int pCX     = pLeft  + FRAME_HALF;          // horizontal centre of parent
        final int pCY     = pTop   + FRAME_HALF;          // vertical   centre of parent

        // ── Child frame edges ─────────────────────────────────────────────────
        final int cLeft   = ox + this.x + FRAME_INSET_X;
        final int cRight  = cLeft  + FRAME_SIZE;
        final int cTop    = oy + this.y;
        final int cBottom = cTop   + FRAME_SIZE;
        final int cCX     = cLeft  + FRAME_HALF;
        final int cCY     = cTop   + FRAME_HALF;

        switch (this.cs_arrivedDir) {

            case 0 -> {
                // RIGHT — child is to the right of parent.
                // Parent exits its right edge; child enters its left edge.
                // Junction column at the midpoint between the two facing edges.
                final int jX = (pRight + cLeft) / 2;
                g.hLine(pRight + d, jX    + d, pCY  + d, color); // parent → junction
                g.vLine(jX    + d, pCY   + d, cCY  + d, color); // junction vertical
                g.hLine(jX    + d, cLeft + d, cCY  + d, color); // junction → child
            }

            case 1 -> {
                // DOWN — child is below parent.
                // Parent exits its bottom edge; child enters its top edge.
                // Junction row at the midpoint between the two facing edges.
                final int jY = (pBottom + cTop) / 2;
                g.vLine(pCX    + d, pBottom + d, jY   + d, color); // parent → junction
                g.hLine(pCX    + d, cCX     + d, jY   + d, color); // junction horizontal
                g.vLine(cCX    + d, jY      + d, cTop + d, color); // junction → child
            }

            case 2 -> {
                // LEFT — child is to the left of parent.
                // Parent exits its left edge; child enters its right edge.
                // Junction column at the midpoint between the two facing edges.
                final int jX = (pLeft + cRight) / 2;
                g.hLine(pLeft  + d, jX     + d, pCY  + d, color); // parent → junction
                g.vLine(jX     + d, pCY    + d, cCY  + d, color); // junction vertical
                g.hLine(jX     + d, cRight + d, cCY  + d, color); // junction → child
            }

            case 3 -> {
                // UP — child is above parent.
                // Parent exits its top edge; child enters its bottom edge.
                // Junction row at the midpoint between the two facing edges.
                final int jY = (pTop + cBottom) / 2;
                g.vLine(pCX    + d, pTop    + d, jY      + d, color); // parent → junction
                g.hLine(pCX    + d, cCX     + d, jY      + d, color); // junction horizontal
                g.vLine(cCX    + d, jY      + d, cBottom + d, color); // junction → child
            }

            // default / -1: root widget — no parent edge to draw.
        }
    }

    // ── drawConnectivityCS() — filtered connectivity pass ────────────────────

    /**
     * Draws connectivity lines for {@code this} widget and all its descendants,
     * filtered to only the "green" or "dark" colour category.
     *
     * <p>Invoked four times per frame by {@code AdvancementTabMixin}:
     * dark shadows → green shadows → dark lines → green lines.  This ordering
     * guarantees that green lines and their shadows always appear on top of dark
     * ones regardless of tree traversal order.
     *
     * @param dropShadow {@code true} for the shadow pass
     * @param greenOnly  {@code true} → skip dark edges; {@code false} → skip green
     */
    public void cs$drawConnectivityCS(GuiGraphics guiGraphics, int x, int y,
                                      boolean dropShadow, boolean greenOnly) {
        if (this.parent != null) {
            boolean green = this.theCoralineSystems$isGreenEdge();
            if (green == greenOnly) {
                this.theCoralineSystems$drawEdgeLines(
                        guiGraphics, x, y, dropShadow,
                        theCoralineSystems$lineColor(dropShadow, green));
            }
        }
        for (AdvancementWidget child : this.children)
            ((IAdvancementWidgetCS) child).drawConnectivityCS(
                    guiGraphics, x, y, dropShadow, greenOnly);
    }

    // ── drawConnectivity() — vanilla entry point (kept for external callers) ─

    /**
     * @author coralinesystems
     * @reason Replaced vanilla's white/black two-tone lines with the two-colour
     * scheme (green or dark only).  {@code AdvancementTabMixin} redirects
     * both calls from {@code drawContents} to {@link #cs$drawConnectivityCS}
     * instead, so this path is only hit by external callers.  Per-edge
     * layering is not guaranteed here; use the redirected path for that.
     */
    @Overwrite
    public void drawConnectivity(GuiGraphics guiGraphics, int x, int y, boolean dropShadow) {
        if (this.parent != null) {
            boolean green = this.theCoralineSystems$isGreenEdge();
            this.theCoralineSystems$drawEdgeLines(
                    guiGraphics, x, y, dropShadow,
                    theCoralineSystems$lineColor(dropShadow, green));
        }
        for (AdvancementWidget child : this.children)
            child.drawConnectivity(guiGraphics, x, y, dropShadow);
    }

    // ── draw() ────────────────────────────────────────────────────────────────

    /**
     * @author coralinesystems
     * @reason Darkened frame + black-silhouette icon for locked advancements via
     * RenderSystem shader-colour tinting — no Z-elevated fill geometry,
     * so the tooltip pass is never occluded.
     */
    @Overwrite
    public void draw(GuiGraphics guiGraphics, int x, int y) {
        boolean obscured = this.theCoralineSystems$isObscured();

        float pct = this.progress == null ? 0.0F : this.progress.getPercent();
        AdvancementWidgetType frameType = pct >= 1.0F
                ? AdvancementWidgetType.OBTAINED : AdvancementWidgetType.UNOBTAINED;

        if (obscured) {
            // Darkened frame — texture alpha is preserved so rounded corners
            // stay transparent; only opaque pixels are dimmed.
            RenderSystem.setShaderColor(CS_FRAME_DIM, CS_FRAME_DIM, CS_FRAME_DIM, 1.0f);
            guiGraphics.blit(WIDGETS_LOCATION,
                    x + this.x + 3, y + this.y,
                    this.display.getFrame().getTexture(),
                    128 + frameType.getIndex() * 26, 26, 26);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            // Black-silhouette icon — zeroing RGB preserves the sprite's alpha,
            // producing an exact black cutout of the item's shape.
            RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
            guiGraphics.renderFakeItem(this.display.getIcon(),
                    x + this.x + 8, y + this.y + 5);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            guiGraphics.blit(WIDGETS_LOCATION,
                    x + this.x + 3, y + this.y,
                    this.display.getFrame().getTexture(),
                    128 + frameType.getIndex() * 26, 26, 26);
            guiGraphics.renderFakeItem(this.display.getIcon(),
                    x + this.x + 8, y + this.y + 5);
        }

        for (AdvancementWidget child : this.children)
            child.draw(guiGraphics, x, y);
    }

    // ── drawHover() ───────────────────────────────────────────────────────────

    /**
     * @author coralinesystems
     * @reason Locked tooltip shows "???" title + description, black-silhouette
     * icon, and darkened frame.  Unlocked tooltip faithfully reproduces
     * vanilla (with the awt2 bug corrected — see below).
     *
     * flipUp fix: description Y is now properly anchored based on the
     * flip state, ensuring it stays inside the tooltip box.
     */
    @Overwrite
    public void drawHover(GuiGraphics guiGraphics, int x, int y,
                          float fade, int width, int height) {

        if (this.theCoralineSystems$isObscured()) {
            // ── Locked tooltip ────────────────────────────────────────────────
            Component lockedTitle = Component.literal("???");

            int textW = this.minecraft.font.width(lockedTitle);
            int boxW  = 29 + textW + 8;
            int n     = 41; // 26 header + 9 desc line + 6 padding

            boolean flipLeft = width + x + this.x + boxW + 26 >= this.tab.getScreen().width;
            boolean flipUp   = 113 - y - this.y - 26 <= 6 + 9;

            RenderSystem.enableBlend();

            int l    = y + this.y;
            int m    = flipLeft ? x + this.x - boxW + 26 + 6 : x + this.x;
            int boxY = flipUp   ? l + 26 - n : l;

            // Background box
            guiGraphics.blitNineSliced(WIDGETS_LOCATION, m, boxY, boxW, n, 10, 200, 26, 0, 52);

            // Header strips
            int j = boxW / 2, k = boxW - j;
            guiGraphics.blit(WIDGETS_LOCATION, m,     l, 0,       AdvancementWidgetType.UNOBTAINED.getIndex() * 26, j, 26);
            guiGraphics.blit(WIDGETS_LOCATION, m + j, l, 200 - k, AdvancementWidgetType.UNOBTAINED.getIndex() * 26, k, 26);

            // Frame — darkened
            RenderSystem.setShaderColor(CS_FRAME_DIM, CS_FRAME_DIM, CS_FRAME_DIM, 1.0f);
            guiGraphics.blit(WIDGETS_LOCATION,
                    x + this.x + 3, y + this.y,
                    this.display.getFrame().getTexture(),
                    128 + AdvancementWidgetType.UNOBTAINED.getIndex() * 26, 26, 26);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            // Title
            int titleX = flipLeft ? m + 5 : x + this.x + 32;
            guiGraphics.drawString(this.minecraft.font, lockedTitle, titleX, y + this.y + 9, -1, false);

            // Description — properly anchored based on flipUp state
            int descY = flipUp ? boxY + 7 : l + 26;
            guiGraphics.drawString(this.minecraft.font,
                    Component.literal("???"),
                    m + 5, descY, 0xAAAAAA, false);

            // Icon — black silhouette
            RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
            guiGraphics.renderFakeItem(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            return;
        }

        // ── Unlocked tooltip — vanilla re-implementation (awt2 bug corrected) ─
        boolean bl  = width + x + this.x + this.width + 26 >= this.tab.getScreen().width;
        String  progressText = this.progress == null ? null : this.progress.getProgressText();
        int     progressW    = progressText == null ? 0 : this.minecraft.font.width(progressText);
        boolean bl2 = 113 - y - this.y - 26 <= 6 + this.description.size() * 9;
        float   f   = this.progress == null ? 0.0F : this.progress.getPercent();
        int     j   = Mth.floor(f * this.width);

        AdvancementWidgetType awt1, awt2, awt3;
        if (f >= 1.0F) {
            j = this.width / 2;
            awt1 = AdvancementWidgetType.OBTAINED;
            awt2 = AdvancementWidgetType.OBTAINED;
            awt3 = AdvancementWidgetType.OBTAINED;
        } else if (j < 2) {
            j = this.width / 2;
            awt1 = AdvancementWidgetType.UNOBTAINED;
            awt2 = AdvancementWidgetType.UNOBTAINED;
            awt3 = AdvancementWidgetType.UNOBTAINED;
        } else if (j > this.width - 2) {
            j = this.width / 2;
            awt1 = AdvancementWidgetType.OBTAINED;
            awt2 = AdvancementWidgetType.OBTAINED;   // fixed: was incorrectly UNOBTAINED
            awt3 = AdvancementWidgetType.UNOBTAINED;
        } else {
            awt1 = AdvancementWidgetType.OBTAINED;
            awt2 = AdvancementWidgetType.UNOBTAINED;
            awt3 = AdvancementWidgetType.UNOBTAINED;
        }

        int k = this.width - j;
        RenderSystem.enableBlend();
        int l = y + this.y;
        int m = bl ? x + this.x - this.width + 26 + 6 : x + this.x;
        int n = 32 + this.description.size() * 9;

        if (!this.description.isEmpty()) {
            guiGraphics.blitNineSliced(WIDGETS_LOCATION,
                    m, bl2 ? l + 26 - n : l, this.width, n, 10, 200, 26, 0, 52);
        }

        guiGraphics.blit(WIDGETS_LOCATION, m,     l, 0,       awt1.getIndex() * 26, j, 26);
        guiGraphics.blit(WIDGETS_LOCATION, m + j, l, 200 - k, awt2.getIndex() * 26, k, 26);
        guiGraphics.blit(WIDGETS_LOCATION,
                x + this.x + 3, y + this.y,
                this.display.getFrame().getTexture(),
                128 + awt3.getIndex() * 26, 26, 26);

        if (bl) {
            guiGraphics.drawString(this.minecraft.font, this.title, m + 5, y + this.y + 9, -1);
            if (progressText != null)
                guiGraphics.drawString(this.minecraft.font, progressText,
                        x + this.x - progressW, y + this.y + 9, -1);
        } else {
            guiGraphics.drawString(this.minecraft.font, this.title,
                    x + this.x + 32, y + this.y + 9, -1);
            if (progressText != null)
                guiGraphics.drawString(this.minecraft.font, progressText,
                        x + this.x + this.width - progressW - 5, y + this.y + 9, -1);
        }

        for (int o = 0; o < this.description.size(); o++) {
            guiGraphics.drawString(this.minecraft.font,
                    this.description.get(o), m + 5,
                    (bl2 ? l + 26 - n + 7 : y + this.y + 9 + 17) + o * 9,
                    -5592406, false);
        }

        guiGraphics.renderFakeItem(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
    }

    // ── isMouseOver() ─────────────────────────────────────────────────────────

    /**
     * @author coralinesystems
     * @reason All visible (non-hidden) advancements are hoverable so the player
     * can inspect the "???" locked tooltip.
     */
    @Overwrite
    public boolean isMouseOver(int x, int y, int mouseX, int mouseY) {
        int i = x + this.x, j = i + 26;
        int k = y + this.y, l = k + 26;
        return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
    }
}