package net.zharok01.coralinesystems.block;

/**
 * Tracks the lifecycle of an in-progress or resolved brew, independent of
 * {@link CultureType} (which tracks *which recipe*, not how far along it
 * is). NONE culture + BREWING state is meaningless and never occurs —
 * state only becomes relevant once a culture has been set (see
 * BrewingCauldronBlockEntity#culture and BrewingCauldronInteractions'
 * addCultureInteraction, which is the only place brewing actually starts).
 * <p>
 * BREWING covers the "stalled" condition from the design doc (Kombucha at
 * light 0) — stalling isn't a distinct state a Player can observe as
 * different from "still brewing normally," it's just BREWING with zero
 * progress accrual for as long as the light stays wrong. No separate
 * STALLED enum value exists for that reason.
 * <p>
 * FINISHED and SPOILED are both terminal — once set, randomTick no longer
 * touches progress (see BrewingCauldronBlock#randomTick's early-return).
 * FINISHED applies to both Wine and Kombucha (the drink differs based on
 * CultureType, not on a Wine-only/Kombucha-only enum split). SPOILED is
 * Wine-only per the design doc's stated asymmetry — Kombucha never
 * spoils — but the enum doesn't enforce that; BrewingCauldronBlock's
 * randomTick logic is what actually withholds SPOILED from the Kombucha
 * branch.
 */
public enum BrewState {
    BREWING,
    FINISHED,
    SPOILED
}