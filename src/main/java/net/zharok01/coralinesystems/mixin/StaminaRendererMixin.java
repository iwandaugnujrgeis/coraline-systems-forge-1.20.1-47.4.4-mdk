package net.zharok01.coralinesystems.mixin;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaData;
import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaHelper;
import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaRenderer;
import mod.adrenix.nostalgic.util.common.asset.ModSprite;
import mod.adrenix.nostalgic.util.common.math.MathUtil;
import mod.adrenix.nostalgic.util.common.sprite.GuiSprite;
import mod.adrenix.nostalgic.util.client.renderer.RenderUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.util.CoralineGuiUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.zharok01.coralinesystems.registry.CoralineModSprites;
import net.zharok01.coralinesystems.registry.CoralineEffects;
import net.zharok01.coralinesystems.event.stamina.StaminaClientHudEvents;

@OnlyIn(Dist.CLIENT)
@Mixin(value = StaminaRenderer.class, remap = false)
public abstract class StaminaRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private static void coraline$render(GuiGraphics graphics, int rightHeight, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !StaminaRenderer.isVisible()) return;

        ci.cancel();
        RenderUtil.beginBatching();

        StaminaData data = StaminaHelper.get(player);
        int width  = CoralineGuiUtil.getGuiWidth();
        int height = CoralineGuiUtil.getGuiHeight();
        int left   = width / 2 + 91;
        int top    = height - rightHeight;
        int stamina      = data.getStaminaLevel();
        boolean isCooldown        = data.isCooldown();
        boolean isExhausted       = data.isExhausted();
        boolean hasPositiveEffect = data.hasPositiveEffect(player);
        boolean hasNegativeEffect = data.hasNegativeEffect(player);
        boolean cannotRegain      = data.cannotRegain(player);

        boolean hasPersistence  = player.hasEffect(CoralineEffects.PERSISTENCE.get());

        if ((Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.FLASH_STAMINA_BAR_WHEN_FULL.get()
                && stamina < 20) {
            StaminaRenderer.HAS_BEGUN_TO_DRAIN.enable();
        }

        for (int i = 0; i < 10; i++) {
            GuiSprite sprite = isExhausted ? ModSprite.STAMINA_RECHARGE : ModSprite.STAMINA_LEVEL;
            int x    = left - i * 8 - 9;
            int icon = i * 2 + 1;

            if (hasPositiveEffect) sprite = ModSprite.STAMINA_POSITIVE;
            if (cannotRegain && !(Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIDE_STAMINA_BAR_MOVING.get()) sprite = ModSprite.STAMINA_NEGATIVE;
            if (hasNegativeEffect) sprite = ModSprite.STAMINA_NEGATIVE;
            if (isCooldown && !(Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIDE_STAMINA_BAR_COOLDOWN.get()) sprite = ModSprite.STAMINA_COOLING;

            if (hasPersistence) {
                sprite = isExhausted
                        ? CoralineModSprites.STAMINA_PERSISTENCE_RECHARGE
                        : CoralineModSprites.STAMINA_PERSISTENCE;
            }

            RenderUtil.blitSprite(ModSprite.STAMINA_EMPTY, graphics, x, top);

            if (icon > stamina) {
                sprite = ModSprite.STAMINA_EMPTY;
            } else if (icon == stamina && MathUtil.isOdd(stamina)) {
                sprite = isExhausted ? ModSprite.STAMINA_RECHARGE_HALF : ModSprite.STAMINA_LEVEL_HALF;
                if (hasPositiveEffect)  sprite = ModSprite.STAMINA_POSITIVE_HALF;
                if (cannotRegain && !(Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIDE_STAMINA_BAR_MOVING.get()) sprite = ModSprite.STAMINA_NEGATIVE_HALF;
                if (hasNegativeEffect)  sprite = ModSprite.STAMINA_NEGATIVE_HALF;
                if (isCooldown && !(Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIDE_STAMINA_BAR_COOLDOWN.get()) sprite = ModSprite.STAMINA_COOLING_HALF;

                if (hasPersistence) {
                    sprite = isExhausted
                            ? CoralineModSprites.STAMINA_PERSISTENCE_RECHARGE_HALF
                            : CoralineModSprites.STAMINA_PERSISTENCE_HALF;
                }
            }

            RenderUtil.blitSprite(sprite, graphics, x, top);

            // ── Highlight logic ───────────────────────────────────────────────
            boolean shouldHighlight = false;

            if ((Boolean) mod.adrenix.nostalgic.tweak.config.CandyTweak.HIGHLIGHT_STAMINA_BAR.get() && stamina != 20) {
                shouldHighlight = player.isSprinting();
            }

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

            int flashAt = (Integer) mod.adrenix.nostalgic.tweak.config.CandyTweak.FLASH_STAMINA_BAR_AT.get();
            if (flashAt > 0 && flashAt >= stamina && player.isSprinting()) {
                shouldHighlight = StaminaRenderer.LOW_FLASH_TIMER.getFlag();
            }

            // CORALINE: Fixed Absolute Time Drink Flash
            long currentTime = Util.getMillis();
            if (currentTime < StaminaClientHudEvents.drinkFlashEndTime && stamina < 20) {
                long elapsed = 600 - (StaminaClientHudEvents.drinkFlashEndTime - currentTime);
                // Pulses true/false mathematically every 150ms
                shouldHighlight = (elapsed / 150) % 2 == 0;
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