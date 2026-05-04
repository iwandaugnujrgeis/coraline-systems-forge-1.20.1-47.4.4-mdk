package net.zharok01.coralinesystems.content.zipline;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "coraline_systems")
public class ZiplineHandler {

    /** Tracks momentum and the current rope anchor block */
    public static class ZiplineData {
        public Vec3 momentum;
        public Vec3 targetMomentum;
        public BlockPos anchor;

        public ZiplineData(Vec3 momentum, Vec3 targetMomentum, BlockPos anchor) {
            this.momentum = momentum;
            this.targetMomentum = targetMomentum;
            this.anchor = anchor;
        }
    }

    public static final Map<UUID, ZiplineData> ZIPLINING_PLAYERS = new HashMap<>();

    private static final double MIN_MOMENTUM = 0.01;
    // REMOVED: private static final double FRICTION = 0.97;
    private static final double LAUNCH_MULTIPLIER = 2.0;

    // CHANGED: Added speed cap — set just below a Minecart on powered rails (0.4 blocks/tick),
    // so rail travel remains a meaningfully faster option. Also closes the spam-click
    // exploit: even if the player re-grabs the rope while carrying full speed,
    // startZiplining() clamps the launch target here before it can be multiplied further.
    private static final double MAX_SPEED = 0.35;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        if (event.phase != TickEvent.Phase.END) return;
        if (!ZIPLINING_PLAYERS.containsKey(player.getUUID())) return;

        ZiplineData data = ZIPLINING_PLAYERS.get(player.getUUID());

        // 1. Client & Server Input Checks
        if (!player.level().isClientSide()) {
            if (!ZiplineStateCapability.isPressed(player) || !player.getMainHandItem().is(ItemTags.PICKAXES)) {
                stopZiplining(player);
                return;
            }
        } else {
            // Kill WASD inputs and sprinting on the Client!
            player.xxa = 0.0F;
            player.zza = 0.0F;
            player.yya = 0.0F;
            player.setSprinting(false);

            if (!player.getMainHandItem().is(ItemTags.PICKAXES)) {
                stopZiplining(player);
                return;
            }
        }

        // 2. Dynamic Rope Scanning (Allows sliding under horizontal ropes)
        boolean onRope = false;
        BlockPos playerPos = player.blockPosition();

        // Scan up to 5 blocks above the player to find the rope
        for (int y = 0; y <= 5; y++) {
            BlockPos checkPos = playerPos.above(y);
            if (player.level().getBlockState(checkPos).getBlock() instanceof RopeBlock) {
                onRope = true;
                data.anchor = checkPos; // Update anchor to the new rope block
                break;
            }
        }

        if (!onRope) {
            stopZiplining(player);
            return;
        }

        // 3. Momentum Acceleration (no friction — player slides at constant speed once up to target)
        Vec3 momentum = data.momentum;

        // Smoothly accelerate towards the target momentum.
        // Tweak the 0.15 value (0.0 to 1.0) to control how quickly they reach max speed!
        momentum = momentum.lerp(data.targetMomentum, 0.15);

        // REMOVED: momentum = momentum.multiply(FRICTION, 1.0, FRICTION);

        // CHANGED: Enforce the hard speed cap every tick so no edge case can push past it.
        if (momentum.horizontalDistance() > MAX_SPEED) {
            momentum = momentum.scale(MAX_SPEED / momentum.horizontalDistance());
        }

        if (momentum.horizontalDistance() < MIN_MOMENTUM) {
            momentum = Vec3.ZERO; // Hang in place
        }

        // 4. The Pull-Up Mechanic
        double playerEyeY = player.getY() + player.getEyeHeight();
        double ropeBottomY = data.anchor.getY();
        double yDelta = 0.0;

        // Smoothly pull the player towards the rope if they are too low
        if (playerEyeY < ropeBottomY - 0.2) {
            yDelta = Math.min(0.4, (ropeBottomY - 0.2 - playerEyeY) * 0.3);
        } else if (playerEyeY > ropeBottomY + 0.2) {
            yDelta = -0.1; // Gently pull down if they glitch above it
        }

        // 5. Apply Movement
        player.setDeltaMovement(momentum.x, yDelta, momentum.z);
        player.fallDistance = 0.0F;
        player.setNoGravity(true);

        data.momentum = momentum;
        ZIPLINING_PLAYERS.put(player.getUUID(), data);
    }

    public static void startZiplining(Player player, Vec3 initialMomentum, BlockPos anchorPos) {
        Vec3 flatInitial = new Vec3(initialMomentum.x, 0.0, initialMomentum.z);

        // If the player is barely moving, give them a baseline speed in the direction
        // they are looking so they don't get stuck instantly.
        if (flatInitial.horizontalDistance() < 0.1) {
            flatInitial = Vec3.directionFromRotation(0, player.getYRot()).scale(0.1);
        }

        Vec3 target = flatInitial.scale(LAUNCH_MULTIPLIER);

        // CHANGED: Clamp the launch target to MAX_SPEED.
        // This is the key fix for the spam-click exploit: even if the player releases
        // and immediately re-grabs the rope while carrying full speed, the LAUNCH_MULTIPLIER
        // cannot compound the velocity beyond this ceiling.
        if (target.horizontalDistance() > MAX_SPEED) {
            target = target.scale(MAX_SPEED / target.horizontalDistance());
        }

        ZIPLINING_PLAYERS.put(player.getUUID(), new ZiplineData(flatInitial, target, anchorPos));
    }

    public static void stopZiplining(Player player) {
        ZIPLINING_PLAYERS.remove(player.getUUID());
        player.setNoGravity(false);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        stopZiplining(player);
        ZiplineStateCapability.clear(player);
    }
}