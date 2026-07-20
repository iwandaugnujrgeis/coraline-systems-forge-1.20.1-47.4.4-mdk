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
import net.zharok01.coralinesystems.client.advancements.GridPos;
import net.zharok01.coralinesystems.util.interfaces.IAdvancementWidget;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(AdvancementWidget.class)
@Implements(@Interface(iface = IAdvancementWidget.class, prefix = "cs$"))
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

    @Unique private int cs_arrivedDir = -1;
    @Unique @Nullable private List<GridPos> cs_incomingRoute = null;

    @Unique private static final ResourceLocation WIDGETS_LOCATION =
            new ResourceLocation("textures/gui/advancements/widgets.png");

    @Unique private static final float CS_FRAME_DIM = 0.20f;
    @Unique private static final int CS_GREEN_LINE   = 0xFF55FF55;
    @Unique private static final int CS_GREEN_SHADOW = 0xFF004000;
    @Unique private static final int CS_DARK_LINE    = 0xFF404040;
    @Unique private static final int CS_DARK_SHADOW  = 0xFF101010;

    @Unique private static final int FRAME_INSET_X = 3;
    @Unique private static final int FRAME_SIZE    = 26;
    @Unique private static final int FRAME_HALF    = 13;

    @Unique private static final int CS_SLOT_W = 34;
    @Unique private static final int CS_SLOT_H = 30;

    // ── IAdvancementWidget ──────────────────────────────────────────────────

    public void cs$setX(int x)                       { this.x = x; }
    public void cs$setY(int y)                       { this.y = y; }
    public List<AdvancementWidget> cs$getChildren()  { return this.children; }
    @Nullable
    public AdvancementProgress cs$getProgress()      { return this.progress; }
    @Nullable
    public AdvancementWidget cs$getParentWidget()    { return this.parent; }
    public void cs$setArrivalDir(int dir)            { this.cs_arrivedDir = dir; }
    public int  cs$getArrivalDir()                   { return this.cs_arrivedDir; }

    public void cs$setIncomingRoute(@Nullable List<GridPos> route) {
        this.cs_incomingRoute = route;
    }

    @Nullable
    public List<GridPos> cs$getIncomingRoute() {
        return this.cs_incomingRoute;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @Unique
    private boolean theCoralineSystems$isObscured() {
        if (this.progress != null && this.progress.isDone()) return false;
        if (this.display.isHidden()) return true;
        Advancement parentAdv = this.advancement.getParent();
        if (parentAdv != null) {
            AdvancementWidget pw = this.tab.getWidget(parentAdv);
            if (pw != null) {
                AdvancementProgress pp = ((IAdvancementWidget) pw).getProgress();
                return pp == null || !pp.isDone();
            }
            return true;
        }
        return false;
    }

    @Unique
    private boolean theCoralineSystems$isGreenEdge() {
        assert this.parent != null;
        AdvancementProgress pp = ((IAdvancementWidget) this.parent).getProgress();
        return pp != null && pp.isDone() && !this.theCoralineSystems$isObscured();
    }

    @Unique
    private static int theCoralineSystems$lineColor(boolean dropShadow, boolean green) {
        if (green) return dropShadow ? CS_GREEN_SHADOW : CS_GREEN_LINE;
        else       return dropShadow ? CS_DARK_SHADOW  : CS_DARK_LINE;
    }

    // ── Cell-walking renderer ─────────────────────────────────────────────────

    @Unique
    private void theCoralineSystems$drawRouteLines(GuiGraphics g, int ox, int oy,
                                                   boolean dropShadow, int color) {
        List<GridPos> route = this.cs_incomingRoute;
        if (route == null || route.isEmpty() || this.parent == null) return;

        final int d = dropShadow ? 1 : 0;

        final int pCX = ox + this.parent.getX() + FRAME_INSET_X + FRAME_HALF;
        final int pCY = oy + this.parent.getY() + FRAME_HALF;

        int prevX = pCX;
        int prevY = pCY;

        for (GridPos cell : route) {
            int cellPX = ox + cell.x() * CS_SLOT_W + FRAME_INSET_X + FRAME_HALF;
            int cellPY = oy + cell.y() * CS_SLOT_H + FRAME_HALF;

            int minX = Math.min(prevX, cellPX);
            int maxX = Math.max(prevX, cellPX);
            int minY = Math.min(prevY, cellPY);
            int maxY = Math.max(prevY, cellPY);

            // Using explicit fill() bypasses Vanilla's vLine off-by-one bug
            // entirely. This mathematical strictness eliminates T-junction stubs.
            if (prevY == cellPY) {
                // Horizontal segment: inclusive of maxX
                g.fill(minX, prevY + d, maxX + 1, prevY + 1 + d, color);
            } else {
                // Vertical segment: inclusive of maxY
                g.fill(prevX + d, minY, prevX + 1 + d, maxY + 1, color);
            }

            prevX = cellPX;
            prevY = cellPY;
        }
    }

    // ── drawConnectivityCS() ──────────────────────────────────────────────────

    public void cs$drawConnectivityCS(GuiGraphics guiGraphics, int x, int y,
                                      boolean dropShadow, boolean greenOnly) {
        if (this.parent != null) {
            boolean green = this.theCoralineSystems$isGreenEdge();
            if (green == greenOnly) {
                this.theCoralineSystems$drawRouteLines(
                        guiGraphics, x, y, dropShadow,
                        theCoralineSystems$lineColor(dropShadow, green));
            }
        }
        for (AdvancementWidget child : this.children) {
            ((IAdvancementWidget) child).drawConnectivityCS(
                    guiGraphics, x, y, dropShadow, greenOnly);
        }
    }

    // ── drawConnectivity() — vanilla entry point ──────────────────────────────

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void drawConnectivity(GuiGraphics guiGraphics, int x, int y, boolean dropShadow) {
        if (this.parent != null) {
            boolean green = this.theCoralineSystems$isGreenEdge();
            this.theCoralineSystems$drawRouteLines(
                    guiGraphics, x, y, dropShadow,
                    theCoralineSystems$lineColor(dropShadow, green));
        }
        for (AdvancementWidget child : this.children)
            child.drawConnectivity(guiGraphics, x, y, dropShadow);
    }

    // ── draw() ────────────────────────────────────────────────────────────────

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void draw(GuiGraphics guiGraphics, int x, int y) {
        boolean obscured = this.theCoralineSystems$isObscured();

        float pct = this.progress == null ? 0.0F : this.progress.getPercent();
        AdvancementWidgetType frameType = pct >= 1.0F
                ? AdvancementWidgetType.OBTAINED : AdvancementWidgetType.UNOBTAINED;

        if (obscured) {
            RenderSystem.setShaderColor(CS_FRAME_DIM, CS_FRAME_DIM, CS_FRAME_DIM, 1.0f);
            guiGraphics.blit(WIDGETS_LOCATION,
                    x + this.x + 3, y + this.y,
                    this.display.getFrame().getTexture(),
                    128 + frameType.getIndex() * 26, 26, 26);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

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
     * @author
     * @reason
     */
    @Overwrite
    public void drawHover(GuiGraphics guiGraphics, int x, int y,
                          float fade, int width, int height) {

        if (this.theCoralineSystems$isObscured()) {
            Component lockedTitle = Component.literal("???");

            int textW = this.minecraft.font.width(lockedTitle);
            int boxW  = 29 + textW + 8;
            int n     = 41;

            boolean flipLeft = width + x + this.x + boxW + 26 >= this.tab.getScreen().width;
            boolean flipUp   = 113 - y - this.y - 26 <= 6 + 9;

            RenderSystem.enableBlend();

            int l    = y + this.y;
            int m    = flipLeft ? x + this.x - boxW + 26 + 6 : x + this.x;
            int boxY = flipUp   ? l + 26 - n : l;

            guiGraphics.blitNineSliced(WIDGETS_LOCATION, m, boxY, boxW, n, 10, 200, 26, 0, 52);

            int j = boxW / 2, k = boxW - j;
            guiGraphics.blit(WIDGETS_LOCATION, m,     l, 0,       AdvancementWidgetType.UNOBTAINED.getIndex() * 26, j, 26);
            guiGraphics.blit(WIDGETS_LOCATION, m + j, l, 200 - k, AdvancementWidgetType.UNOBTAINED.getIndex() * 26, k, 26);

            RenderSystem.setShaderColor(CS_FRAME_DIM, CS_FRAME_DIM, CS_FRAME_DIM, 1.0f);
            guiGraphics.blit(WIDGETS_LOCATION,
                    x + this.x + 3, y + this.y,
                    this.display.getFrame().getTexture(),
                    128 + AdvancementWidgetType.UNOBTAINED.getIndex() * 26, 26, 26);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            int titleX = flipLeft ? m + 5 : x + this.x + 32;
            guiGraphics.drawString(this.minecraft.font, lockedTitle, titleX, y + this.y + 9, -1, false);

            int descY = flipUp ? boxY + 7 : l + 26;
            guiGraphics.drawString(this.minecraft.font,
                    Component.literal("???"),
                    m + 5, descY, 0xAAAAAA, false);

            RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
            guiGraphics.renderFakeItem(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            return;
        }

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
            awt2 = AdvancementWidgetType.OBTAINED;
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

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean isMouseOver(int x, int y, int mouseX, int mouseY) {
        int i = x + this.x, j = i + 26;
        int k = y + this.y, l = k + 26;
        return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
    }
}