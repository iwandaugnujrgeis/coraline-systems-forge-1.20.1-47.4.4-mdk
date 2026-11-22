package net.zharok01.coralinesystems.registry;

import mod.adrenix.nostalgic.util.common.sprite.GuiSprite;
import mod.adrenix.nostalgic.util.common.sprite.SpriteAtlas;

public final class CoralineModSprites {

    private CoralineModSprites() {}

    public static final GuiSprite STAMINA_PERSISTENCE =
            GuiSprite.stretch(SpriteAtlas.fromSprite("hud/stamina_persistence", 9, 9));

    public static final GuiSprite STAMINA_PERSISTENCE_HALF =
            GuiSprite.stretch(SpriteAtlas.fromSprite("hud/stamina_persistence_half", 9, 9));

    public static final GuiSprite STAMINA_PERSISTENCE_RECHARGE =
            GuiSprite.stretch(SpriteAtlas.fromSprite("hud/stamina_persistence_recharge", 9, 9));

    public static final GuiSprite STAMINA_PERSISTENCE_RECHARGE_HALF =
            GuiSprite.stretch(SpriteAtlas.fromSprite("hud/stamina_persistence_recharge_half", 9, 9));
}
