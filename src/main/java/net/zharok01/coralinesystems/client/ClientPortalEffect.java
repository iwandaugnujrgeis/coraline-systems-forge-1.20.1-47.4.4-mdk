package net.zharok01.coralinesystems.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zharok01.coralinesystems.content.block.StaticPortalBlock;
import net.zharok01.coralinesystems.registry.CoralineBlocks;

import java.util.Random;

/**
 * Renders a TV-static noise overlay that builds up while the player stands in a Static portal,
 * matching the server-side 5-second countdown.
 *
 * HOW TO REGISTER — add these two lines inside your main mod class's clientSetup method:
 *
 *   @SubscribeEvent
 *   public void clientSetup(FMLClientSetupEvent event) {
 *       ClientPortalEffect.init();
 *   }
 *
 * Make sure that method is subscribed on the MOD event bus (the default for @Mod inner classes).
 */
@OnlyIn(Dist.CLIENT)
public class ClientPortalEffect {

    /** Counts up while the player is inside the portal, counts back down when they leave. */
    private static int staticTicks = 0;

    private static final Random RAND = new Random();

    public static void init() {
        // Tick logic runs on the FORGE (game) event bus
        MinecraftForge.EVENT_BUS.addListener(ClientPortalEffect::onClientTick);
        // Overlay registration MUST be on the MOD event bus — this is a Forge 1.20.1 requirement
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientPortalEffect::registerOverlays);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;

        // Check the block at foot-level and head-level so tall portals trigger correctly
        BlockPos feetPos = mc.player.blockPosition();
        boolean inPortal =
                mc.level.getBlockState(feetPos).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get()) ||
                        mc.level.getBlockState(feetPos.above()).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get());

        if (inPortal) {
            // Charge up at the same rate as the server-side countdown
            staticTicks = Math.min(staticTicks + 1, StaticPortalBlock.PORTAL_TICKS_REQUIRED);
        } else {
            // Fade out faster than it fades in — snappy departure feels better than a slow linger
            staticTicks = Math.max(staticTicks - 4, 0);
        }
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("static_portal_noise", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
            if (staticTicks <= 0) return;

            float alpha = staticTicks / (float) StaticPortalBlock.PORTAL_TICKS_REQUIRED;

            // --- Layer 1: scattered noise pixels ---
            // Pixel count scales with intensity: barely visible at first, overwhelming near the end
            int pixelCount = (int) (alpha * alpha * 5000); // squared so it accelerates toward the end
            for (int i = 0; i < pixelCount; i++) {
                int x = RAND.nextInt(screenWidth);
                int y = RAND.nextInt(screenHeight);
                int size = RAND.nextInt(3) + 1;

                // Mix of bright white, mid grey, and dark static noise
                int brightness = RAND.nextInt(256);
                int a = Math.min((int) (alpha * 230), 230);
                int color = (a << 24) | (brightness << 16) | (brightness << 8) | brightness;

                guiGraphics.fill(x, y, x + size, y + size, color);
            }

            // --- Layer 2: full-screen white surge at high intensity ---
            // Kicks in after 70% charge to simulate a building overload before the snap
            if (alpha > 0.7f) {
                float surgeAlpha = (alpha - 0.7f) / 0.3f; // 0.0 at 70%, 1.0 at 100%
                int surgeA = (int) (surgeAlpha * surgeAlpha * 160); // ease-in so it hits hard at the end
                int surgeColor = (surgeA << 24) | 0x00FFFFFF;
                guiGraphics.fill(0, 0, screenWidth, screenHeight, surgeColor);
            }
        });
    }
}
