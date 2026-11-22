package net.zharok01.coralinesystems.util.helper;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class StealthHelper {

    public static boolean coraline$isHidden(Entity entity) {
        return entity.isInvisible() && (!(entity instanceof LivingEntity living) || living.getArmorCoverPercentage() == 0.0F);
    }
}