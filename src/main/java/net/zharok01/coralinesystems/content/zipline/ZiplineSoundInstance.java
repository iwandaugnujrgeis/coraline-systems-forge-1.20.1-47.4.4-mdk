package net.zharok01.coralinesystems.content.zipline;

import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ZiplineSoundInstance extends AbstractTickableSoundInstance {

    private final Player player;
    private int slideTicks;

    public ZiplineSoundInstance(Player player) {
        super(ModSounds.ROPE_SLIDE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.slideTicks = 0;
    }

    @Override
    public void tick() {
        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();

        // 1. Check if we should stop
        if (this.player.isRemoved() || !ZiplineHandler.ZIPLINING_PLAYERS.containsKey(this.player.getUUID())) {
            this.forceStop(); // Call our helper
            return;
        }

        // 2. Calculate horizontal momentum for the "whoosh"
        float horizontalSpeed = (float) this.player.getDeltaMovement().horizontalDistance();
        float minPitch = 0.7f;
        float maxPitch = 2.0f;
        float speedScaling = 1.5f;

        float newPitch = Mth.clamp(0.5f + horizontalSpeed * speedScaling, 0, maxPitch);

        if (newPitch >= minPitch) {
            this.slideTicks++;
            this.pitch = newPitch;
            this.volume = Mth.clamp(this.slideTicks * 0.07f, 0.0f, 1.0f);
        } else {
            // If they are moving too slow, stop the sound
            this.forceStop();
        }
    }

    private void forceStop() {
        this.stop(); // This calls the FINAL method in AbstractTickableSoundInstance
        this.pitch = 0.0F;
        this.volume = 0.0F;
        this.slideTicks = 0;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        // We use the base class's isStopped() check alongside our custom state
        return !this.isStopped() && !this.player.isRemoved() && !this.player.isSilent() && ZiplineHandler.ZIPLINING_PLAYERS.containsKey(this.player.getUUID());
    }
}