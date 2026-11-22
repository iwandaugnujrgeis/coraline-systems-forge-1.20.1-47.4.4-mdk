package net.zharok01.coralinesystems.util;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class CoralineFishingTuning {
    public static final double AIR_GRAVITY = 0.08;
    public static final double AIR_VERTICAL_DRAG = 0.98;
    public static final double BITE_REACH_SQR = 0.5;
    public static final double DIRECT_STEER_RANGE = 3.0;
    public static final double FINAL_APPROACH_RESPONSIVENESS = 0.3;
    public static final double FINAL_APPROACH_SPEED = 0.12;
    public static final double FISH_NOTICE_HEIGHT = 8.0;
    public static final double FISH_NOTICE_RADIUS = 20.0;
    public static final double FLEE_EXTRA_SWIM_ACCELERATION = 0.035;
    public static final double FLEE_SPEED_MODIFIER = 1.5;
    public static final double HESITATION_DRAG = 0.85;
    public static final double HOOKED_ESCAPE_DISTANCE_SQR = 20.0 * 20.0;
    public static final double HOOKED_FLEE_RESPONSIVENESS = 0.2;
    public static final double HOOKED_FLEE_SPEED = 0.18;
    public static final double LURE_DEPTH_OFFSET = 0.3;
    public static final double LURE_EXTRA_SWIM_ACCELERATION = 0.02;
    public static final double LURE_SPEED_MODIFIER = 1.0;
    public static final double REEL_FLIGHT_TICKS_PER_BLOCK = 0.7;
    public static final double REEL_FLIGHT_TICKS_PER_RISE_BLOCK = 1.0;
    public static final double REEL_LANDING_OFFSET = 1.0;
    public static final double REEL_MAX_DROP = 6.0;
    public static final double REEL_MAX_FLIGHT_TICKS = 22.0;
    public static final double REEL_MAX_RANGE = 14.0;
    public static final double REEL_MAX_RISE = 4.0;
    public static final double REEL_MIN_FLIGHT_TICKS = 6.0;
    public static final double REEL_RESPONSIVENESS = 0.5;
    public static final double REEL_SPEED = 0.45;
    public static final double REEL_STOP_DISTANCE = 1.5;
    public static final double REEL_WATER_LIFT = 0.4;
    public static final float FISH_HITBOX_SCALE = 0.6F;
    public static final float FLEE_DETECTION_RADIUS = 8.0F;
    public static final float HESITATION_CHANCE = 0.4F;
    public static final float MAX_NOTICE_CHANCE = 0.9F;
    public static final float NOTICE_CHANCE = 0.35F;
    public static final float NOTICE_CHANCE_PER_LURE_LEVEL = 0.1F;
    public static final int APPROACH_TIMEOUT_TICKS = 160;
    public static final int FISH_CATCH_ROD_DAMAGE = 1;
    public static final int HESITATION_MAX_TICKS = 50;
    public static final int HESITATION_MIN_TICKS = 15;
    public static final int INITIAL_NOTICE_DELAY_TICKS = 8;
    public static final int LURE_REPATH_INTERVAL_TICKS = 10;
    public static final int MAX_INTERESTED_FISH = 3;
    public static final int NOTICE_CHECK_INTERVAL_TICKS = 10;
    public static final int NOTICE_NEAREST_POOL = 3;
    public static final int REEL_LANDING_GRACE_TICKS = 3;
    public static final int REEL_MAX_TICKS = 60;

    private CoralineFishingTuning() {
    }

    public static boolean isScaryToFish(LivingEntity entity) {
        if (!EntitySelector.NO_SPECTATORS.test(entity)) {
            return false;
        }
        return !(entity instanceof Player player && player.fishing != null && !player.isSprinting() && !player.isInWater());
    }
}
