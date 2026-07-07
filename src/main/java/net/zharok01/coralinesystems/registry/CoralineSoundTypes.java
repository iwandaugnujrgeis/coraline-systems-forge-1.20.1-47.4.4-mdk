package net.zharok01.coralinesystems.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;

public class CoralineSoundTypes {

    // Volume and pitch defaults are typically set to 1.0F unless you want specialized feedback (like metal blocks)
    public static final SoundType MUSHROOM = new ForgeSoundType(
            1.0F,
            1.0F,
            CoralineSounds.MUSHROOM_BREAK,
            CoralineSounds.MUSHROOM_STEP,
            CoralineSounds.MUSHROOM_PLACE,
            CoralineSounds.MUSHROOM_HIT,
            CoralineSounds.MUSHROOM_FALL
    );

    public static final SoundType LEAVES = new ForgeSoundType(
            1.0F,
            1.0F,
            CoralineSounds.LEAVES_BREAK,
            CoralineSounds.LEAVES_STEP,
            CoralineSounds.LEAVES_PLACE,
            CoralineSounds.LEAVES_HIT,
            CoralineSounds.LEAVES_FALL
    );
}