package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.CryptidEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class CryptidThrowPlayerGoal extends Goal {

    private static final double PANIC_RADIUS = 2.5D;
    private static final double PANIC_RADIUS_SQ = PANIC_RADIUS * PANIC_RADIUS;

    private static final double MIN_LAUNCH_Y = 1.789D;
    private static final double MAX_LAUNCH_Y = 2.000D;

    private static final int COOLDOWN_TICKS = 100;

    private final CryptidEntity cryptid;
    private int cooldown;

    @Nullable
    private Player victim;

    public CryptidThrowPlayerGoal(CryptidEntity cryptid) {
        this.cryptid = cryptid;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private static boolean isValidTarget(Player player) {
        return !player.isCreative() && !player.isSpectator() && player.isAlive();
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        Player nearest = this.cryptid.level().getNearestPlayer(this.cryptid, PANIC_RADIUS);
        if (nearest == null || !isValidTarget(nearest)) return false;
        if (this.cryptid.distanceToSqr(nearest) > PANIC_RADIUS_SQ) return false;

        this.victim = nearest;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        this.cryptid.getNavigation().stop();

        this.cryptid.startThrowAnim();

        if (this.victim != null) {
            launchVictim();
            spawnLaunchEffects();
        }
    }

    @Override
    public void stop() {
        this.victim = null;
        this.cooldown = COOLDOWN_TICKS;
    }

    private void launchVictim() {
        double launchY = MIN_LAUNCH_Y
                + this.cryptid.getRandom().nextDouble() * (MAX_LAUNCH_Y - MIN_LAUNCH_Y);

        this.victim.setDeltaMovement(0.0D, launchY, 0.0D);
        this.victim.hurtMarked = true;
    }

    private void spawnLaunchEffects() {
        if (!(this.cryptid.level() instanceof ServerLevel serverLevel)) return;

        double x = this.victim.getX();
        double y = this.victim.getY();
        double z = this.victim.getZ();

        serverLevel.sendParticles(ParticleTypes.POOF,         x, y + 0.1, z, 12, 0.3, 0.1, 0.3, 0.15);
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y + 0.8, z,  4, 0.4, 0.2, 0.4, 0.0);

        serverLevel.playSound(
                null, x, y, z,
                SoundEvents.EGG_THROW,
                SoundSource.HOSTILE,
                1.2F,
                0.6F + this.cryptid.getRandom().nextFloat() * 0.3F
        );
    }
}