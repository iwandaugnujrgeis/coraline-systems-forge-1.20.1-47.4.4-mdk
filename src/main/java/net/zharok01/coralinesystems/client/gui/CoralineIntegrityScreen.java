package net.zharok01.coralinesystems.client.gui;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.registry.CoralineIntegrityChecker;
import net.zharok01.coralinesystems.util.CoralineLoadingUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CoralineIntegrityScreen extends Screen {

    private static final int TITLE_Y = 40;
    private static final int CONTENT_TOP = 65;
    private static final int CONTENT_BOTTOM_MARGIN = 75;
    private static final int BUTTON_BOTTOM_MARGIN = 62;
    private static final int MAX_TEXT_WIDTH = 320;
    private static final int TEXT_SIDE_MARGIN = 20;
    private static final int LINE_SPACING = 2;
    private static final int SCROLL_STEP = 18;
    private static final int COLOR_HEADER = 0xFFFF55;
    private static final int COLOR_BODY = 0xFFFFFF;
    private static final String TEXT_KEY_PREFIX = "coraline_systems.integrity.reason.";
    private static final String TEXT_KEY_FALLBACK = TEXT_KEY_PREFIX + "fallback";

    private record Line(FormattedCharSequence text, int color) {}

    private final Map<String, List<String>> coraline$namesByReason = new LinkedHashMap<>();
    private final List<Line> coraline$lines = new ArrayList<>();
    private int coraline$wrapWidth;
    private int coraline$scroll;

    public CoralineIntegrityScreen(List<CoralineIntegrityChecker.Issue> issues) {
        super(Component.translatable("coraline_systems.integrity.title"));
        for (CoralineIntegrityChecker.Issue issue : issues) {
            coraline$namesByReason.computeIfAbsent(issue.reason(), reason -> new ArrayList<>()).add(issue.displayName());
        }
    }

    @Override
    protected void init() {
        coraline$scroll = 0;
        coraline$lines.clear();
        coraline$wrapWidth = Math.min(MAX_TEXT_WIDTH, width - TEXT_SIDE_MARGIN * 2);

        coraline$namesByReason.forEach((reason, names) -> {
            String textKey = TEXT_KEY_PREFIX + reason;
            if (!Language.getInstance().has(textKey)) {
                textKey = TEXT_KEY_FALLBACK;
            }

            coraline$addWrapped(Component.translatable("coraline_systems.integrity.affected", String.join(", ", names)), COLOR_HEADER);
            coraline$addWrapped(Component.translatable(textKey), COLOR_BODY);
            coraline$lines.add(new Line(FormattedCharSequence.EMPTY, COLOR_BODY));
        });

        addRenderableWidget(Button.builder(Component.translatable("menu.quit"), button -> minecraft.stop())
                .bounds(width / 2 - 100, height - BUTTON_BOTTOM_MARGIN, 200, 20)
                .build());
    }

    private void coraline$addWrapped(Component text, int color) {
        for (FormattedCharSequence wrapped : font.split(text, coraline$wrapWidth)) {
            coraline$lines.add(new Line(wrapped, color));
        }
    }

    private int coraline$lineHeight() {
        return font.lineHeight + LINE_SPACING;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CoralineLoadingUtils.renderSusBackgroundOnly(guiGraphics, width, height);
        guiGraphics.drawCenteredString(font, title, width / 2, TITLE_Y, COLOR_BODY);

        int textX = (width - coraline$wrapWidth) / 2;
        int y = CONTENT_TOP - coraline$scroll;

        guiGraphics.enableScissor(0, CONTENT_TOP, width, height - CONTENT_BOTTOM_MARGIN);
        for (Line line : coraline$lines) {
            guiGraphics.drawString(font, line.text(), textX, y, line.color(), true);
            y += coraline$lineHeight();
        }
        guiGraphics.disableScissor();

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        CoralineLoadingUtils.renderBrandingOnly(guiGraphics, font, width, height, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int contentHeight = coraline$lines.size() * coraline$lineHeight();
        int maxScroll = Math.max(0, contentHeight - (height - CONTENT_BOTTOM_MARGIN - CONTENT_TOP));
        coraline$scroll = Mth.clamp(coraline$scroll - (int) (delta * SCROLL_STEP), 0, maxScroll);
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            mouseScrolled(0, 0, -1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP) {
            mouseScrolled(0, 0, 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (getFocused() != null && event instanceof FocusNavigationEvent.ArrowNavigation arrow) {
            if (arrow.direction() == ScreenDirection.DOWN) {
                mouseScrolled(0, 0, -1);
                return null;
            }
            if (arrow.direction() == ScreenDirection.UP) {
                mouseScrolled(0, 0, 1);
                return null;
            }
        }
        return super.nextFocusPath(event);
    }
}
