package net.zharok01.coralinesystems.mixin.advancements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

/**
 * Coraline Systems — visual overhaul of the advancement widget.
 *
 * <h3>Changes</h3>
 * <ol>
 *   <li><b>Always render</b> — the vanilla hidden/progress gate on
 *       {@code draw}, {@code drawHover}, and {@code isMouseOver} is removed.
 *       Locked advancements render with a dark icon slot and obfuscated
 *       "???" title on hover.</li>
 *   <li><b>Green connecting lines</b> — {@code drawConnectivity} colours
 *       lines to a child bright green when the parent is already done.</li>
 *   <li><b>Mutable positions</b> — {@code x}/{@code y} are made non-final
 *       via {@code @Mutable} so {@link MixinAdvancementTab}'s layout pass
 *       can reposition every node after the tree is assembled.</li>
 *   <li><b>IAdvancementWidgetCS</b> — soft-implements the interface so
 *       {@link MixinAdvancementTab} can call layout API without reflection.
 *       The {@code cs$} prefix on mixin methods is stripped by Mixin before
 *       matching against the interface (which uses plain names).</li>
 * </ol>
 *
 * <h3>Why {@code isObscured()} has no {@code cs$} prefix</h3>
 * Only methods that implement {@link IAdvancementWidgetCS} carry
 * the {@code cs$} prefix. {@code isObscured()} is a private helper
 * internal to this mixin and must NOT carry the prefix, otherwise Mixin
 * would try — and fail — to match it against the interface.
 */
@Mixin(AdvancementWidget.class)
@Implements(@Interface(iface = IAdvancementWidgetCS.class, prefix = "cs$"))
public abstract class MixinAdvancementWidget {

    // =========================================================================
    // Shadowed vanilla fields
    // =========================================================================

    /** Made mutable so the layout pass can rewrite positions. */
    @Mutable @Shadow private int x;
    /** Made mutable so the layout pass can rewrite positions. */
    @Mutable @Shadow private int y;

    @Shadow @Final private AdvancementTab tab;
    @Shadow @Final private DisplayInfo display;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private FormattedCharSequence title;
    @Shadow @Final private int width;
    @Shadow @Final private List<FormattedCharSequence> description;
    @Shadow @Final private List<AdvancementWidget> children;

    @Shadow @Nullable private AdvancementWidget parent;
    @Shadow @Nullable private AdvancementProgress progress;

    // =========================================================================
    // Constants
    // =========================================================================

    @Unique
    private static final ResourceLocation WIDGETS_LOCATION =
            new ResourceLocation("textures/gui/advancements/widgets.png");

    /** Near-black fill drawn over the icon slot of a locked advancement. */
    @Unique
    private static final int CS_LOCKED_FILL  = 0xFF111111;
    /** Bright green — connecting-line colour when the parent is done. */
    @Unique
    private static final int CS_GREEN_LINE   = 0xFF55FF55;
    /** Dark green — shadow-pass colour when the parent is done. */
    @Unique
    private static final int CS_GREEN_SHADOW = 0xFF004000;

    // =========================================================================
    // IAdvancementWidgetCS soft-implementation  (prefix = "cs$")
    //
    // Mixin strips "cs$" before matching these against the interface, so:
    //   cs$setX      → IAdvancementWidgetCS.setX
    //   cs$setY      → IAdvancementWidgetCS.setY
    //   cs$getChildren → IAdvancementWidgetCS.getChildren
    //   cs$getProgress → IAdvancementWidgetCS.getProgress
    // =========================================================================

    public void cs$setX(int x) { this.x = x; }
    public void cs$setY(int y) { this.y = y; }

    public List<AdvancementWidget> cs$getChildren() { return this.children; }

    @Nullable
    public AdvancementProgress cs$getProgress() { return this.progress; }

    // =========================================================================
    // Private helper — NO cs$ prefix (not part of the interface)
    // =========================================================================

    /**
     * Returns {@code true} when the advancement is flagged {@code hidden: true}
     * in JSON <em>and</em> has not yet been obtained.
     * Visible but unobtained advancements are never obscured.
     */
    @Unique
    private boolean theCoralineSystems$isObscured() {
        return this.display.isHidden()
                && (this.progress == null || !this.progress.isDone());
    }

    // =========================================================================
    // @Overwrite — draw
    // =========================================================================

