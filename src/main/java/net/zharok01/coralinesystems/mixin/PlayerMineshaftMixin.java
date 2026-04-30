package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.zharok01.coralinesystems.registry.CoralineTriggers;
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

    @Unique private static final String NBT_KEY = "coraline_last_creak_time";

    // ── Timing constants ──────────────────────────────────────────────────────
    // PRODUCTION values:
    //   CHECK_INTERVAL  = 2400  (~2 minutes average between checks at 20 TPS)
    //   CREAK_COOLDOWN  = 24000 (one in-game day between sounds per player)
    //
    // TESTING values (swap these in while testing, then revert):
    //   CHECK_INTERVAL  = 40    (~2 seconds — fires very frequently)
    //   CREAK_COOLDOWN  = 200   (10 seconds between sounds)
    @Unique private static final int  CHECK_INTERVAL       = 200;
    @Unique private static final long CREAK_COOLDOWN_TICKS = 6000;

    /**
     * Server game-time of the last creak play for this player.
     * Long.MIN_VALUE = "not yet initialised this session" — handled by the
     * initialisation guard on the first tick after joining.
     * Persisted to NBT so it survives re-logins and death.
     */
    @Unique private long coraline$lastCreakTime = Long.MIN_VALUE;

    @Unique private Structure coraline$cachedMineshaft     = null;
    @Unique private Structure coraline$cachedMineshaftMesa = null;

    // ─────────────────────────────────────────────────────────────────────────
    // NBT persistence
    // ─────────────────────────────────────────────────────────────────────────

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void coraline$loadCreakTime(CompoundTag tag, CallbackInfo ci) {
        CompoundTag persistent = tag.getCompound(Player.PERSISTED_NBT_TAG);
        if (persistent.contains(NBT_KEY)) {
            coraline$lastCreakTime = persistent.getLong(NBT_KEY);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void coraline$saveCreakTime(CompoundTag tag, CallbackInfo ci) {
        if (coraline$lastCreakTime == Long.MIN_VALUE) return;
        CompoundTag persistent = tag.getCompound(Player.PERSISTED_NBT_TAG);
        persistent.putLong(NBT_KEY, coraline$lastCreakTime);
        tag.put(Player.PERSISTED_NBT_TAG, persistent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main tick logic
    // ─────────────────────────────────────────────────────────────────────────

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$mineshaftCreak(CallbackInfo ci) {
        if (this.level().isClientSide()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        long currentTime = serverLevel.getGameTime();

        // First tick guard — starts a clean cooldown, prevents sound on world entry
        if (coraline$lastCreakTime == Long.MIN_VALUE) {
            coraline$lastCreakTime = currentTime;
            return;
        }

        if (this.random.nextInt(CHECK_INTERVAL) != 0) return;

        // Per-player cooldown (negative diff = different world session, treat as expired)
        long elapsed = currentTime - coraline$lastCreakTime;
        if (elapsed >= 0 && elapsed < CREAK_COOLDOWN_TICKS) return;

        // Persistent chunk blacklist
        ChunkPos playerChunk = new ChunkPos(this.blockPosition());
        CoralineWorldData worldData = CoralineWorldData.get(serverLevel);
        if (worldData.isBlacklisted(playerChunk)) return;

        // Lazy structure cache
        if (coraline$cachedMineshaft == null) {
            Registry<Structure> registry = serverLevel.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE);
            coraline$cachedMineshaft     = registry.get(BuiltinStructures.MINESHAFT);
            coraline$cachedMineshaftMesa = registry.get(BuiltinStructures.MINESHAFT_MESA);
        }

        // Structure check
        List<StructureStart> starts = serverLevel.structureManager().startsForStructure(
                playerChunk,
                struct -> struct == coraline$cachedMineshaft
                        || struct == coraline$cachedMineshaftMesa
        );

        if (starts.isEmpty()) return;

        // ── Play sound ────────────────────────────────────────────────────────
        float pitch = 0.8F + this.random.nextFloat() * 0.4F;
        serverLevel.playSound(
                null,
                this.blockPosition().below(3),
                CoralineSounds.MINESHAFT_SPOOK.get(),
                SoundSource.AMBIENT,
                1.0F,
                pitch
        );

        coraline$lastCreakTime = currentTime;
        worldData.recordPlay(playerChunk);

        // ── Advancement trigger ───────────────────────────────────────────────
        // AMAdvancementTrigger.trigger() requires a ServerPlayer — cast is safe
        // here because we already confirmed !isClientSide() above, and Player
        // on a server is always a ServerPlayer.
        if ((Object) this instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.MINESHAFT_DISCOVERED.trigger(serverPlayer);
        }
    }
}