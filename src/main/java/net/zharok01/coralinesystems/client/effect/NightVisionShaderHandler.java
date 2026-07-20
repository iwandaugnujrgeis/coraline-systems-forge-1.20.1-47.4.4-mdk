package net.zharok01.coralinesystems.client.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NightVisionShaderHandler {

    private static final String NV_SHADER_PATH = "coraline_systems:shaders/post/night_vision.json";
    private static final ResourceLocation NV_SHADER_LOCATION = new ResourceLocation("coraline_systems", "shaders/post/night_vision.json");

    // We track our own state to politely hand off the renderer back to Vanilla
    private static String lastAppliedShader = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Only run once per tick
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        GameRenderer renderer = mc.gameRenderer;
        String currentShader = renderer.currentEffect() == null ? null : renderer.currentEffect().getName();

        // 1. POLITE HANDOFF: If another shader is active (like Creeper Vision) and it's not ours, abort.
        if (currentShader != null && !currentShader.equals(NV_SHADER_PATH)) {
            return;
        }

        // 2. SPECTATOR CHECK: Vanilla clears post-effects for spectators manually, we should respect that.
        if (player.isSpectator()) {
            if (currentShader != null && lastAppliedShader != null) {
                renderer.shutdownEffect();
                lastAppliedShader = null;
            }
            return;
        }

        // 3. STATE SYNC: Clear our tracker if another system shut down our shader forcefully.
        if (currentShader == null && lastAppliedShader != null) {
            lastAppliedShader = null;
        }

        // 4. THE LOGIC: Check for Night Vision and First-Person camera
        boolean hasNightVision = player.hasEffect(MobEffects.NIGHT_VISION);
        boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();

        if (hasNightVision && isFirstPerson) {
            // Apply the shader if we don't already have it
            if (!NV_SHADER_PATH.equals(currentShader)) {
                renderer.loadEffect(NV_SHADER_LOCATION);
                lastAppliedShader = NV_SHADER_PATH;
            }
        } else {
            // Remove the shader if the conditions are no longer met
            if (NV_SHADER_PATH.equals(currentShader)) {
                renderer.shutdownEffect();
                lastAppliedShader = null;

                // Safely restore any entity-specific spectator shaders (e.g., if looking through a Spider)
                renderer.checkEntityPostEffect(mc.getCameraEntity());
            }
        }
    }
}