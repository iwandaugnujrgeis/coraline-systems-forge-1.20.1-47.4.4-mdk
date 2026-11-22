package net.zharok01.coralinesystems.mixin.advancement;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.client.advancement.GridPos;
import net.zharok01.coralinesystems.util.interfaces.IAdvancementWidget;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(AdvancementWidget.class)
@Implements(@Interface(iface = IAdvancementWidget.class, prefix = "coralineApi$"))
public abstract class AdvancementWidgetMixin {

    @Unique private static final float FRAME_DIM = 0.20F;
    @Unique private static final int COMPLETED_LINE = 0xFF55FF55;
    @Unique private static final int COMPLETED_SHADOW = 0xFF004000;
    @Unique private static final int PENDING_LINE = 0xFF404040;
    @Unique private static final int PENDING_SHADOW = 0xFF101010;
    @Unique private static final int FRAME_CENTER_X = 16;
    @Unique private static final int FRAME_CENTER_Y = 13;

    @Unique private static final Component LOCKED_LABEL = Component.literal("???");

    @Mutable @Final @Shadow private int x;
    @Mutable @Final @Shadow private int y;

    @Shadow @Final private static ResourceLocation WIDGETS_LOCATION;
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

    @Unique private List<GridPos> coraline$incomingRoute;

    public void coralineApi$setX(int x) {
        this.x = x;
    }

    public void coralineApi$setY(int y) {
        this.y = y;
    }

    public List<AdvancementWidget> coralineApi$getChildren() {
        return this.children;
    }

    public AdvancementProgress coralineApi$getProgress() {
        return this.progress;
    }

    public AdvancementWidget coralineApi$getParentWidget() {
        return this.parent;
    }

    public void coralineApi$setIncomingRoute(List<GridPos> route) {
        this.coraline$incomingRoute = route;
    }

    public void coralineApi$drawEdges(GuiGraphics guiGraphics, int x, int y, boolean dropShadow, boolean completedPass) {
        if (this.parent != null) {
            boolean completed = this.coraline$isParentDone() && !this.coraline$isObscured();
            if (completed == completedPass) {
                this.coraline$drawRoute(guiGraphics, x, y, dropShadow, completed);
            }
        }
        for (AdvancementWidget child : this.children) {
            ((IAdvancementWidget) child).drawEdges(guiGraphics, x, y, dropShadow, completedPass);
        }
    }

    @Unique
    private boolean coraline$isParentDone() {
        AdvancementProgress parentProgress = ((IAdvancementWidget) this.parent).getProgress();
        return parentProgress != null && parentProgress.isDone();
    }

    @Unique
    private boolean coraline$isObscured() {
        if (this.progress != null && this.progress.isDone()) return false;
        if (this.display.isHidden()) return true;
        if (this.parent == null) return this.advancement.getParent() != null;
        return !this.coraline$isParentDone();
    }

    @Unique
    private void coraline$drawRoute(GuiGraphics guiGraphics, int x, int y, boolean dropShadow, boolean completed) {
        int color = completed
                ? (dropShadow ? COMPLETED_SHADOW : COMPLETED_LINE)
                : (dropShadow ? PENDING_SHADOW : PENDING_LINE);
        int shadowOffset = dropShadow ? 1 : 0;

        int previousX = x + this.parent.getX() + FRAME_CENTER_X;
        int previousY = y + this.parent.getY() + FRAME_CENTER_Y;

        for (GridPos cell : this.coraline$incomingRoute) {
            int cellX = x + cell.x() * GridPos.CELL_WIDTH + FRAME_CENTER_X;
            int cellY = y + cell.y() * GridPos.CELL_HEIGHT + FRAME_CENTER_Y;

            if (previousY == cellY) {
                guiGraphics.fill(Math.min(previousX, cellX), previousY + shadowOffset, Math.max(previousX, cellX) + 1, previousY + 1 + shadowOffset, color);
            } else {
                guiGraphics.fill(previousX + shadowOffset, Math.min(previousY, cellY), previousX + 1 + shadowOffset, Math.max(previousY, cellY) + 1, color);
            }

            previousX = cellX;
            previousY = cellY;
        }
    }

