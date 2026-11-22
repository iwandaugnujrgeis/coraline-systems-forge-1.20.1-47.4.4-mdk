package net.zharok01.coralinesystems.event.time;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.block.CentrifugeBlock;
import net.zharok01.coralinesystems.util.data.CentrifugeAccelerationData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TimeAccelerationManager {

    private static final double DIMINISHING_RETURNS_FACTOR = 0.7;
    private static final double HASTE_RADIUS = 50.0;
    private static final int MAX_HASTE_AMPLIFIER = 9;
    private static final int HASTE_EFFECT_DURATION = 200;

    private final Map<GlobalPos, TimeAccelerationSession> activeSessions = new LinkedHashMap<>();
    private final CentrifugeAccelerationData savedData;

    public TimeAccelerationManager(ServerLevel level) {
        this.savedData = CentrifugeAccelerationData.get(level);
        tryRestoreSessions();
    }

    private void tryRestoreSessions() {
        for (CentrifugeAccelerationData.SavedSession saved : savedData.getSessions()) {
            if (saved.ticksRemaining() <= 0) {
                continue;
            }
            activeSessions.put(saved.source(), new TimeAccelerationSession(saved.ticksRemaining()));
        }
    }

    public boolean startSession(ServerLevel level, BlockPos sourcePos) {
        GlobalPos key = GlobalPos.of(level.dimension(), sourcePos.immutable());
        if (activeSessions.containsKey(key)) {
            return false;
        }
        activeSessions.put(key, new TimeAccelerationSession());
        setActivatedState(level, sourcePos, true);
        savedData.setSessions(snapshotSessions());
        return true;
    }

    public boolean isActive(ResourceKey<Level> dimension, BlockPos sourcePos) {
        return activeSessions.containsKey(GlobalPos.of(dimension, sourcePos.immutable()));
    }

    public boolean isAnyActive() {
        return !activeSessions.isEmpty();
    }

    public Set<GlobalPos> getActivePositions() {
        return java.util.Collections.unmodifiableSet(activeSessions.keySet());
    }

    public void forceStop(ServerLevel level, BlockPos source) {
        GlobalPos key = GlobalPos.of(level.dimension(), source.immutable());
        if (!activeSessions.containsKey(key)) {
            return;
        }
        playStopEffects(level, source);
        endSession(level, key);
        savedData.setSessions(snapshotSessions());
    }

    public void tick(ServerLevel level) {
        long extraTicks = computeExtraTicks();
        if (extraTicks > 0) {
            level.setDayTime(level.getDayTime() + extraTicks);
        }

        broadcastTime(level);


        List<GlobalPos> finished = new ArrayList<>();
        for (Map.Entry<GlobalPos, TimeAccelerationSession> entry : activeSessions.entrySet()) {
            if (!entry.getValue().tick()) {
                finished.add(entry.getKey());
            }
        }

        for (GlobalPos key : finished) {
            ServerLevel sessionLevel = level.getServer().getLevel(key.dimension());
            playStopEffects(sessionLevel, key.pos());
            endSession(sessionLevel, key);
        }

        if (!finished.isEmpty()) {
            savedData.setSessions(snapshotSessions());
        } else {
            savedData.setSessionsQuiet(snapshotSessions());
        }
    }



    private long computeExtraTicks() {
        List<Double> speeds = new ArrayList<>(activeSessions.size());
        for (TimeAccelerationSession session : activeSessions.values()) {
            speeds.add(session.getCurrentSpeed());
        }
        speeds.sort(Comparator.reverseOrder());

        double total = 0.0;
        double weight = 1.0;
        for (double speed : speeds) {
            total += (speed - 1.0) * weight;
            weight *= DIMINISHING_RETURNS_FACTOR;
        }
        return (long) total;
    }

    private void endSession(ServerLevel sessionLevel, GlobalPos key) {
        activeSessions.remove(key);
        setActivatedState(sessionLevel, key.pos(), false);
    }

    private void playStopEffects(ServerLevel sessionLevel, BlockPos source) {
        double cx = source.getX() + 0.5;
        double cy = source.getY() + 1.0;
        double cz = source.getZ() + 0.5;

        sessionLevel.playSound(
                null,
                source,
                SoundEvents.BEACON_DEACTIVATE,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );

        sessionLevel.sendParticles(ParticleTypes.FLASH, cx, cy, cz, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private void setActivatedState(ServerLevel sessionLevel, BlockPos pos, boolean activated) {
        BlockState current = sessionLevel.getBlockState(pos);
        if (current.hasProperty(CentrifugeBlock.ACTIVATED)) {
            sessionLevel.setBlock(pos, current.setValue(CentrifugeBlock.ACTIVATED, activated), 3);
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

    private List<CentrifugeAccelerationData.SavedSession> snapshotSessions() {
        List<CentrifugeAccelerationData.SavedSession> snapshot = new ArrayList<>(activeSessions.size());
        for (Map.Entry<GlobalPos, TimeAccelerationSession> entry : activeSessions.entrySet()) {
            snapshot.add(new CentrifugeAccelerationData.SavedSession(
                    entry.getKey(),
                    entry.getValue().getTicksRemaining()
            ));
        }
        return snapshot;
    }
}