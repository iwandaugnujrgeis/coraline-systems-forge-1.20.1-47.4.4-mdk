package net.zharok01.coralinesystems.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineUtils;
import org.jetbrains.annotations.NotNull;

public class OnyxSwordItem extends SwordItem {

    private static final double TELEPORT_RANGE = 16.0;
    private static final int TELEPORT_COOLDOWN_TICKS = 80;
    private static final int DURABILITY_COST_MIN = 1;
    private static final int DURABILITY_COST_MAX = 3;

    public OnyxSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack sword = player.getItemInHand(hand);

        if (level.isClientSide) {
            player.startUsingItem(hand);
            return InteractionResultHolder.success(sword);
        }

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 rayEnd = eyePos.add(lookVec.scale(TELEPORT_RANGE));

        BlockHitResult hit = level.clip(new ClipContext(
                eyePos, rayEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        Vec3 target;
        if (hit.getType() == HitResult.Type.BLOCK) {
            target = hit.getLocation().subtract(lookVec.normalize().scale(0.5));
        } else {
            target = rayEnd;
        }

        boolean teleported = coraline$attemptVoidTeleport(level, player, sword, target);

        if (teleported) {
            return InteractionResultHolder.success(sword);
        }

        player.playSound(CoralineSounds.MONSTER_TELEPORT.get(), 0.5F, 1.8F);
        return InteractionResultHolder.fail(sword);
    }

    private boolean coraline$attemptVoidTeleport(Level level, Player player, ItemStack sword, Vec3 target) {
        if (!(level instanceof ServerLevel serverLevel)) return false;

        double fromX = player.getX();
        double fromY = player.getY();
        double fromZ = player.getZ();

        boolean success = CoralineUtils.randomTeleportStatic(
                player, target.x, target.y, target.z, false
        );

        if (!success) return false;

        float pitch = 0.7F + serverLevel.getRandom().nextFloat() * 0.6F;
        serverLevel.playSound(null, fromX, fromY, fromZ,
                CoralineSounds.MONSTER_TELEPORT.get(),
                player.getSoundSource(), 1.3F, pitch);

        float pitchDest = 0.7F + serverLevel.getRandom().nextFloat() * 0.6F;
        player.playSound(CoralineSounds.MONSTER_TELEPORT.get(), 1.3F, pitchDest);

        serverLevel.sendParticles(
                CoralineParticles.DARK_SMOKE.get(),
                fromX, fromY + 1.0, fromZ,
                10, 0.2, 0.5, 0.2, 0.02
        );

        serverLevel.sendParticles(
                CoralineParticles.DARK_SMOKE.get(),
                player.getX(), player.getY() + 1.0, player.getZ(),
                10, 0.2, 0.5, 0.2, 0.02
        );

        int durabilityCost = DURABILITY_COST_MIN
                + serverLevel.getRandom().nextInt(DURABILITY_COST_MAX - DURABILITY_COST_MIN + 1);
        sword.hurtAndBreak(durabilityCost, player,
                p -> p.broadcastBreakEvent(player.getUsedItemHand()));

        player.getCooldowns().addCooldown(this, TELEPORT_COOLDOWN_TICKS);

        return true;
    }
}