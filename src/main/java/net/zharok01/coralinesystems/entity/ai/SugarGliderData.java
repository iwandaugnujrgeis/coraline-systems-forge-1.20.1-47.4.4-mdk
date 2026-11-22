package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import net.minecraft.util.Mth;

public final class SugarGliderData {

    public static final int MAX_STICKS = 16;
    private static final String STICK_COUNT_TAG = "CoralineStickCount";

    private SugarGliderData() {
    }

    public static int getSticks(EntitySugarGlider glider) {
        return glider.getPersistentData().getInt(STICK_COUNT_TAG);
    }

    public static void setSticks(EntitySugarGlider glider, int count) {
        glider.getPersistentData().putInt(STICK_COUNT_TAG, Mth.clamp(count, 0, MAX_STICKS));
    }

    public static boolean isSleeping(EntitySugarGlider glider) {
        return ((SleepingSugarGlider) glider).coraline$isSleeping();
    }

    public static void setSleeping(EntitySugarGlider glider, boolean sleeping) {
        ((SleepingSugarGlider) glider).coraline$setSleeping(sleeping);
    }
}