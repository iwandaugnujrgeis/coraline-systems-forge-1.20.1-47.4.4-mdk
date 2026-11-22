package net.zharok01.coralinesystems.event.zipline;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.network.ClientboundZiplineSoundPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTags;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class ZiplineHandler {

    public static class ZiplineData {
        public Vec3 momentum;
        public Vec3 targetMomentum;
        public Vec3 direction;
        public BlockPos anchor;
        public Item tool;
        public boolean isNew = true;

        public ZiplineData(Vec3 momentum, Vec3 targetMomentum, Vec3 direction, BlockPos anchor, Item tool) {
            this.momentum = momentum;
            this.targetMomentum = targetMomentum;
            this.direction = direction;
            this.anchor = anchor;
            this.tool = tool;
        }
    }

    public static final Map<UUID, ZiplineData> SERVER_ZIPLINERS = new HashMap<>();
    public static final Map<UUID, ZiplineData> CLIENT_ZIPLINERS = new HashMap<>();
    public static final Set<UUID> coraline$clientObserverZiplines = new HashSet<>();

    private static final double MIN_MOMENTUM = 0.01;
    private static final double LAUNCH_MULTIPLIER = 2.0;
    private static final double MAX_SPEED = 0.35;
    private static final int ROPE_SEARCH_HEIGHT = 5;
    private static final double HANG_HEIGHT_OFFSET = 0.15;
    private static final double HOVER_TOLERANCE = 0.2;
    private static final double CENTERING_STRENGTH = 0.3;
    private static final double MAX_CENTERING_SPEED = 0.15;
    private static final int REATTACH_COOLDOWN_TICKS = 20;

    public static Map<UUID, ZiplineData> coraline$ziplinersFor(Player player) {
        return player.level().isClientSide() ? CLIENT_ZIPLINERS : SERVER_ZIPLINERS;
    }

    public static BlockPos coraline$findRopeAbove(Player player) {
        BlockPos playerPos = player.blockPosition();
        for (int y = 0; y <= ROPE_SEARCH_HEIGHT; y++) {
            BlockPos checkPos = playerPos.above(y);
            if (player.level().getBlockState(checkPos).getBlock() instanceof RopeBlock) {
                return checkPos;
            }
        }
        return null;
    }

    private static Vec3 coraline$alignToRope(Level level, BlockPos anchor, Vec3 heading) {
        BlockState ropeState = level.getBlockState(anchor);
        RopeBlock rope = (RopeBlock) ropeState.getBlock();

        boolean ropeAlongX = rope.hasConnection(Direction.EAST, ropeState) || rope.hasConnection(Direction.WEST, ropeState);
        boolean ropeAlongZ = rope.hasConnection(Direction.NORTH, ropeState) || rope.hasConnection(Direction.SOUTH, ropeState);

        if (!ropeAlongX && !ropeAlongZ) return heading;

        boolean useX = ropeAlongX && (!ropeAlongZ || Math.abs(heading.x) >= Math.abs(heading.z));
        return useX ? new Vec3(Math.copySign(1.0, heading.x), 0.0, 0.0) : new Vec3(0.0, 0.0, Math.copySign(1.0, heading.z));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        boolean clientSide = player.level().isClientSide();
        ZiplineData data = coraline$ziplinersFor(player).get(player.getUUID());
        if (data == null) return;

        if (!player.isAlive() || !player.getMainHandItem().is(CoralineTags.ZIPLINEABLE)) {
            stopZiplining(player);
            return;
        }

        if (!clientSide) {
            if (data.isNew) {
                data.isNew = false;
                if (player instanceof ServerPlayer serverPlayer) {
                    CoralineTriggers.ZIPLINE.trigger(serverPlayer);
                }
            }

            if (!ZiplineStateCapability.isPressed(player)) {
                stopZiplining(player);
                return;
            }
        }

        BlockPos ropePos = coraline$findRopeAbove(player);
        if (ropePos == null) {
            stopZiplining(player);
            return;
        }

        if (!clientSide) {
            player.fallDistance = 0.0F;
            return;
        }

        data.anchor = ropePos;
        player.setSprinting(false);

        data.direction = coraline$alignToRope(player.level(), ropePos, data.direction);
        data.momentum = data.direction.scale(data.momentum.horizontalDistance());
        data.targetMomentum = data.direction.scale(data.targetMomentum.horizontalDistance());

        Vec3 momentum = data.momentum.lerp(data.targetMomentum, 0.15);

        if (momentum.horizontalDistance() > MAX_SPEED) {
            momentum = momentum.scale(MAX_SPEED / momentum.horizontalDistance());
        }

        if (momentum.horizontalDistance() < MIN_MOMENTUM) {
            momentum = Vec3.ZERO;
        }

        double centeringX = 0.0;
        double centeringZ = 0.0;
        if (data.direction.x == 0.0) {
            centeringX = Mth.clamp((ropePos.getX() + 0.5 - player.getX()) * CENTERING_STRENGTH, -MAX_CENTERING_SPEED, MAX_CENTERING_SPEED);
        } else if (data.direction.z == 0.0) {
            centeringZ = Mth.clamp((ropePos.getZ() + 0.5 - player.getZ()) * CENTERING_STRENGTH, -MAX_CENTERING_SPEED, MAX_CENTERING_SPEED);
        }

        double playerEyeY = player.getY() + player.getEyeHeight();
        double hangY = data.anchor.getY() + HANG_HEIGHT_OFFSET;
        double yDelta = 0.0;

        if (playerEyeY < hangY - HOVER_TOLERANCE) {
            yDelta = Math.min(0.4, (hangY - HOVER_TOLERANCE - playerEyeY) * 0.3);
        } else if (playerEyeY > hangY + HOVER_TOLERANCE) {
            yDelta = -0.1;
        }

        player.setDeltaMovement(momentum.x + centeringX, yDelta, momentum.z + centeringZ);
        player.fallDistance = 0.0F;
        player.setNoGravity(true);

        data.momentum = momentum;
    }

    public static void startZiplining(Player player, Vec3 initialMomentum, BlockPos anchorPos) {
        Vec3 flatInitial = new Vec3(initialMomentum.x, 0.0, initialMomentum.z);

        if (flatInitial.horizontalDistance() < 0.1) {
            flatInitial = Vec3.directionFromRotation(0, player.getYRot()).scale(0.1);
        }

        Vec3 target = flatInitial.scale(LAUNCH_MULTIPLIER);

        if (target.horizontalDistance() > MAX_SPEED) {
            target = target.scale(MAX_SPEED / target.horizontalDistance());
        }

        coraline$ziplinersFor(player).put(player.getUUID(),
                new ZiplineData(flatInitial, target, flatInitial.normalize(), anchorPos, player.getMainHandItem().getItem()));

        if (player instanceof ServerPlayer serverPlayer) {
            CoralinePacketHandler.sendToTracking(serverPlayer, new ClientboundZiplineSoundPacket(player.getUUID(), true));
        }
    }

    public static void stopZiplining(Player player) {
        ZiplineData data = coraline$ziplinersFor(player).remove(player.getUUID());
        if (data == null) return;

        if (player instanceof ServerPlayer serverPlayer) {
            CoralinePacketHandler.sendToTracking(serverPlayer, new ClientboundZiplineSoundPacket(player.getUUID(), false));
        }

        if (player.level().isClientSide()) {
            player.setNoGravity(false);
        }

        if (ZiplineStateCapability.isPressed(player)) {
            ZiplineStateCapability.coraline$requireRelease(player);
        } else {
            player.getCooldowns().addCooldown(data.tool, REATTACH_COOLDOWN_TICKS);
        }

        player.level().playSound(player, player.getX(), player.getY(), player.getZ(), CoralineSounds.ZIPLINE_DETACH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof ServerPlayer target)) return;
        if (!(event.getEntity() instanceof ServerPlayer trackingPlayer)) return;
        if (!SERVER_ZIPLINERS.containsKey(target.getUUID())) return;

        CoralinePacketHandler.sendToClient(
                new ClientboundZiplineSoundPacket(target.getUUID(), true),
                trackingPlayer
        );
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        SERVER_ZIPLINERS.remove(player.getUUID());
        ZiplineStateCapability.clear(player);
    }
}