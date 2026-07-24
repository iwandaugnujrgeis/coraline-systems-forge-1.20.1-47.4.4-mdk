package net.zharok01.coralinesystems.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.zharok01.coralinesystems.event.stamina.StaminaEvents;

/**
 * Persistence — a beneficial MobEffect applied when the player drinks Wine.
 *
 * The actual stamina drain-halving logic lives in
 * {@link StaminaEvents}, which
 * runs a server-side PlayerTickEvent listener. This class is intentionally
 * a pure flag effect (no applyEffectTick override needed) — the effect's
 * presence on the player is the only signal the tick listener checks.
 *
 * Color: 0x7B2FBE — deep purple, matching the Wine aesthetic.
 * Duration and amplifier are set by WineBottleItem / WineBucketItem at
 * drink time, keyed off the drink's strength level (1–5).
 */
public class PersistenceEffect extends MobEffect {

    public PersistenceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7B2FBE);
    }

    // isDurationEffectTick intentionally not overridden — this effect does
    // nothing server-side per tick by itself. All stamina logic is in the
    // external tick listener, which avoids polluting applyEffectTick with
    // NT-specific imports at the effect class level.
}
