package net.zharok01.coralinesystems.block;

/**
 * Tracks which culture (if any) has been added to a BrewingCauldron, and
 * therefore which recipe branch (Wine vs. Kombucha) the cauldron is
 * currently committed to. NONE means no culture has been added yet — the
 * cauldron is still in the "add solids" phase and can still accept either
 * Mulberries or Tea Leaves.
 * <p>
 * Once a culture is set (Yeast -> WINE, Dregs -> KOMBUCHA), the cauldron is
 * locked into that recipe branch for the rest of the brew — see
 * BrewingCauldronBlockEntity for the solid-ingredient rejection logic this
 * enables (e.g. refusing Tea Leaves once WINE is set).
 */
public enum CultureType {
    NONE,
    WINE,
    KOMBUCHA
}
