package net.zharok01.coralinesystems.mixin.entity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.ai.LurableFish;
import net.zharok01.coralinesystems.util.CoralineFishingTuning;
import net.zharok01.coralinesystems.registry.CoralineTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile {
    @Shadow
    @Final
    private int lureSpeed;
    @Shadow
    @Nullable
    private Entity hookedIn;
    @Unique
    private final Map<AbstractFish, Integer> coraline$interestedFish = new HashMap<>();
    @Unique
    private final Set<AbstractFish> coraline$timedOutFish = new HashSet<>();
    @Unique
    private int coraline$noticeTimer = CoralineFishingTuning.INITIAL_NOTICE_DELAY_TICKS;

    protected FishingHookMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected abstract void setHookedEntity(@Nullable Entity hookedEntity);

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    private void coraline$lureFishEntities(BlockPos pos, CallbackInfo ci) {
        ci.cancel();
        if (this.hookedIn != null) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        Iterator<Map.Entry<AbstractFish, Integer>> iterator = this.coraline$interestedFish.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<AbstractFish, Integer> entry = iterator.next();
            AbstractFish fish = entry.getKey();
            int ticksLeft = entry.getValue() - 1;
            if (((LurableFish) fish).coraline$getLureHook() != (Object) this) {
                iterator.remove();
            } else if (!fish.isAlive() || !fish.isInWater() || ticksLeft <= 0) {
                if (ticksLeft <= 0) {
                    this.coraline$timedOutFish.add(fish);
                }
                this.coraline$releaseFish(fish);
                iterator.remove();
            } else {
                entry.setValue(ticksLeft);
            }
        }
        Vec3 hookPosition = this.position();
        AbstractFish biter = null;
        double closestDistanceSqr = CoralineFishingTuning.BITE_REACH_SQR;
        for (AbstractFish fish : this.coraline$interestedFish.keySet()) {
            double distanceSqr = fish.getEyePosition().distanceToSqr(hookPosition);
            if (distanceSqr < closestDistanceSqr) {
                closestDistanceSqr = distanceSqr;
                biter = fish;
            }
        }
        if (biter != null) {
            this.coraline$hookFish(serverLevel, biter);
            return;
        }
        if (this.coraline$interestedFish.size() < CoralineFishingTuning.MAX_INTERESTED_FISH) {
            this.coraline$tryNoticeFish(serverLevel);
        }
    }

    @Unique
    private void coraline$tryNoticeFish(ServerLevel serverLevel) {
        if (--this.coraline$noticeTimer > 0) {
            return;
        }
        this.coraline$noticeTimer = CoralineFishingTuning.NOTICE_CHECK_INTERVAL_TICKS;
        float noticeChance = Math.min(CoralineFishingTuning.MAX_NOTICE_CHANCE, CoralineFishingTuning.NOTICE_CHANCE + this.lureSpeed * CoralineFishingTuning.NOTICE_CHANCE_PER_LURE_LEVEL);
        if (this.random.nextFloat() >= noticeChance) {
            return;
        }
        List<AbstractFish> candidates = serverLevel.getEntitiesOfClass(AbstractFish.class, this.getBoundingBox().inflate(CoralineFishingTuning.FISH_NOTICE_RADIUS, CoralineFishingTuning.FISH_NOTICE_HEIGHT, CoralineFishingTuning.FISH_NOTICE_RADIUS), fish -> fish.getType().is(CoralineTags.FISH) && fish.isInWater() && ((LurableFish) fish).coraline$getLureHook() == null && !this.coraline$timedOutFish.contains(fish));
        if (candidates.isEmpty()) {
            return;
        }
        candidates.sort(Comparator.comparingDouble(fish -> fish.distanceToSqr(this)));
        AbstractFish noticer = candidates.get(this.random.nextInt(Math.min(CoralineFishingTuning.NOTICE_NEAREST_POOL, candidates.size())));
        ((LurableFish) noticer).coraline$setLureHook((FishingHook) (Object) this);
        this.coraline$interestedFish.put(noticer, CoralineFishingTuning.APPROACH_TIMEOUT_TICKS);
    }

    @Unique
    private void coraline$hookFish(ServerLevel serverLevel, AbstractFish biter) {
        for (AbstractFish fish : this.coraline$interestedFish.keySet()) {
            if (fish != biter) {
                this.coraline$releaseFish(fish);
            }
        }
        this.coraline$interestedFish.clear();
        this.setHookedEntity(biter);
        this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
        double splashY = this.getY() + 0.5;
        int particleCount = (int) (1.0F + this.getBbWidth() * 20.0F);
        serverLevel.sendParticles(ParticleTypes.BUBBLE, this.getX(), splashY, this.getZ(), particleCount, this.getBbWidth(), 0.0, this.getBbWidth(), 0.2);
        serverLevel.sendParticles(ParticleTypes.FISHING, this.getX(), splashY, this.getZ(), particleCount, this.getBbWidth(), 0.0, this.getBbWidth(), 0.2);
    }

    @Unique
    private void coraline$releaseFish(AbstractFish fish) {
        LurableFish lurable = (LurableFish) fish;
        if (lurable.coraline$getLureHook() == (Object) this) {
            lurable.coraline$setLureHook(null);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$rideHookedFish(CallbackInfo ci) {
        if (!(this.hookedIn instanceof AbstractFish fish) || fish.isRemoved()) {
            return;
        }
        if (!this.level().isClientSide) {
            Entity angler = this.getOwner();
            boolean fishGotAway = !fish.isAlive() || angler != null && this.distanceToSqr(angler) > CoralineFishingTuning.HOOKED_ESCAPE_DISTANCE_SQR;
            if (fishGotAway) {
                if (fish.isAlive()) {
                    this.coraline$timedOutFish.add(fish);
                    this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 0.6F);
                }
                this.coraline$releaseFish(fish);
                this.setHookedEntity(null);
                return;
            }
        }
        this.setPos(fish.getX(), fish.getY(0.8), fish.getZ());
        this.setDeltaMovement(Vec3.ZERO);
    }

    @Inject(method = "pullEntity", at = @At("HEAD"), cancellable = true)
    private void coraline$reelFishAshore(Entity entity, CallbackInfo ci) {
        if (entity instanceof AbstractFish fish && this.getOwner() != null) {
            ((LurableFish) fish).coraline$startReel(this.getOwner());
            ci.cancel();
        }
    }

    @Inject(method = "retrieve", at = @At("RETURN"), cancellable = true)
    private void coraline$cheapFishCatch(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (this.hookedIn instanceof AbstractFish && cir.getReturnValue() > 0) {
            cir.setReturnValue(CoralineFishingTuning.FISH_CATCH_ROD_DAMAGE);
        }
    }

    @Inject(method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At("HEAD"))
    private void coraline$freeFishWhenHookVanishes(Entity.RemovalReason reason, CallbackInfo ci) {
        for (AbstractFish fish : this.coraline$interestedFish.keySet()) {
            this.coraline$releaseFish(fish);
        }
        this.coraline$interestedFish.clear();
        if (this.hookedIn instanceof AbstractFish fish) {
            this.coraline$releaseFish(fish);
        }
    }
}
