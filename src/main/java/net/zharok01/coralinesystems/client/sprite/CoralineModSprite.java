package net.zharok01.coralinesystems.client.sprite;

import mod.adrenix.nostalgic.util.common.sprite.GuiSprite;
import mod.adrenix.nostalgic.util.common.sprite.SpriteAtlas;

/**
 * Sprite constants for Coraline Systems' custom HUD icons.
 *
 * These follow the exact same pattern as NT's {@link mod.adrenix.nostalgic.util.common.asset.ModSprite}:
 * {@code GuiSprite.stretch(SpriteAtlas.fromSprite(path, w, h))}.
 * The path string is relative to NT's sprite atlas namespace — BLIND SPOT:
 * we are assuming NT's SpriteAtlas uses a shared atlas that accepts sprites
 * from any namespace path. If NT's atlas is namespace-locked to
 * "nostalgic_tweaks" this will fail silently (missing texture). In that case
 * we would need to register a separate Forge atlas or use a plain
 * ResourceLocation blit instead. Verify in-game on first test.
 *
 * TEXTURE FILES TO CREATE (9×9 px each, placed in your mod's assets):
 *   assets/coraline_systems/textures/gui/sprites/hud/stamina_persistence.png
 *   assets/coraline_systems/textures/gui/sprites/hud/stamina_persistence_half.png
 *   assets/coraline_systems/textures/gui/sprites/hud/stamina_persistence_recharge.png
 *   assets/coraline_systems/textures/gui/sprites/hud/stamina_persistence_recharge_half.png
 *
 * Visual guideline: deep purple (#7B2FBE) tint, same shape as stamina_level.png.
 */
public final class CoralineModSprite {

    private CoralineModSprite() {}

    // ── Persistence (Wine) stamina icons ─────────────────────────────────────

    /** Full icon: player has Persistence and the bar slot is filled. */
    public static final GuiSprite STAMINA_PERSISTENCE =
            GuiSprite.stretch(SpriteAtlas.fromSprite("hud/stamina_persistence", 9, 9));

    /** Half icon: rightmost filled slot is a half-heart equivalent. */
    public static final GuiSprite STAMINA_PERSISTENCE_HALF =
            GuiSprite.stretch(SpriteAtlas.fromSprite("hud/stamina_persistence_half", 9, 9));

    /** Full icon used while exhausted/recharging under Persistence. */
    public static final GuiSprite STAMINA_PERSISTENCE_RECHARGE =
            GuiSprite.stretch(SpriteAtlas.fromSprite("hud/stamina_persistence_recharge", 9, 9));

    /** Half icon used while exhausted/recharging under Persistence. */
    public static final GuiSprite STAMINA_PERSISTENCE_RECHARGE_HALF =
            GuiSprite.stretch(SpriteAtlas.fromSprite("hud/stamina_persistence_recharge_half", 9, 9));
}
