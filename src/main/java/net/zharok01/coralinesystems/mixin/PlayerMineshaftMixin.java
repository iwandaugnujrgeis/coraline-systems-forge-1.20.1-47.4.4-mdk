package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
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
    @Unique
    private Structure coraline$cachedMineshaft = null;
    @Unique
    private Structure coraline$cachedMineshaftMesa = null;

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$mineshaftCreak(CallbackInfo ci) {
        // Early exits first — cheapest checks at the top
        if (this.level().isClientSide()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // 1 in 400 ticks ≈ once every 20 seconds on average.
        // NOTE: the condition MUST be == 0, not != 0.
        // != 0 would run 399/400 ticks (i.e. almost every tick) and cause severe lag.
        if (this.random.nextInt(400) != 0) return;

        if (coraline$cachedMineshaft == null) {
            Registry<Structure> registry = serverLevel.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE);
            coraline$cachedMineshaft = registry.get(BuiltinStructures.MINESHAFT);
            coraline$cachedMineshaftMesa = registry.get(BuiltinStructures.MINESHAFT_MESA);
        }

        List<StructureStart> starts = serverLevel.structureManager().startsForStructure(
                new ChunkPos(this.blockPosition()),
                struct -> struct == coraline$cachedMineshaft || struct == coraline$cachedMineshaftMesa
        );

        if (!starts.isEmpty()) {
            serverLevel.playSound(null, this.blockPosition(),
                    SoundEvents.MINECART_RIDING, SoundSource.AMBIENT, 0.05F, 0.5F);
        }
    }
}