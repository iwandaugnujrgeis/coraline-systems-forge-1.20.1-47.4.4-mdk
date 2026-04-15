package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineWorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Player.class)
public abstract class PlayerMineshaftMixin extends LivingEntity {

    protected PlayerMineshaftMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Cached Structure references so registry lookups don't happen on every
     * triggered tick. Null until first use, then kept for the session.
     * These are instance fields (not static) because each player instance
     * lives on a specific ServerLevel — static fields can cause issues if
     * multiple worlds with different registries are loaded in the same session.
     */
    @Unique private Structure coraline$cachedMineshaft     = null;
    @Unique private Structure coraline$cachedMineshaftMesa = null;

    /**
     * The server game-time (in ticks) at which the creak sound last played
     * for this player. Initialised to -24000 so the very first detection can
     * fire immediately rather than waiting a full cooldown period first.
     *
     * This is intentionally NOT persisted — it resets every session, so a
     * player who logs back in near a familiar mineshaft can hear one reminder
     * creak before the chunk's permanent play count takes over.
     */
    //TODO: Change it to 24000, after testing!
    @Unique private long coraline$lastCreakTime = -24000L;

    //TODO: Change it to 24000, after testing!
    /** One in-game day in ticks — the per-player cooldown between creaks. */
    @Unique private static final long CREAK_COOLDOWN_TICKS = 24000L;

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$mineshaftCreak(CallbackInfo ci) {
        // ── Cheap early exits ─────────────────────────────────────────────────
        if (this.level().isClientSide()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        //TODO: Change it to 400, after testing!
        //1-in-400 ticks ≈ once every 20 seconds on average.
        if (this.random.nextInt(400) != 0) return;

        // ── Per-player cooldown ───────────────────────────────────────────────
        // Skip if a creak already played within the last in-game day for this
        // player. This prevents the sound from firing more than once per ~20
        // real-world minutes even when the player stays in one spot.
        long currentTime = serverLevel.getGameTime();
        if (currentTime - coraline$lastCreakTime < CREAK_COOLDOWN_TICKS) return;

        // ── Persistent blacklist ──────────────────────────────────────────────
        // If this chunk has already had its creak quota (3 plays), skip the
        // structure lookup entirely — no point doing it forever.
        ChunkPos playerChunk = new ChunkPos(this.blockPosition());
        CoralineWorldData worldData = CoralineWorldData.get(serverLevel);
        if (worldData.isBlacklisted(playerChunk)) return;

        // ── Lazy structure cache ──────────────────────────────────────────────
        if (coraline$cachedMineshaft == null) {
            Registry<Structure> registry = serverLevel.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE);
            coraline$cachedMineshaft     = registry.get(BuiltinStructures.MINESHAFT);
            coraline$cachedMineshaftMesa = registry.get(BuiltinStructures.MINESHAFT_MESA);
        }

        // ── Structure check ───────────────────────────────────────────────────
        List<StructureStart> starts = serverLevel.structureManager().startsForStructure(
                playerChunk,
                struct -> struct == coraline$cachedMineshaft
                        || struct == coraline$cachedMineshaftMesa
        );

        if (starts.isEmpty()) return;

        // ── Play sound & update state ─────────────────────────────────────────
        serverLevel.playSound(null, this.blockPosition().below(3), CoralineSounds.MINESHAFT_SPOOK.get(), SoundSource.AMBIENT, 1.0F, 1.0F);

        // Stamp the cooldown so this player won't hear it again for one in-game day.
        coraline$lastCreakTime = currentTime;

        // Record the play in the persistent data. Once this chunk reaches 3
        // plays across any number of sessions, it is blacklisted permanently.
        worldData.recordPlay(playerChunk);
    }
}