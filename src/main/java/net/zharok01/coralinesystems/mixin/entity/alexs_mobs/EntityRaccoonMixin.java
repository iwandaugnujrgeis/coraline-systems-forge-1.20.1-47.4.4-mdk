package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.gameevent.GameEvent;
import net.zharok01.coralinesystems.util.access.RaccoonEyeFadeAccess;
import net.zharok01.coralinesystems.util.helper.EatItemsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityRaccoon.class, remap = false)
public class EntityRaccoonMixin implements RaccoonEyeFadeAccess {

    @Unique private static final float FOOD_HEAL_AMOUNT = 5.0F;
    @Unique private static final int TAME_CHANCE_DENOMINATOR = 3;
    @Unique private static final int EYE_DARKNESS_THRESHOLD = 7;
    @Unique private static final int EYE_FADE_TICKS = 40;
    @Unique private static final float EYE_FADE_STEP = 1.0F / EYE_FADE_TICKS;

    @Unique private float coraline$eyeFade;
    @Unique private float coraline$prevEyeFade;

    @Inject(method = "m_8119_", at = @At("TAIL"))
    private void coraline$tickEyeFade(CallbackInfo ci) {
        EntityRaccoon raccoon = (EntityRaccoon) (Object) this;
        if (!raccoon.level().isClientSide()) return;

        long dayTime = raccoon.level().getDayTime() % 24000L;
        boolean night = dayTime >= 13000L && dayTime <= 22000L;
        BlockPos lightPos = raccoon.getLightPosition();
        int blockLight = raccoon.level().getBrightness(LightLayer.BLOCK, lightPos);
        int brightness = night ? blockLight : Math.max(raccoon.level().getBrightness(LightLayer.SKY, lightPos), blockLight);

        float target = brightness < EYE_DARKNESS_THRESHOLD && !raccoon.isRigby() ? 1.0F : 0.0F;
        this.coraline$prevEyeFade = this.coraline$eyeFade;
        this.coraline$eyeFade += Mth.clamp(target - this.coraline$eyeFade, -EYE_FADE_STEP, EYE_FADE_STEP);
    }

    @Override
    public float coraline$getEyeFade(float partialTick) {
        float linear = Mth.lerp(partialTick, this.coraline$prevEyeFade, this.coraline$eyeFade);
        return linear * linear;
    }

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntityRaccoon;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntityRaccoon;setCommand(I)V")
    )
    private void coraline$skipWandering(EntityRaccoon instance, int command) {
        instance.setCommand(command == 0 ? 1 : command);
    }

    @Inject(method = "m_6071_", at = @At("HEAD"), cancellable = true)
    private void coraline$simpleFeeding(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        EntityRaccoon raccoon = (EntityRaccoon) (Object) this;
        ItemStack stack = player.getItemInHand(hand);
        boolean clientSide = raccoon.level().isClientSide();

        if (!raccoon.isTame() && stack.is(AMTagRegistry.RACCOON_TAMEABLES)) {
            if (!clientSide) {
                this.coraline$consumeFeedItem(player, stack, raccoon);
                if (raccoon.getRandom().nextInt(TAME_CHANCE_DENOMINATOR) == 0) {
                    raccoon.tame(player);
                    raccoon.level().broadcastEntityEvent(raccoon, (byte) 7);
                } else {
                    raccoon.level().broadcastEntityEvent(raccoon, (byte) 6);
                }
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(clientSide));
        } else if (raccoon.isFood(stack) && raccoon.getHealth() < raccoon.getMaxHealth()) {
            if (raccoon.level() instanceof ServerLevel serverLevel) {
                double particleX = raccoon.getX();
                double particleY = raccoon.getY() + raccoon.getBbHeight() * 0.75D;
                double particleZ = raccoon.getZ();

                serverLevel.sendParticles(ParticleTypes.HEART, particleX, particleY, particleZ, 3, 0.25D, 0.25D, 0.25D, 0.0D);
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), particleX, particleY, particleZ, 8, 0.15D, 0.15D, 0.15D, 0.1D);

                EatItemsHelper.applyFoodEffects(raccoon, stack);
                this.coraline$consumeFeedItem(player, stack, raccoon);
                raccoon.heal(FOOD_HEAL_AMOUNT);
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(clientSide));
        } else if (raccoon.isTame()
                && EntityRaccoon.isRaccoonFood(stack)
                && !raccoon.isFood(stack)
                && !stack.is(AMTagRegistry.RACCOON_TEAMING_FOODS)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Unique
    private void coraline$consumeFeedItem(Player player, ItemStack stack, EntityRaccoon raccoon) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        raccoon.gameEvent(GameEvent.EAT);
        raccoon.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
    }

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntityRaccoon;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;m_5661_(Lnet/minecraft/network/chat/Component;Z)V")
    )
    private void coraline$hideModeMessage(Player instance, Component message, boolean overlay) {}

    @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true)
    private void coraline$disableItemPickup(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}