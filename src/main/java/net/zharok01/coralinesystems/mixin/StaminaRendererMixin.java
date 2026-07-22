package net.zharok01.coralinesystems.mixin;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaData;
import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaHelper;
import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaRenderer;
import mod.adrenix.nostalgic.util.common.asset.ModSprite;
import mod.adrenix.nostalgic.util.common.math.MathUtil;
import mod.adrenix.nostalgic.util.common.sprite.GuiSprite;
import mod.adrenix.nostalgic.util.client.renderer.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.zharok01.coralinesystems.client.sprite.CoralineModSprite;
import net.zharok01.coralinesystems.registry.CoralineEffects;
import net.zharok01.coralinesystems.event.CoralineClientHudEvents;

/**
 * Mixin into NT's {@link StaminaRenderer#render(GuiGraphics, int)}.
 *
 * We CANCEL the original render entirely and re-implement it, injecting:
 * 1. Purple Persistence icons (shown even at full stamina).
 * 2. A 2-blink white highlight flash triggered by Tea / Mulberry Juice drinks.
 *
 * MAINTENANCE NOTE: If NT updates StaminaRenderer#render(), this Mixin must
 * be updated to match. The method is intentionally structured to make that
 * diff obvious — our additions are marked with "CORALINE:" comments.
 *
 * remap = false because NT's fields/methods are not in Minecraft's mappings.
 */
@OnlyIn(Dist.CLIENT)
@Mixin(value = StaminaRenderer.class, remap = false)
public abstract class StaminaRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private static void coraline$render(GuiGraphics graphics, int rightHeight, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !StaminaRenderer.isVisible()) return;

        // Cancel the original render — we're replacing it fully.
        ci.cancel();

        RenderUtil.beginBatching();

        StaminaData data = StaminaHelper.get(player);
        int width  = net.zharok01.coralinesystems.client.gui.CoralineGuiUtil.getGuiWidth();
        int height = net.zharok01.coralinesystems.client.gui.CoralineGuiUtil.getGuiHeight();
        int left   = width / 2 + 91;
        int top    = height - rightHeight;
        int stamina      = data.getStaminaLevel();
        boolean isCooldown        = data.isCooldown();
        boolean isExhausted       = data.isExhausted();
        boolean hasPositiveEffect = data.hasPositiveEffect(player);
        boolean hasNegativeEffect = data.hasNegativeEffect(player);
        boolean cannotRegain      = data.cannotRegain(player);

        // CORALINE: check for our custom effects.
        boolean hasPersistence  = player.hasEffect(CoralineEffects.PERSISTENCE.get());

        // NT's original drain-tracking flag (we still respect it).
        if ((Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.FLASH_STAMINA_BAR_WHEN_FULL.get()
                && stamina < 20) {
            StaminaRenderer.HAS_BEGUN_TO_DRAIN.enable();
        }

        for (int i = 0; i < 10; i++) {
            // ── Base sprite selection (mirrors NT original exactly) ────────────
            GuiSprite sprite = isExhausted ? ModSprite.STAMINA_RECHARGE : ModSprite.STAMINA_LEVEL;
            int x    = left - i * 8 - 9;
            int icon = i * 2 + 1;

            if (hasPositiveEffect) {
                sprite = ModSprite.STAMINA_POSITIVE;
            }
            if (cannotRegain && !(Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIDE_STAMINA_BAR_MOVING.get()) {
                sprite = ModSprite.STAMINA_NEGATIVE;
            }
            if (hasNegativeEffect) {
                sprite = ModSprite.STAMINA_NEGATIVE;
            }
            if (isCooldown && !(Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIDE_STAMINA_BAR_COOLDOWN.get()) {
                sprite = ModSprite.STAMINA_COOLING;
            }

            // CORALINE: Persistence overrides ALL prior sprite assignments
            if (hasPersistence) {
                sprite = isExhausted
                        ? CoralineModSprite.STAMINA_PERSISTENCE_RECHARGE
                        : CoralineModSprite.STAMINA_PERSISTENCE;
            }

            RenderUtil.blitSprite(ModSprite.STAMINA_EMPTY, graphics, x, top);

            if (icon > stamina) {
                sprite = ModSprite.STAMINA_EMPTY;
            } else if (icon == stamina && MathUtil.isOdd(stamina)) {
                // Half-slot logic (mirrors NT original).
                sprite = isExhausted ? ModSprite.STAMINA_RECHARGE_HALF : ModSprite.STAMINA_LEVEL_HALF;
                if (hasPositiveEffect)  sprite = ModSprite.STAMINA_POSITIVE_HALF;
                if (cannotRegain && !(Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIDE_STAMINA_BAR_MOVING.get()) {
                    sprite = ModSprite.STAMINA_NEGATIVE_HALF;
                }
                if (hasNegativeEffect)  sprite = ModSprite.STAMINA_NEGATIVE_HALF;
                if (isCooldown && !(Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIDE_STAMINA_BAR_COOLDOWN.get()) {
                    sprite = ModSprite.STAMINA_COOLING_HALF;
                }

                // CORALINE: Persistence half-slot override.
                if (hasPersistence) {
                    sprite = isExhausted
                            ? CoralineModSprite.STAMINA_PERSISTENCE_RECHARGE_HALF
                            : CoralineModSprite.STAMINA_PERSISTENCE_HALF;
                }
            }

            RenderUtil.blitSprite(sprite, graphics, x, top);

            // ── Highlight logic ───────────────────────────────────────────────
            boolean shouldHighlight = false;

            if ((Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIGHLIGHT_STAMINA_BAR.get()
                    && stamina != 20) {
                shouldHighlight = player.isSprinting();
            }

            // NT's own full-bar blink (we preserve this unchanged).
            if ((Boolean) StaminaRenderer.HAS_BEGUN_TO_DRAIN.get() && stamina == 20) {
                shouldHighlight = StaminaRenderer.FULL_FLASH_TIMER.getFlag();
            } else {
                StaminaRenderer.FULL_FLASH_TIMER.reset();
            }
            if (StaminaRenderer.FULL_FLASH_TIMER.hasReachedMax()) {
                StaminaRenderer.FULL_FLASH_TIMER.reset();
                StaminaRenderer.HAS_BEGUN_TO_DRAIN.disable();
                shouldHighlight = false;
            }

            // NT's low-stamina sprint flash.
            int flashAt = (Integer) mod.adrenix.nostalgic.tweak.config.CandyTweak.FLASH_STAMINA_BAR_AT.get();
            if (flashAt > 0 && flashAt >= stamina && player.isSprinting()) {
                shouldHighlight = StaminaRenderer.LOW_FLASH_TIMER.getFlag();
            }

            // CORALINE: Drink flash (Tea / Mulberry Juice).
            if (CoralineClientHudEvents.drinkFlashRemaining > 0 && stamina < 20) {
                // We piggyback on NT's FULL_FLASH_TIMER cadence for consistent timing.
                boolean flashNow = StaminaRenderer.FULL_FLASH_TIMER.getFlag();
                if (!flashNow) {
                    // Timer just flipped — count one half-cycle down.
                    CoralineClientHudEvents.drinkFlashRemaining--;
                }
                shouldHighlight = flashNow;
            }

            if (shouldHighlight) {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 1.0F);
                RenderUtil.blitSprite(ModSprite.STAMINA_HIGHLIGHT, graphics, x, top);
                graphics.pose().popPose();
            }
        }

        RenderUtil.endBatching();
    }
}