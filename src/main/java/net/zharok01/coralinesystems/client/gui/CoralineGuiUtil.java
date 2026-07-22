package net.zharok01.coralinesystems.client.gui;

import mod.adrenix.nostalgic.util.client.gui.GuiUtil;

/**
 * Thin pass-through wrapper around NT's {@link GuiUtil} used by
 * {@link net.zharok01.coralinesystems.mixin.StaminaRendererMixin}.
 *
 * Keeping this as a separate class means the Mixin file doesn't import NT's
 * GuiUtil directly in a remap=false context, which can occasionally cause
 * confusing remapping errors at dev-time.
 *
 * BLIND SPOT: If NT renames GuiUtil or its methods this breaks. Verify on
 * any NT update.
 */
public final class CoralineGuiUtil {

    private CoralineGuiUtil() {}

    public static int getGuiWidth() {
        return GuiUtil.getGuiWidth();
    }

    public static int getGuiHeight() {
        return GuiUtil.getGuiHeight();
    }
}
