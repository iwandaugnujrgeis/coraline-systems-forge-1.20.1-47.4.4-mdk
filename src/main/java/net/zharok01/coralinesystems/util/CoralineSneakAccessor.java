package net.zharok01.coralinesystems.util;

/**
 * Implemented implicitly by PlayerSneakTickMixin (mixed into Player).
 * Lets non-mixin / other-mixin code read the tracked sneak-amount float
 * without casting to the mixin class directly.
 */
public interface CoralineSneakAccessor {
    float coralinesystems$getSneakAmount(float partialTick);
}