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

    // The cardinal direction this widget's subtree travels in, set during layout.
    // -1 = root/unknown, 0 = Right, 1 = Down, 2 = Left, 3 = Up.
    @Unique private int cs_arrivedDir = -1;

    @Unique private static final ResourceLocation WIDGETS_LOCATION =
            new ResourceLocation("textures/gui/advancements/widgets.png");

    // Frame tint: 20 % brightness preserves the frame's shape (rounded corners
    // remain transparent) while making it clearly dark.
    @Unique private static final float CS_FRAME_DIM = 0.20f;

    // Line colours — green for completed-parent links, dark for locked links.
    @Unique private static final int CS_GREEN_LINE   = 0xFF55FF55;
    @Unique private static final int CS_GREEN_SHADOW = 0xFF004000;
    @Unique private static final int CS_DARK_LINE    = 0xFF404040;
    @Unique private static final int CS_DARK_SHADOW  = 0xFF101010;

    // ──────────────────────────────────────────────────────────────────────────
    // IAdvancementWidgetCS (Mixin strips "cs$" prefix)
    // ──────────────────────────────────────────────────────────────────────────

    public void cs$setX(int x)                      { this.x = x; }
    public void cs$setY(int y)                      { this.y = y; }
    public List<AdvancementWidget> cs$getChildren() { return this.children; }
    @Nullable
    public AdvancementProgress cs$getProgress()     { return this.progress; }
    public void cs$setArrivalDir(int dir)           { this.cs_arrivedDir = dir; }
    public int  cs$getArrivalDir()                  { return this.cs_arrivedDir; }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * True when this advancement should appear "locked":
     * – Completed advancements are never locked.
     * – Hidden-flag advancements (json "hidden":true) that haven't been done are.
     * – Any advancement whose direct parent hasn't been completed yet is locked
     *   (the player is more than one step away).
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
            return true;
        }
        return false;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // draw()
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * @author coralinesystems
     * @reason Darkened frame + black-silhouette icon for locked advancements via
     *         RenderSystem shader-colour tinting — no Z-elevated fill geometry,
     *         so the tooltip pass is never occluded.
     */
    @Overwrite
    public void draw(GuiGraphics guiGraphics, int x, int y) {
        boolean obscured = theCoralineSystems$isObscured();

        float pct = this.progress == null ? 0.0F : this.progress.getPercent();
        AdvancementWidgetType frameType = pct >= 1.0F
                ? AdvancementWidgetType.OBTAINED : AdvancementWidgetType.UNOBTAINED;

        if (obscured) {
            // Dark frame — texture alpha is preserved so the rounded corners
            // stay transparent; only the opaque pixels are darkened.
            RenderSystem.setShaderColor(CS_FRAME_DIM, CS_FRAME_DIM, CS_FRAME_DIM, 1.0f);
            guiGraphics.blit(WIDGETS_LOCATION,
                    x + this.x + 3, y + this.y,
                    this.display.getFrame().getTexture(),
                    128 + frameType.getIndex() * 26, 26, 26);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            // Black-silhouette icon — zeroing RGB preserves the item sprite's
            // alpha, so the result is an exact black cutout of the item's shape.
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

        for (AdvancementWidget child : this.children) child.draw(guiGraphics, x, y);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // drawConnectivity()
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * @author coralinesystems
     * @reason Junction-based H-V-H (or V-H-V) bus routing that eliminates
     *         ambiguous chain-like visuals for siblings.
     *
     * <h3>How the bus routing works</h3>
     * Every child knows its travel direction ({@code arrivedDir}, stored during
     * layout).  That direction determines which edge of the parent frame is used
     * as the "junction" — a fixed pixel column (or row) shared by ALL siblings
     * of the same parent.
     *
     * <p>Each sibling's connection is drawn as three segments:
     * <ol>
     *   <li>A short run from the parent centre to the junction.</li>
     *   <li>A bus segment at the junction running perpendicular to the primary
     *       axis from the parent's Y (or X) to the child's Y (or X).  Because
     *       all siblings use the same junction position this segment is naturally
     *       shared and merges into a single visible "bus line".</li>
     *   <li>A run from the junction to the child centre.</li>
     * </ol>
     *
     * <h3>Why the old routing caused chain-like visuals</h3>
     * The previous approach inferred routing direction from the (dx, dy) vector.
     * When a parent has many children spread perpendicular to the primary axis,
     * the farther siblings have |dy| > |dx| and were mis-classified as
     * "vertical-primary", which assigned them a different junction position than
     * their closer siblings.  The resulting vertical segments happened to fall on
     * the same column as the previous child, producing the Porkchop→Cookie→Flower
     * chain illusion.  Storing the direction during layout and reading it back
     * here fixes the classification for every sibling regardless of how far it
     * sits from the parent.
     */
    @Overwrite
    public void drawConnectivity(GuiGraphics guiGraphics, int x, int y, boolean dropShadow) {
        if (this.parent != null) {
            int pX = x + this.parent.getX() + 13;
            int pY = y + this.parent.getY() + 13;
            int cX = x + this.x + 13;
            int cY = y + this.y + 13;

            AdvancementProgress pp = ((IAdvancementWidgetCS) this.parent).getProgress();
            boolean parentDone    = pp != null && pp.isDone();
            boolean childObscured = theCoralineSystems$isObscured();

            int color;
            if (dropShadow) {
                if      (parentDone && !childObscured) color = CS_GREEN_SHADOW;
                else if (childObscured)                color = CS_DARK_SHADOW;
                else                                   color = 0xFF000000;
            } else {
                if      (parentDone && !childObscured) color = CS_GREEN_LINE;
                else if (childObscured)                color = CS_DARK_LINE;
                else                                   color = 0xFFFFFFFF;
            }

            // Read the direction stored during layout (0=R, 1=D, 2=L, 3=U, -1=root).
            // This tells us which parent edge to use as the shared junction so that
            // all siblings — even those far in the perpendicular direction — always
            // use the same junction column/row and share a visible bus segment.
            int dir = ((IAdvancementWidgetCS)(Object)(AdvancementWidget)(Object)this).getArrivalDir();

            // Junction offset from parent centre — just past the frame edge.
            // Frame: blit at widget.x+3, width/height 26 → edge is 16 px from centre.
            // We add 1 px extra clearance so the junction is never inside the frame.
            // RIGHT (0) / LEFT (2) → junction on the horizontal axis (jX).
            // DOWN  (1) / UP   (3) → junction on the vertical axis (jY).
            final int EDGE = 17; // 16 px frame edge + 1 px clearance

            int jX, jY;
            boolean horizPrimary;
            switch (dir) {
                case 0:  jX = pX + EDGE; jY = pY; horizPrimary = true;  break; // RIGHT
                case 2:  jX = pX - EDGE; jY = pY; horizPrimary = true;  break; // LEFT
                case 1:  jX = pX; jY = pY + EDGE; horizPrimary = false; break; // DOWN
                case 3:  jX = pX; jY = pY - EDGE; horizPrimary = false; break; // UP
                default:
                    // Root widget or unknown direction: fall back to a simple
                    // straight line (hLine if same row, vLine if same column,
                    // or H-then-V otherwise).
                    jX = cX; jY = pY;
                    horizPrimary = true;
                    break;
            }

            if (!dropShadow) {
                if (horizPrimary) {
                    // seg1: parent centre → junction  (short horizontal)
                    guiGraphics.hLine(pX, jX, pY, color);
                    // seg2: bus at jX from parent Y to child Y  (shared by siblings)
                    guiGraphics.vLine(jX, pY, cY, color);
                    // seg3: junction → child centre  (individual branch)
                    guiGraphics.hLine(jX, cX, cY, color);
                } else {
                    guiGraphics.vLine(pX, pY, jY, color);
                    guiGraphics.hLine(pX, cX, jY, color);
                    guiGraphics.vLine(cX, jY, cY, color);
                }
            } else {
                // Shadow pass: offset all coordinates by (+1, +1).
                if (horizPrimary) {
                    guiGraphics.hLine(pX + 1, jX + 1, pY + 1, color);
                    guiGraphics.vLine(jX + 1, pY + 1, cY + 1, color);
                    guiGraphics.hLine(jX + 1, cX + 1, cY + 1, color);
                } else {
                    guiGraphics.vLine(pX + 1, pY + 1, jY + 1, color);
                    guiGraphics.hLine(pX + 1, cX + 1, jY + 1, color);
                    guiGraphics.vLine(cX + 1, jY + 1, cY + 1, color);
                }
            }
        }

        for (AdvancementWidget child : this.children) {
            child.drawConnectivity(guiGraphics, x, y, dropShadow);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // drawHover()
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * @author coralinesystems
     * @reason Locked tooltip shows "???" title + description, black-silhouette
     *         icon, and darkened frame.  Unlocked tooltip is a faithful vanilla
     *         re-implementation.  No Z elevation anywhere — tinting via
     *         RenderSystem shader colour is depth-neutral.
     *
     *         flipUp fix: description Y is now relative to {@code boxY} so it
     *         moves with the box when the tooltip flips above the frame.
     */
    @Overwrite
    public void drawHover(GuiGraphics guiGraphics, int x, int y, float fade, int width, int height) {
        if (theCoralineSystems$isObscured()) {
            // ── Locked tooltip ────────────────────────────────────────────────
            Component lockedTitle = Component.literal("???");
            // PIN: to obfuscate the description too, replace Component.literal("???")
            // on the description drawString below with an obfuscated component.

            int textW  = this.minecraft.font.width(lockedTitle);
            int boxW   = 29 + textW + 8;
            int n      = 41; // 26 header + 9 desc line + 6 padding

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

            // Description — shifts with the box when flipUp
            int descY = flipUp ? boxY + 7 : l + 26;
            guiGraphics.drawString(this.minecraft.font,
                    Component.literal("???"),  // ← PIN: swap to obfuscated component if needed
                    m + 5, descY, 0xAAAAAA, false);

            // Icon — black silhouette
            RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
            guiGraphics.renderFakeItem(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            return;
        }

        // ── Unlocked tooltip — vanilla re-implementation ──────────────────────
        boolean bl = width + x + this.x + this.width + 26 >= this.tab.getScreen().width;
        String  progressText = this.progress == null ? null : this.progress.getProgressText();
        int     progressW    = progressText == null ? 0 : this.minecraft.font.width(progressText);
        boolean bl2 = 113 - y - this.y - 26 <= 6 + this.description.size() * 9;
        float   f   = this.progress == null ? 0.0F : this.progress.getPercent();
        int     j   = Mth.floor(f * this.width);

        AdvancementWidgetType awt1, awt2, awt3;
        if (f >= 1.0F) {
            j = this.width / 2;
            awt1 = awt2 = awt3 = AdvancementWidgetType.OBTAINED;
        } else if (j < 2) {
            j = this.width / 2;
            awt1 = awt2 = awt3 = AdvancementWidgetType.UNOBTAINED;
        } else if (j > this.width - 2) {
            j = this.width / 2;
            awt1 = AdvancementWidgetType.OBTAINED;
            awt2 = AdvancementWidgetType.UNOBTAINED;
            awt3 = AdvancementWidgetType.UNOBTAINED;
        } else {
            awt1 = AdvancementWidgetType.OBTAINED;
            awt2 = awt3 = AdvancementWidgetType.UNOBTAINED;
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

    // ──────────────────────────────────────────────────────────────────────────
    // isMouseOver()
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * @author coralinesystems
     * @reason All visible (non-hidden) advancements are hoverable so the player
     *         can read the "???" locked tooltip.
     */
    @Overwrite
    public boolean isMouseOver(int x, int y, int mouseX, int mouseY) {
        int i = x + this.x, j = i + 26;
        int k = y + this.y, l = k + 26;
        return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
    }
}
