package net.zharok01.coralinesystems.time;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.block.CentrifugeBlock;
import org.jetbrains.annotations.Nullable;

public class TimeAccelerationManager {

    @Nullable
    private TimeAccelerationSession activeSession = null;

    /** Position of the Centrifuge that started the current session. */
    @Nullable
    private BlockPos sessionSource = null;

    /**
     * Starts a new acceleration session originating from {@code sourcePos}.
     * Does nothing if a session is already running.
     *
     * @param level     the overworld ServerLevel
     * @param sourcePos the BlockPos of the Centrifuge that triggered activation
     * @return true if a new session was started
     */
    public boolean startSession(ServerLevel level, BlockPos sourcePos) {
        if (activeSession != null) {
            return false;
        }

        activeSession = new TimeAccelerationSession();
        // Store an immutable copy — BlockPos is mutable in some contexts.
        sessionSource = sourcePos.immutable();

        // Flip the blockstate to activated.
        setActivatedState(level, sessionSource, true);

        return true;
    }

    /** @return true if a session is currently running */
    public boolean isActive() {
        return activeSession != null;
    }

    /**
     * Called once per server tick for the overworld.
     */
    public void tick(ServerLevel level) {
        if (activeSession == null) {
            return;
        }

        double speed = activeSession.getCurrentSpeed();

        long extraTicks = (long) (speed - 1.0);
        if (extraTicks > 0) {
            level.setDayTime(level.getDayTime() + extraTicks);
        }

        broadcastTime(level);

        boolean stillRunning = activeSession.tick();
        if (!stillRunning) {
            onSessionEnd(level);
        }
    }

    /**
     * Cleans up after a session expires: resets blockstate, plays stop sound,
     * and bursts particles at the Centrifuge's position.
     */
    private void onSessionEnd(ServerLevel level) {
        if (sessionSource != null) {
            // Reset blockstate to idle.
            setActivatedState(level, sessionSource, false);

            double cx = sessionSource.getX() + 0.5;
            double cy = sessionSource.getY() + 1.0;
            double cz = sessionSource.getZ() + 0.5;

            // Stop sound — swap for your custom SoundEvent later.
            level.playSound(
                    null,
                    sessionSource,
                    SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.BLOCKS,
                    1.0f,
                    1.0f
            );

            // Burst of PORTAL particles on stop — a sharp "shutting down" visual.
            level.sendParticles(
                    ParticleTypes.PORTAL,
                    cx, cy, cz,
                    60,          // particle count
                    0.4, 0.4, 0.4,  // spread
                    0.1          // speed
            );

            // A few REVERSE_PORTAL for contrast.
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    cx, cy, cz,
                    20,
                    0.3, 0.3, 0.3,
                    0.05
            );
        }

        activeSession = null;
        sessionSource = null;
    }

    /**
     * Sets the {@link CentrifugeBlock#ACTIVATED} blockstate property at
     * {@code pos}, guarding against the block having been broken mid-session.
     */
    private void setActivatedState(ServerLevel level, BlockPos pos, boolean activated) {
        BlockState current = level.getBlockState(pos);
        if (current.hasProperty(CentrifugeBlock.ACTIVATED)) {
            level.setBlock(pos, current.setValue(CentrifugeBlock.ACTIVATED, activated), 3);
        }
    }

    private void broadcastTime(ServerLevel level) {
        ClientboundSetTimePacket packet = new ClientboundSetTimePacket(
                level.getGameTime(),
                level.getDayTime(),
                level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)
        );
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }
}