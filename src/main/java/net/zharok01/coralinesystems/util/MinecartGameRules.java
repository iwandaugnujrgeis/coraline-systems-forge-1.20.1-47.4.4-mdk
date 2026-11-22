package net.zharok01.coralinesystems.util;

import net.minecraft.world.level.GameRules;

public final class MinecartGameRules {

    public static final GameRules.Key<GameRules.IntegerValue> MINECART_MAX_SPEED = GameRules.register(
            "minecartMaxSpeed", GameRules.Category.MISC, GameRules.IntegerValue.create(10));

    public static void bootstrap() {
    }
}