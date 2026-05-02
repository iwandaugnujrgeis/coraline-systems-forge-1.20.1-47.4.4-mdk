package net.zharok01.coralinesystems.client.fog;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CoralineFogEvents {

    /**
     * Tick the morning fog intensity interpolator once per client game tick.
     * ClientTickEvent fires only when the game is not paused, matching Fog Looks Good Now's
     * GameRenderer.tick() injection which has the same property.
     */
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MorningFogManager.tick();
        }
    }

    /**
     * Adjust fog planes for morning mist.
     *
     * Priority.LOW runs AFTER Fog Looks Good Now's NORMAL-priority handler,
     * so we read the fog values FLGN already set and nudge them further in.
     * This means the two systems compose cleanly: FLGN handles biome and
     * underground fog; we layer morning fog on top.
     *
     * We skip the effect when:
     *   - the camera is inside a fluid (water/lava handle their own fog)
     *   - intensity is effectively zero (night or midday)
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return;

        float intensity = MorningFogManager.getIntensity((float) event.getPartialTick());
        if (intensity <= 0.001F) return;

        float renderDistance = event.getRenderer().getRenderDistance();

        // Read the values already in place, so we compose on top of them
        float currentStart = RenderSystem.getShaderFogStart();
        float currentEnd   = RenderSystem.getShaderFogEnd();

        float morningEnd   = renderDistance * MorningFogManager.MORNING_FOG_END;
        float morningStart = renderDistance * MorningFogManager.MORNING_FOG_START;

        // Only tighten: never push the fog further out than Fog Looks Good Now set it
        // This prevents morning fog from making a dense-biome fog even sparser
        float newEnd   = Math.min(currentEnd,   Mth.lerp(intensity, currentEnd,   morningEnd));
        float newStart = Math.min(currentStart, Mth.lerp(intensity, currentStart, morningStart));

        RenderSystem.setShaderFogEnd(newEnd);
        RenderSystem.setShaderFogStart(newStart);
    }

}