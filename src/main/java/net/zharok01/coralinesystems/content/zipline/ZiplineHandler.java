package net.zharok01.coralinesystems.content.zipline;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
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

    /** Maps player UUID → current horizontal momentum while ziplining. */
    public static final Map<UUID, Vec3> ZIPLINING_PLAYERS = new HashMap<>();

    /** Minimum speed before the player is considered stopped on the rope. */
    private static final double MIN_MOMENTUM = 0.04;

    /** Horizontal friction applied every tick — lower = longer glide. */
    private static final double FRICTION = 0.97;

    /** Multiplier applied to the player's pre-jump velocity when grabbing the rope. */
    private static final double LAUNCH_MULTIPLIER = 1.5;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        // FIX: guard for server side — TickEvent.PlayerTickEvent fires on both
        // logical sides; state-map logic must only run on the server.
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide()) return;
        if (!ZIPLINING_PLAYERS.containsKey(player.getUUID())) return;

        // Stop if the player releases right-click or puts away the pickaxe
        if (!ZiplineStateCapability.isPressed(player) || !player.getMainHandItem().is(ItemTags.PICKAXES)) {
            stopZiplining(player);
            return;
        }

        // FIX: scan both above() and above(2) — a 1.8-block-tall player's head is
        // two blocks up, so the rope may be at either position depending on the setup.
        BlockState stateAbove1 = player.level().getBlockState(player.blockPosition().above());
        BlockState stateAbove2 = player.level().getBlockState(player.blockPosition().above(2));
        boolean onRope = stateAbove1.getBlock() instanceof RopeBlock
                || stateAbove2.getBlock() instanceof RopeBlock;

        if (!onRope) {
            stopZiplining(player);
            return;
        }

        Vec3 momentum = ZIPLINING_PLAYERS.get(player.getUUID());

        // Apply horizontal friction each tick
        momentum = momentum.multiply(FRICTION, 1.0, FRICTION);

        // FIX: knot detection was dead code before — the previous guard already
        // confirmed the block is a RopeBlock, so checking instanceof RopeKnotBlock
        // on the same variable could never be true. Instead we stop when momentum
        // runs out, which naturally handles reaching a knot or the rope's end
        // (the rope check above will fail on the next tick once there's no rope).
        if (momentum.horizontalDistance() < MIN_MOMENTUM) {
            // Player has come to rest on the rope — zero out and hang in place
            momentum = Vec3.ZERO;
        }

        // Apply movement: horizontal only, no vertical (gravity suppressed below)
        player.setDeltaMovement(momentum.x, 0.0, momentum.z);
        player.fallDistance = 0.0F; // suppress fall damage accumulation
        player.setNoGravity(true);

        ZIPLINING_PLAYERS.put(player.getUUID(), momentum);
    }

    /** Called when the player grabs the rope. Converts pre-jump momentum into zipline momentum. */
    public static void startZiplining(Player player, Vec3 initialMomentum) {
        Vec3 launch = new Vec3(
                initialMomentum.x * LAUNCH_MULTIPLIER,
                0.0,
                initialMomentum.z * LAUNCH_MULTIPLIER
        );
        ZIPLINING_PLAYERS.put(player.getUUID(), launch);
    }

    public static void stopZiplining(Player player) {
        ZIPLINING_PLAYERS.remove(player.getUUID());
        player.setNoGravity(false);
        // Leave fallDistance as-is so that a drop from height still causes fall damage —
        // if the player releases while in motion they should feel the consequence.
    }

    /** Clean up state when a player disconnects so the map doesn't leak entries. */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        ZIPLINING_PLAYERS.remove(player.getUUID());
        ZiplineStateCapability.clear(player);
    }
}