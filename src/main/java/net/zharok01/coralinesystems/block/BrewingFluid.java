package net.zharok01.coralinesystems.block;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum BrewingFluid implements StringRepresentable {
    NONE("none", false),
    JUICE("juice", false),
    TEA("tea", false),
    WINE("wine", true),
    KOMBUCHA("kombucha", true),
    DREGS("dregs", true);

    private final String name;
    private final boolean opaqueOverlay;

    BrewingFluid(String name, boolean opaqueOverlay) {
        this.name = name;
        this.opaqueOverlay = opaqueOverlay;
    }

    public boolean hasOpaqueOverlay() {
        return opaqueOverlay;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static BrewingFluid of(CultureType culture, CultureType impliedCulture, BrewState brewState) {
        if (brewState == BrewState.SPOILED) return DREGS;
        if (brewState == BrewState.FINISHED && culture == CultureType.WINE) return WINE;
        if (brewState == BrewState.FINISHED && culture == CultureType.KOMBUCHA) return KOMBUCHA;

        CultureType branch = culture != CultureType.NONE ? culture : impliedCulture;
        if (branch == CultureType.WINE) return JUICE;
        if (branch == CultureType.KOMBUCHA) return TEA;
        return NONE;
    }
}