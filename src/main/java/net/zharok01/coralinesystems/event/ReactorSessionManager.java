package net.zharok01.coralinesystems.event;

import com.legacy.rediscovered.client.RediscoveredSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.block.ReactorBlock;
import net.zharok01.coralinesystems.util.data.ReactorSavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReactorSessionManager {

    public static final int SESSION_DURATION_TICKS = 3_000;

    private final Map<BlockPos, int[]> activeSessions = new LinkedHashMap<>();
    private final ReactorSavedData savedData;

    public ReactorSessionManager(ServerLevel level) {
        this.savedData = ReactorSavedData.get(level);
        coraline$restoreSessions(level);
    }

    private void coraline$restoreSessions(ServerLevel level) {
        for (ReactorSavedData.SavedSession saved : savedData.getSessions()) {
            if (saved.ticksRemaining() <= 0) continue;
            activeSessions.put(saved.source(), new int[]{ saved.ticksRemaining() });
            coraline$setActiveState(level, saved.source(), true);
        }
    }

    public boolean startSession(ServerLevel level, BlockPos sourcePos) {
        BlockPos key = sourcePos.immutable();
        if (activeSessions.containsKey(key)) return false;
        activeSessions.put(key, new int[]{ SESSION_DURATION_TICKS });
        coraline$setActiveState(level, key, true);
        savedData.setSessions(coraline$snapshot());
        return true;
    }

    public boolean isActive(BlockPos sourcePos) {
        return activeSessions.containsKey(sourcePos.immutable());
    }

    public int activeCount() {
        return activeSessions.size();
    }

    public void forceStop(ServerLevel level, BlockPos source) {
        BlockPos key = source.immutable();
        if (!activeSessions.containsKey(key)) return;
        coraline$playStopEffects(level, key);
        coraline$endSession(level, key);
        savedData.setSessions(coraline$snapshot());
    }

    public void tick(ServerLevel level) {
        List<BlockPos> finished = new ArrayList<>();

        for (Map.Entry<BlockPos, int[]> entry : activeSessions.entrySet()) {
            entry.getValue()[0]--;
            if (entry.getValue()[0] <= 0) {
                finished.add(entry.getKey());
            }
        }

        for (BlockPos pos : finished) {
            coraline$playStopEffects(level, pos);
            coraline$endSession(level, pos);
        }

        if (!finished.isEmpty()) {
            savedData.setSessions(coraline$snapshot());
        } else {
            savedData.setSessionsQuiet(coraline$snapshot());
        }
    }

    private void coraline$endSession(ServerLevel level, BlockPos source) {
        activeSessions.remove(source);
        coraline$setActiveState(level, source, false);
    }

    private void coraline$playStopEffects(ServerLevel level, BlockPos source) {
        level.playSound(null, source,
                RediscoveredSounds.BLOCK_NETHER_REACTOR_DEACTIVATE,
                SoundSource.BLOCKS, 3.5f, 1.0f);
        level.sendParticles(ParticleTypes.FLASH,
                source.getX() + 0.5, source.getY() + 1.0, source.getZ() + 0.5,
                1, 0.0, 0.0, 0.0, 0.0);
    }

    private void coraline$setActiveState(ServerLevel level, BlockPos pos, boolean active) {
        BlockState current = level.getBlockState(pos);
        if (current.hasProperty(ReactorBlock.ACTIVE)) {
            level.setBlock(pos, current.setValue(ReactorBlock.ACTIVE, active), 3);
        }
    }

    private List<ReactorSavedData.SavedSession> coraline$snapshot() {
        List<ReactorSavedData.SavedSession> snapshot = new ArrayList<>(activeSessions.size());
        for (Map.Entry<BlockPos, int[]> entry : activeSessions.entrySet()) {
            snapshot.add(new ReactorSavedData.SavedSession(entry.getKey(), entry.getValue()[0]));
        }
        return snapshot;
    }
}