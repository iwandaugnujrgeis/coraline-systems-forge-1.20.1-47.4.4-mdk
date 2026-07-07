package net.zharok01.coralinesystems.block;

/**
 * Marker interface shared by {@link MaglevRailBlock} and
 * {@link PoweredMaglevRailBlock}.
 *
 * Its only purpose is to give {@code AbstractMinecartMixin} a single
 * {@code instanceof} check that covers both rail types, even though they
 * extend two different vanilla superclasses ({@code BaseRailBlock} and
 * {@code PoweredRailBlock} respectively) and therefore share no common
 * mod-specific ancestor.
 *
 * IMPORTANT: any future maglev-family rail block must implement this
 * interface, or the slope-correction guard in AbstractMinecartMixin will
 * silently skip it — this is exactly what caused the lap-2 "invisible wall"
 * bug when PoweredMaglevRailBlock was missing it.
 */
public interface IMaglevRail {
}