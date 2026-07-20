package net.zharok01.coralinesystems.util.interfaces;

/**
 * Implemented implicitly by PlayerSneakTickMixin (mixed into Player).
 * Lets non-mixin / other-mixin code read the tracked sneak-amount float
 * without casting to the mixin class directly.
 */
public interface SneakAccessor {
    float coralinesystems$getSneakAmount(float partialTick);
}