    /**
     * @reason CS — Remove the hidden/progress gate so every node renders.
     *         Locked advancements cover their icon with a dark fill.
     * @author Coraline Systems
     */
    @Overwrite
    public void draw(GuiGraphics guiGraphics, int x, int y) {
        float pct = this.progress == null ? 0.0F : this.progress.getPercent();
        AdvancementWidgetType frameType = pct >= 1.0F
                ? AdvancementWidgetType.OBTAINED
                : AdvancementWidgetType.UNOBTAINED;

        guiGraphics.blit(WIDGETS_LOCATION,
                x + this.x + 3, y + this.y,
                this.display.getFrame().getTexture(),
                128 + frameType.getIndex() * 26, 26, 26);

        if (theCoralineSystems$isObscured()) {
            guiGraphics.fill(
                    x + this.x + 8,      y + this.y + 5,
                    x + this.x + 8 + 16, y + this.y + 5 + 16,
                    CS_LOCKED_FILL);
        } else {
            guiGraphics.renderFakeItem(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
        }

        for (AdvancementWidget child : this.children) {
            child.draw(guiGraphics, x, y);
        }
    }

    // =========================================================================
    // @Overwrite — drawConnectivity  (green lines when parent is done)
    // =========================================================================

    /**
     * @reason CS — Colour connecting lines green when the parent advancement
     *         is already completed, matching classic Achievement style.
     * @author Coraline Systems
     */
    @Overwrite
    public void drawConnectivity(GuiGraphics guiGraphics, int x, int y, boolean dropShadow) {
        if (this.parent != null) {
            int i = x + this.parent.getX() + 13;
            int j = x + this.parent.getX() + 26 + 4;
            int k = y + this.parent.getY() + 13;
            int l = x + this.x + 13;
            int m = y + this.y + 13;

            // Green when parent is completed, vanilla black/white otherwise.
            AdvancementProgress parentProgress =
                    ((IAdvancementWidgetCS) this.parent).getProgress();
            boolean parentDone = parentProgress != null && parentProgress.isDone();

            int n;
            if (dropShadow) {
                n = parentDone ? CS_GREEN_SHADOW : -16777216;
            } else {
                n = parentDone ? CS_GREEN_LINE   : -1;
            }

            if (dropShadow) {
                guiGraphics.hLine(j, i, k - 1, n);
                guiGraphics.hLine(j + 1, i, k,     n);
                guiGraphics.hLine(j, i, k + 1, n);
                guiGraphics.hLine(l, j - 1, m - 1, n);
                guiGraphics.hLine(l, j - 1, m,     n);
                guiGraphics.hLine(l, j - 1, m + 1, n);
                guiGraphics.vLine(j - 1, m, k, n);
                guiGraphics.vLine(j + 1, m, k, n);
            } else {
                guiGraphics.hLine(j, i, k, n);
                guiGraphics.hLine(l, j, m, n);
                guiGraphics.vLine(j, m, k, n);
            }
        }

        for (AdvancementWidget child : this.children) {
            child.drawConnectivity(guiGraphics, x, y, dropShadow);
        }
    }

    // =========================================================================
    // @Overwrite — drawHover
    // =========================================================================

    /**
     * @reason CS — Show obfuscated "???" tooltip for locked advancements
     *         instead of skipping the hover entirely.
     * @author Coraline Systems
     */
    @Overwrite
    public void drawHover(GuiGraphics guiGraphics, int x, int y, float fade, int width, int height) {

        // --- Locked / obscured branch ----------------------------------------
        if (theCoralineSystems$isObscured()) {
            boolean flipLeft = width + x + this.x + this.width + 26
                    >= this.tab.getScreen().width;
            RenderSystem.enableBlend();

            int l = y + this.y;
            int m = flipLeft ? x + this.x - this.width + 26 + 6 : x + this.x;

            int half  = this.width / 2;
            int other = this.width - half;
            guiGraphics.blit(WIDGETS_LOCATION, m, l,
                    0, AdvancementWidgetType.UNOBTAINED.getIndex() * 26, half, 26);
            guiGraphics.blit(WIDGETS_LOCATION, m + half, l,
                    200 - other, AdvancementWidgetType.UNOBTAINED.getIndex() * 26, other, 26);

            guiGraphics.blit(WIDGETS_LOCATION,
                    x + this.x + 3, y + this.y,
                    this.display.getFrame().getTexture(),
                    128 + AdvancementWidgetType.UNOBTAINED.getIndex() * 26, 26, 26);

            // §k obfuscation through Style — no raw formatting codes needed.
            Component lockedTitle = Component.literal("???")
                    .withStyle(Style.EMPTY.withObfuscated(true));
            int titleX = flipLeft ? m + 5 : x + this.x + 32;
            guiGraphics.drawString(this.minecraft.font, lockedTitle, titleX, y + this.y + 9, -1, false);

            guiGraphics.fill(
                    x + this.x + 8,      y + this.y + 5,
                    x + this.x + 8 + 16, y + this.y + 5 + 16,
                    CS_LOCKED_FILL);
            return;
        }

        // --- Normal / unlocked branch (vanilla logic, unchanged) -------------
        boolean bl  = width + x + this.x + this.width + 26 >= this.tab.getScreen().width;
        String  progressText = this.progress == null ? null : this.progress.getProgressText();
        int     progressW    = progressText == null ? 0 : this.minecraft.font.width(progressText);
        boolean bl2 = 113 - y - this.y - 26 <= 6 + this.description.size() * 9;
        float   f   = this.progress == null ? 0.0F : this.progress.getPercent();
        int     j   = Mth.floor(f * this.width);

        AdvancementWidgetType awt1, awt2, awt3;
        if (f >= 1.0F) {
            j    = this.width / 2;
            awt1 = AdvancementWidgetType.OBTAINED;
            awt2 = AdvancementWidgetType.OBTAINED;
            awt3 = AdvancementWidgetType.OBTAINED;
        } else if (j < 2) {
            j    = this.width / 2;
            awt1 = AdvancementWidgetType.UNOBTAINED;
            awt2 = AdvancementWidgetType.UNOBTAINED;
            awt3 = AdvancementWidgetType.UNOBTAINED;
        } else if (j > this.width - 2) {
            j    = this.width / 2;
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
            if (bl2) {
                guiGraphics.blitNineSliced(WIDGETS_LOCATION, m, l + 26 - n, this.width, n, 10, 200, 26, 0, 52);
            } else {
                guiGraphics.blitNineSliced(WIDGETS_LOCATION, m, l,          this.width, n, 10, 200, 26, 0, 52);
            }
        }

        guiGraphics.blit(WIDGETS_LOCATION, m,     l, 0,         awt1.getIndex() * 26, j, 26);
        guiGraphics.blit(WIDGETS_LOCATION, m + j, l, 200 - k,   awt2.getIndex() * 26, k, 26);
        guiGraphics.blit(WIDGETS_LOCATION, x + this.x + 3, y + this.y,
                this.display.getFrame().getTexture(), 128 + awt3.getIndex() * 26, 26, 26);

        if (bl) {
            guiGraphics.drawString(this.minecraft.font, this.title, m + 5, y + this.y + 9, -1);
            if (progressText != null) {
                guiGraphics.drawString(this.minecraft.font, progressText,
                        x + this.x - progressW, y + this.y + 9, -1);
            }
        } else {
            guiGraphics.drawString(this.minecraft.font, this.title, x + this.x + 32, y + this.y + 9, -1);
            if (progressText != null) {
                guiGraphics.drawString(this.minecraft.font, progressText,
                        x + this.x + this.width - progressW - 5, y + this.y + 9, -1);
            }
        }

        if (bl2) {
            for (int o = 0; o < this.description.size(); o++) {
                guiGraphics.drawString(this.minecraft.font, this.description.get(o),
                        m + 5, l + 26 - n + 7 + o * 9, -5592406, false);
            }
        } else {
            for (int o = 0; o < this.description.size(); o++) {
                guiGraphics.drawString(this.minecraft.font, this.description.get(o),
                        m + 5, y + this.y + 9 + 17 + o * 9, -5592406, false);
            }
        }

        guiGraphics.renderFakeItem(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
    }

    // =========================================================================
    // @Overwrite — isMouseOver
    // =========================================================================

    /**
     * @reason CS — Remove the hidden/progress gate so locked advancements are
     *         hoverable and show the "???" tooltip.
     * @author Coraline Systems
     */
    @Overwrite
    public boolean isMouseOver(int x, int y, int mouseX, int mouseY) {
        int i = x + this.x;
        int j = i + 26;
        int k = y + this.y;
        int l = k + 26;
        return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
    }
}