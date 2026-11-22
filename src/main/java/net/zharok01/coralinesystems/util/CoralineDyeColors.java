package net.zharok01.coralinesystems.util;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public enum CoralineDyeColors implements StringRepresentable {

    BRIGHT_GREEN(16, "bright_green", 0x39D42A, MapColor.COLOR_LIGHT_GREEN, 0x39D42A, 0x39D42A),
    SPRING_GREEN(17, "spring_green", 0x4EDE8A, MapColor.COLOR_LIGHT_GREEN, 0x4EDE8A, 0x4EDE8A),
    SKY_BLUE(18, "sky_blue", 0x5BAAED, MapColor.COLOR_LIGHT_BLUE, 0x5BAAED, 0x5BAAED),
    SLATE_BLUE(19, "slate_blue", 0x5B6DB5, MapColor.COLOR_BLUE, 0x5B6DB5, 0x5B6DB5),
    LAVENDER(20, "lavender", 0xAD85E0, MapColor.COLOR_PURPLE, 0xAD85E0, 0xAD85E0),
    ROSE(21, "rose", 0xE8608A, MapColor.COLOR_PINK, 0xE8608A, 0xE8608A),
    WINE(22, "wine", 0x6B1A2E, MapColor.COLOR_RED, 0x6B1A2E, 0x6B1A2E),
    CORAL(23, "coral", 0xE8654A, MapColor.COLOR_ORANGE, 0xE8654A, 0xE8654A),
    PHTALO(24, "phtalo", 0x0D4F3C, MapColor.COLOR_GREEN, 0x0D4F3C, 0x0D4F3C),
    PLUM(25, "plum", 0x4A1060, MapColor.COLOR_PURPLE, 0x4A1060, 0x4A1060),
    TOXIC_YELLOW(26, "toxic_yellow", 0xD4E81A, MapColor.COLOR_YELLOW, 0xD4E81A, 0xD4E81A),
    NAVY_BLUE(27, "navy_blue", 0x0F1F5C, MapColor.COLOR_BLUE, 0x0F1F5C, 0x0F1F5C);

    private final int id;
    private final String name;
    private final MapColor mapColor;
    private final float[] textureDiffuseColors;
    private final int fireworkColor;
    private final int textColor;

    CoralineDyeColors(int id, String name, int rgbColor, MapColor mapColor, int fireworkColor, int textColor) {
        this.id = id;
        this.name = name;
        this.mapColor = mapColor;
        this.fireworkColor = fireworkColor;
        this.textColor = textColor;
        int r = (rgbColor & 0xFF0000) >> 16;
        int g = (rgbColor & 0x00FF00) >> 8;
        int b = (rgbColor & 0x0000FF);
        this.textureDiffuseColors = new float[]{ r / 255.0F, g / 255.0F, b / 255.0F };
    }

    public static boolean isModColor(DyeColor color) {
        return color.getId() > 15;
    }

    public static CoralineDyeColors fromDyeColor(DyeColor color) {
        if (!isModColor(color)) return null;
        for (CoralineDyeColors entry : values()) {
            if (entry.id == color.getId()) return entry;
        }
        return null;
    }

    public DyeColor dyeColor() {
        return DyeColor.valueOf(this.name());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MapColor getMapColor() {
        return mapColor;
    }

    public float[] getTextureDiffuseColors() {
        return textureDiffuseColors;
    }

    public int getFireworkColor() {
        return fireworkColor;
    }

    public int getTextColor() {
        return textColor;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
