package net.zharok01.coralinesystems.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Haphazard — a harmful MobEffect that serves purely as a flag.
 * The actual input inversion is handled client-side in HaphazardInputHandler,
 * which reads hasEffect() from the synced entity data vanilla already maintains.
 *
 * Color: 0x8B00FF (deep purple, matching the Orb's power field aesthetic).
 */
public class HaphazardEffect extends MobEffect {

    public HaphazardEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B00FF);
    }

    // applyEffectTick is intentionally not overridden: this effect does nothing
    // on the server each tick — it is a pure client-side input flag.
    // isDurationEffectTick() returning false by default ensures no server tick spam.
}