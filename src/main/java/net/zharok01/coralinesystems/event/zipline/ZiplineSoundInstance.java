package net.zharok01.coralinesystems.event.zipline;

import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ZiplineSoundInstance extends AbstractTickableSoundInstance {

    private static final float MIN_MOVING_SPEED = 0.02F;
    private static final float MIN_PITCH = 0.7F;
    private static final float MAX_PITCH = 2.0F;
    private static final float SPEED_PITCH_SCALING = 1.5F;
    private static final float FADE_IN_PER_TICK = 0.25F;
    private static final float FADE_OUT_PER_TICK = 0.2F;

    private final Player player;
    private final boolean coraline$observerSound;

    public ZiplineSoundInstance(Player player) {
        this(player, false);
    }

    public ZiplineSoundInstance(Player player, boolean observerSound) {
        super(ModSounds.ROPE_SLIDE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.coraline$observerSound = observerSound;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
    }

    @Override
    public void tick() {
        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();

        if (this.player.isRemoved() || !this.coraline$isZiplining()) {
            if (this.coraline$observerSound) {
                ZiplineHandler.coraline$clientObserverZiplines.remove(this.player.getUUID());
            }
            this.stop();
            return;
        }

        float horizontalSpeed = (float) Math.hypot(this.player.getX() - this.player.xo, this.player.getZ() - this.player.zo);

        if (horizontalSpeed >= MIN_MOVING_SPEED) {
            this.pitch = Mth.clamp(0.5F + horizontalSpeed * SPEED_PITCH_SCALING, MIN_PITCH, MAX_PITCH);
            this.volume = Math.min(1.0F, this.volume + FADE_IN_PER_TICK);
        } else {
            this.volume = Math.max(0.0F, this.volume - FADE_OUT_PER_TICK);
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return !this.isStopped() && !this.player.isRemoved() && !this.player.isSilent()
                && this.coraline$isZiplining();
    }

    private boolean coraline$isZiplining() {
        return this.coraline$observerSound
                ? ZiplineHandler.coraline$clientObserverZiplines.contains(this.player.getUUID())
                : ZiplineHandler.CLIENT_ZIPLINERS.containsKey(this.player.getUUID());
    }
}