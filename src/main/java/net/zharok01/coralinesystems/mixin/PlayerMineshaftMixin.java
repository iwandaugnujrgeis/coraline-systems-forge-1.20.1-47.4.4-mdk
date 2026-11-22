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

    @Unique private long coraline$lastCreakTime = Long.MIN_VALUE;
    @Unique private Structure coraline$cachedMineshaft = null;
    @Unique private Structure coraline$cachedMineshaftMesa = null;

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

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$mineshaftCreak(CallbackInfo ci) {
        if (this.level().isClientSide()) return;
        if (!this.onGround()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        long currentTime = serverLevel.getGameTime();

        if (coraline$lastCreakTime == Long.MIN_VALUE) {
            coraline$lastCreakTime = currentTime;
            return;
        }

        if (this.random.nextInt(300) != 0) return;

        long elapsed = currentTime - coraline$lastCreakTime;
        if (elapsed >= 0 && elapsed < 22000) return;

        ChunkPos playerChunk = new ChunkPos(this.blockPosition());
        CoralineWorldData worldData = CoralineWorldData.get(serverLevel);
        if (worldData.isBlacklisted(playerChunk)) return;

        if (coraline$cachedMineshaft == null) {
            Registry<Structure> registry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
            coraline$cachedMineshaft = registry.get(BuiltinStructures.MINESHAFT);
            coraline$cachedMineshaftMesa = registry.get(BuiltinStructures.MINESHAFT_MESA);
        }

        List<StructureStart> starts = serverLevel.structureManager().startsForStructure(
                playerChunk,
                struct -> struct == coraline$cachedMineshaft || struct == coraline$cachedMineshaftMesa
        );

        if (starts.isEmpty()) return;

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

        if ((Object) this instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.MINESHAFT_DISCOVERED.trigger(serverPlayer);
        }
    }
}