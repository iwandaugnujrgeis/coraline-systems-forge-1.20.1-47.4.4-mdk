package net.zharok01.coralinesystems.mixin.accessor;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaData;
import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(value = StaminaHelper.class, remap = false)
public interface StaminaHelperAccessor {

    @Accessor("PLAYER_DATA")
    static HashMap<String, StaminaData> coraline$getPlayerData() {
        throw new AssertionError();
    }
}