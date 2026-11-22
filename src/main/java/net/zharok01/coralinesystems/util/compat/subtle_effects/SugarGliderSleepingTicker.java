package net.zharok01.coralinesystems.util.compat.subtle_effects;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import einstein.subtle_effects.ticking.tickers.entity.sleeping.SleepingTicker;
import net.zharok01.coralinesystems.entity.ai.SugarGliderData;

public class SugarGliderSleepingTicker extends SleepingTicker<EntitySugarGlider> {

    public SugarGliderSleepingTicker(EntitySugarGlider glider) {
        super(glider);
    }

    @Override
    protected boolean isSleeping() {
        return SugarGliderData.isSleeping(this.entity);
    }
}