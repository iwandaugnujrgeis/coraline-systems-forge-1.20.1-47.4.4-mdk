package net.zharok01.coralinesystems.util;

/**
 * Exposes the eat-animation scale values from CowMixin to CowModelMixin.
 * Kept separate from CowMilkDuck since the animation is a client concern
 * while milking state is a server concern — clean separation of purpose.
 */
public interface CowEatAnimationDuck {
    float coraline$getHeadEatPositionScale(float partialTick);
    float coraline$getHeadEatAngleScale(float partialTick);

    // Called by AnimalMixin when entity event 10 fires (EatBlockGoal started)
    void coraline$startEatAnimation();

    // Called by AnimalMixin each aiStep to count the animation down
    void coraline$tickEatAnimation();
}