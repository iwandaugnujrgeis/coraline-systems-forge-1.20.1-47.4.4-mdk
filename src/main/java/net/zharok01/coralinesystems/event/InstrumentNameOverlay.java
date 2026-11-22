package net.zharok01.coralinesystems.event;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class InstrumentNameOverlay {

    private static final long DISPLAY_MILLIS = 3000L;
    private static final long FADE_MILLIS = 1000L;
    private static final long COLOR_FADE_MILLIS = 800L;
    private static final int TEXT_BOTTOM_OFFSET = 72;

    private static final Map<String, int[]> PALETTES = new HashMap<>();
    private static final int[] FALLBACK_PALETTE = {0x9AA5B1, 0x8794A1, 0xAEB8C2};

    static {
        // Vanilla
        putPalette("harp", 0x9AB52F, 0x82A02A, 0xB0C93A);
        putPalette("basedrum", 0x5C6BC0, 0x4A59B0, 0x6F7CD0);
        putPalette("snare", 0xC9A56F, 0xB8915A, 0xD9B67D);
        putPalette("hat", 0x14C4D6, 0x00A9C0, 0x2ED8E6);
        putPalette("bass", 0x8D6E63, 0x7A5C52, 0xA07F72);
        putPalette("flute", 0xE9558F, 0xD63F7C, 0xF06FA0);
        putPalette("bell", 0xFFB300, 0xF59E00, 0xFFC21F);
        putPalette("guitar", 0xAB5BBD, 0x9644A8, 0xBD74CC);
        putPalette("chime", 0x3FAEEB, 0x2A98D8, 0x58C0F2);
        putPalette("xylophone", 0xC4AE72, 0xB09A5C, 0xD2BF86);
        putPalette("iron_xylophone", 0x7E96A3, 0x6B8492, 0x93A9B5);
        putPalette("cow_bell", 0xE05656, 0xCC4242, 0xEE6E6A);
        putPalette("didgeridoo", 0xF08000, 0xDB7000, 0xFF9420);
        putPalette("bit", 0x00D46A, 0x00BC5C, 0x25E082);
        putPalette("banjo", 0x00897B, 0x007A6D, 0x1A9E8F);
        putPalette("pling", 0xF0CC00, 0xDDB900, 0xFFD91F);

        // Rosen
        putPalette("accordion", 0xB5483A, 0xA23B30, 0xC85A4A);
        putPalette("beach", 0xF2785C, 0xDE654A, 0xFF8C70);
        putPalette("carillon", 0x9FA8DA, 0x8B95CC, 0xB3BBE6);
        putPalette("music_box", 0xCF7FA8, 0xBB6B94, 0xE095BA);
        putPalette("choir", 0x7986CB, 0x6674BA, 0x8D99DB);
        putPalette("dholak", 0xBF6A2E, 0xAA5A22, 0xD07C40);
        putPalette("kick", 0x3F51B5, 0x32439F, 0x5263C4);
        putPalette("log_drum", 0x9C6B3C, 0x885A2F, 0xAF7C4D);
        putPalette("organ", 0x8E3B5E, 0x7B2F4F, 0xA14D70);
        putPalette("piano", 0x5C7C99, 0x4C6A86, 0x6E8EAB);
        putPalette("power_guitar", 0xE0306A, 0xC9205A, 0xEE4A80);
        putPalette("saxophone", 0xD4A017, 0xBE8F10, 0xE3B232);
        putPalette("sitar", 0xA68B2B, 0x927920, 0xB89E3E);
        putPalette("synth", 0x4D7CFF, 0x3D6BEB, 0x6890FF);
        putPalette("synth_bass", 0x512DA8, 0x452496, 0x6240BA);
        putPalette("timpani", 0xB0643C, 0x9D5530, 0xC27650);
        putPalette("vibraphone", 0x4DB6AC, 0x3DA399, 0x66C4BA);
        putPalette("violin", 0xA0522D, 0x8E4524, 0xB36640);
    }

    private static String name = "";
    private static int[] palette = FALLBACK_PALETTE;
    private static long hideAtMillis;

    private static void putPalette(String instrumentName, int first, int second, int third) {
        PALETTES.put(instrumentName, new int[]{first, second, third});
    }

    public static void show(NoteBlockInstrument instrument) {
        String serializedName = instrument.getSerializedName();
        name = Component.translatable("instrument.coraline_systems." + serializedName).getString();
        palette = PALETTES.getOrDefault(serializedName, FALLBACK_PALETTE);
        hideAtMillis = Util.getMillis() + DISPLAY_MILLIS;
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "instrument_name", InstrumentNameOverlay::render);
    }

    private static void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        long now = Util.getMillis();
        long remaining = hideAtMillis - now;
        if (remaining <= 0L) {
            return;
        }

        int alpha = (int) (255 * Math.min(1.0F, remaining / (float) FADE_MILLIS));
        if (alpha <= 8) {
            return;
        }

        float position = (now % (COLOR_FADE_MILLIS * palette.length)) / (float) COLOR_FADE_MILLIS;
        int from = (int) position;
        float blend = position - from;
        float smooth = blend * blend * (3.0F - 2.0F * blend);

        int a = palette[from];
        int b = palette[(from + 1) % palette.length];
        int red = (int) Mth.lerp(smooth, a >> 16 & 0xFF, b >> 16 & 0xFF);
        int green = (int) Mth.lerp(smooth, a >> 8 & 0xFF, b >> 8 & 0xFF);
        int blue = (int) Mth.lerp(smooth, a & 0xFF, b & 0xFF);

        Font font = gui.getFont();
        int x = (screenWidth - font.width(name)) / 2;
        int y = screenHeight - TEXT_BOTTOM_OFFSET;

        guiGraphics.drawString(font, name, x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
    }
}