package net.zharok01.coralinesystems.event.stamina;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

import java.util.Set;

/**
 * Handles client-side GUI manipulation on the Forge Event Bus.
 */
@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class StaminaClientGuiEvents {

    private static final int RAISE_PX = -6;

    /**
     * A collection of exact Vanilla overlay IDs we want to target.
     */
    private static final Set<ResourceLocation> TARGET_OVERLAYS = Set.of(
            VanillaGuiOverlay.HOTBAR.id(),
            VanillaGuiOverlay.PLAYER_HEALTH.id(),
            VanillaGuiOverlay.FOOD_LEVEL.id(),
            VanillaGuiOverlay.ARMOR_LEVEL.id(),
            VanillaGuiOverlay.AIR_LEVEL.id(),
            VanillaGuiOverlay.MOUNT_HEALTH.id(),
            VanillaGuiOverlay.JUMP_BAR.id(),
            VanillaGuiOverlay.EXPERIENCE_BAR.id()
    );

    /**
     * A strict dictionary of standalone keywords to catch any modded variants.
     */
    private static final Set<String> TARGET_KEYWORDS = Set.of(
            "armor", "air", "health", "food", "stamina", "thirst",
            "mana", "hunger", "hearts", "bubbles", "mount", "jump",
            "experience", "xp", "hotbar"
    );

    @SubscribeEvent
    public static void onPreRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (shouldRaiseOverlay(event.getOverlay().id())) {
            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(0, RAISE_PX, 0);
        }
    }

    @SubscribeEvent
    public static void onPostRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (shouldRaiseOverlay(event.getOverlay().id())) {
            event.getGuiGraphics().pose().popPose();
        }
    }

    /**
     * Determines whether the current overlay should be shifted upwards.
     */
    private static boolean shouldRaiseOverlay(ResourceLocation id) {
        // 1. Check exact Vanilla overlays
        if (TARGET_OVERLAYS.contains(id)) {
            return true;
        }

        // 2. Tokenize the path to safely check for modded replacements
        // This splits "nostalgic_tweaks:classic_air_bubbles" into ["classic", "air", "bubbles"]
        // ensuring we catch "air" but completely ignore "crosshair".
        String[] parts = id.getPath().toLowerCase().split("[_\\-/.]");
        for (String part : parts) {
            if (TARGET_KEYWORDS.contains(part)) {
                return true;
            }
        }

        return false;
    }
}