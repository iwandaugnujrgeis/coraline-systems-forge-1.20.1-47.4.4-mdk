package net.zharok01.coralinesystems.util.compat.subtle_effects;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import einstein.subtle_effects.ticking.tickers.entity.EntityTickerManager;

public final class SubtleEffectsCompat {

    public static final String MOD_ID = "subtle_effects";

    private SubtleEffectsCompat() {
    }

    public static void registerTickers() {
        EntityTickerManager.register(entity -> entity instanceof EntitySugarGlider, SugarGliderSleepingTicker::new);
    }
}