    @Unique
    private void coraline$drawFrame(GuiGraphics guiGraphics, int x, int y, AdvancementWidgetType frameType, boolean obscured) {
        if (obscured) RenderSystem.setShaderColor(FRAME_DIM, FRAME_DIM, FRAME_DIM, 1.0F);
        guiGraphics.blit(WIDGETS_LOCATION, x + this.x + 3, y + this.y, this.display.getFrame().getTexture(), 128 + frameType.getIndex() * 26, 26, 26);
        if (obscured) RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Unique
    private void coraline$drawIcon(GuiGraphics guiGraphics, int x, int y, boolean obscured) {
        if (obscured) RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        guiGraphics.renderFakeItem(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
        if (obscured) RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Overwrite
    public void drawConnectivity(GuiGraphics guiGraphics, int x, int y, boolean dropShadow) {
        IAdvancementWidget self = (IAdvancementWidget) this;
        self.drawEdges(guiGraphics, x, y, dropShadow, false);
        self.drawEdges(guiGraphics, x, y, dropShadow, true);
    }

    @Overwrite
    public void draw(GuiGraphics guiGraphics, int x, int y) {
        boolean obscured = this.coraline$isObscured();
        float percent = this.progress == null ? 0.0F : this.progress.getPercent();
        AdvancementWidgetType frameType = percent >= 1.0F ? AdvancementWidgetType.OBTAINED : AdvancementWidgetType.UNOBTAINED;

        this.coraline$drawFrame(guiGraphics, x, y, frameType, obscured);
        this.coraline$drawIcon(guiGraphics, x, y, obscured);

        for (AdvancementWidget child : this.children) {
            child.draw(guiGraphics, x, y);
        }
    }

    @Overwrite
    public void drawHover(GuiGraphics guiGraphics, int x, int y, float fade, int width, int height) {
        boolean obscured = this.coraline$isObscured();
        Font font = this.minecraft.font;
        FormattedCharSequence lockedText = LOCKED_LABEL.getVisualOrderText();

        int boxWidth = obscured ? 29 + font.width(lockedText) + 8 : this.width;
        FormattedCharSequence titleText = obscured ? lockedText : this.title;
        List<FormattedCharSequence> lines = obscured ? List.of(lockedText) : this.description;
        String progressText = obscured || this.progress == null ? null : this.progress.getProgressText();
        int progressWidth = progressText == null ? 0 : font.width(progressText);
        float percent = obscured || this.progress == null ? 0.0F : this.progress.getPercent();

        boolean flipLeft = width + x + this.x + boxWidth + 26 >= this.tab.getScreen().width;
        boolean flipUp = 113 - y - this.y - 26 <= 6 + lines.size() * font.lineHeight;
        int boxHeight = 32 + lines.size() * font.lineHeight;

        int filled = Mth.floor(percent * boxWidth);
        AdvancementWidgetType leftType;
        AdvancementWidgetType rightType;
        AdvancementWidgetType frameType;
        if (percent >= 1.0F) {
            filled = boxWidth / 2;
            leftType = rightType = frameType = AdvancementWidgetType.OBTAINED;
        } else if (filled < 2) {
            filled = boxWidth / 2;
            leftType = rightType = frameType = AdvancementWidgetType.UNOBTAINED;
        } else if (filled > boxWidth - 2) {
            filled = boxWidth / 2;
            leftType = rightType = AdvancementWidgetType.OBTAINED;
            frameType = AdvancementWidgetType.UNOBTAINED;
        } else {
            leftType = AdvancementWidgetType.OBTAINED;
            rightType = frameType = AdvancementWidgetType.UNOBTAINED;
        }

        int rightWidth = boxWidth - filled;
        int top = y + this.y;
        int boxX = flipLeft ? x + this.x - boxWidth + 26 + 6 : x + this.x;

        RenderSystem.enableBlend();
        if (!lines.isEmpty()) {
            guiGraphics.blitNineSliced(WIDGETS_LOCATION, boxX, flipUp ? top + 26 - boxHeight : top, boxWidth, boxHeight, 10, 200, 26, 0, 52);
        }
        guiGraphics.blit(WIDGETS_LOCATION, boxX, top, 0, leftType.getIndex() * 26, filled, 26);
        guiGraphics.blit(WIDGETS_LOCATION, boxX + filled, top, 200 - rightWidth, rightType.getIndex() * 26, rightWidth, 26);
        this.coraline$drawFrame(guiGraphics, x, y, frameType, obscured);

        if (flipLeft) {
            guiGraphics.drawString(font, titleText, boxX + 5, top + 9, -1);
            if (progressText != null) {
                guiGraphics.drawString(font, progressText, x + this.x - progressWidth, top + 9, -1);
            }
        } else {
            guiGraphics.drawString(font, titleText, x + this.x + 32, top + 9, -1);
            if (progressText != null) {
                guiGraphics.drawString(font, progressText, x + this.x + boxWidth - progressWidth - 5, top + 9, -1);
            }
        }

        int linesY = flipUp ? top + 26 - boxHeight + 7 : top + 26;
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(font, lines.get(i), boxX + 5, linesY + i * font.lineHeight, -5592406, false);
        }

        this.coraline$drawIcon(guiGraphics, x, y, obscured);
    }

    @Overwrite
    public boolean isMouseOver(int x, int y, int mouseX, int mouseY) {
        int left = x + this.x;
        int top = y + this.y;
        return mouseX >= left && mouseX <= left + 26 && mouseY >= top && mouseY <= top + 26;
    }
}