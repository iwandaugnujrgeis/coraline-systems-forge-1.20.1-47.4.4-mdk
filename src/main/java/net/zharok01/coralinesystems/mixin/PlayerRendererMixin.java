package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.zharok01.coralinesystems.client.ClientAnimationTypes;
import net.zharok01.coralinesystems.util.AnimationTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowSize) {
        super(context, model, shadowSize);
    }

    @Inject(method = "getArmPose",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"),
            locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private static void coralinesystems$renderSwordMain(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> info,
                                                        ItemStack itemstack, UseAnim useanim){
        if (useanim == AnimationTypes.SWORD_BLOCK && useanim == itemstack.getUseAnimation()) {
            info.setReturnValue(ClientAnimationTypes.SWORD_BLOCK_POSE);
        }
    }
}