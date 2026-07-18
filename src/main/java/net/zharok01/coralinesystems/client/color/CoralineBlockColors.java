package net.zharok01.coralinesystems.client.color;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.block.BrewState;
import net.zharok01.coralinesystems.block.BrewingCauldronBlockEntity;
import net.zharok01.coralinesystems.block.CultureType;
import org.jetbrains.annotations.Nullable;

public final class CoralineBlockColors {

    // ── Mulberry Juice gradient (pre-culture / still-BREWING Wine branch) ──
    private static final int MULBERRY_JUICE_START = 0xe597e8;
    private static final int MULBERRY_JUICE_END = 0xcb67cf;

    // ── Wine gradient (culture=WINE, state=FINISHED) ────────────────────
    private static final int WINE_START = 0xb93dbf;
    private static final int WINE_END = 0x7a1f80;

    // ── Tea gradient (pre-culture / still-BREWING Kombucha branch) ──────
    private static final int TEA_START = 0xffb866;
    private static final int TEA_END = 0xed8f21;

    // ── Flat colors (no strength gradient) ──────────────────────────────
    private static final int KOMBUCHA_COLOR = 0xffcc7a;
    private static final int DREGS_COLOR = 0xe3ed87;

    private static final int NO_TINT = -1;

    private CoralineBlockColors() {
    }

    public static final BlockColor CAULDRON_CONTENT = (BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) -> {
        // tintIndex 0 is the fluid surface quad (#content element).
        // Any other index belongs to the cauldron shell and should not be tinted.
        if (tintIndex != 0) return NO_TINT;

        if (level == null || pos == null) {
            return NO_TINT;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BrewingCauldronBlockEntity be)) {
            return NO_TINT;
        }

        CultureType culture = be.getCulture();
        BrewState brewState = be.getBrewState();
        int strength = be.getSolidStrength();

        if (brewState == BrewState.SPOILED) {
            return DREGS_COLOR;
        }

        if (culture == CultureType.WINE && brewState == BrewState.FINISHED) {
            return CoralineTintUtils.lerpStrength(WINE_START, WINE_END, strength);
        }

        if (culture == CultureType.KOMBUCHA && brewState == BrewState.FINISHED) {
            return KOMBUCHA_COLOR;
        }

        // culture == NONE (pre-culture drinks) or state == BREWING for
        // either branch: show the un-fermented gradient per design.
        CultureType branch = culture != CultureType.NONE ? culture : be.getImpliedCulture();

        if (branch == CultureType.WINE) {
            return CoralineTintUtils.lerpStrength(MULBERRY_JUICE_START, MULBERRY_JUICE_END, strength);
        }
        if (branch == CultureType.KOMBUCHA) {
            return CoralineTintUtils.lerpStrength(TEA_START, TEA_END, strength);
        }

        // No branch implied yet — fail safe.
        return NO_TINT;
    };
}
