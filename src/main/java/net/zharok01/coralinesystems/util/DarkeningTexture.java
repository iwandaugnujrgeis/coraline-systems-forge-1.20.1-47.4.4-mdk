package net.zharok01.coralinesystems.util;

/**
 * Mixed into {@link net.minecraft.client.renderer.texture.DynamicTexture} so the
 * light-map {@code DynamicTexture} specifically (and only that one) can be flagged
 * for post-upload darkening, without affecting any other DynamicTexture instance
 * in the game (item frames, maps, etc. all use this same class).
 */
public interface DarkeningTexture {
    void coralineSystems$enableDarkening();

    boolean coralineSystems$isDarkeningEnabled();
}
