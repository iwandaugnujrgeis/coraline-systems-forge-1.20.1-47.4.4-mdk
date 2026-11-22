package net.zharok01.coralinesystems.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CoralineClientGuiEvents {

    private static final int OVERLAY_Y_OFFSET = -3;

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

    private static final Set<String> TARGET_KEYWORDS = Set.of(
            "armor", "air", "health", "food", "stamina", "thirst",
            "mana", "hunger", "hearts", "bubbles", "mount", "jump",
            "experience", "xp", "hotbar"
    );

    private static final Map<ResourceLocation, Boolean> OVERLAY_MATCH_CACHE = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPreRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (!coraline$shouldRaiseOverlay(event.getOverlay().id())) return;

        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(0, OVERLAY_Y_OFFSET, 0);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPostRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (coraline$shouldRaiseOverlay(event.getOverlay().id())) {
            event.getGuiGraphics().pose().popPose();
        }
    }

    private static boolean coraline$shouldRaiseOverlay(ResourceLocation id) {
        return OVERLAY_MATCH_CACHE.computeIfAbsent(id, key -> {
            if (TARGET_OVERLAYS.contains(key)) return true;

            for (String part : key.getPath().toLowerCase().split("[_\\-/.]")) {
                if (TARGET_KEYWORDS.contains(part)) return true;
            }
            return false;
        });
    }
}