package net.zharok01.coralinesystems.block;

import com.legacy.rediscovered.api.INetherReactorGrowable;
import com.legacy.rediscovered.client.RediscoveredSounds;
import com.legacy.rediscovered.client.particles.NetherReactorPulseData;
import com.legacy.rediscovered.registry.RediscoveredBlocks;
import com.legacy.rediscovered.registry.RediscoveredParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.event.ReactorEvent;
import net.zharok01.coralinesystems.item.OrbItem;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import net.zharok01.coralinesystems.registry.CoralineTags;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import net.zharok01.coralinesystems.event.ReactorSessionManager;
import org.jetbrains.annotations.NotNull;

public class ReactorBlockEntity extends BlockEntity {

    private static final int GROWTH_RADIUS_H = 16;
    private static final int GROWTH_RADIUS_V = 8;
    private static final int DEFAULT_GROWTH_SPEED = 2;
    private static final int OBSIDIAN_SPREAD_RADIUS = 12;
    private static final int EFFECT_RADIUS = 45;
    private static final int EFFECT_DURATION_TICKS = 100;
    private static final int EFFECT_TICK_INTERVAL = 40;
    private static final String KEY_GROWTH_SPEED = "growth_speed";

    private int growthSpeed = DEFAULT_GROWTH_SPEED;
    private int audioTick = 0;

    public ReactorBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.REACTOR.get(), pos, state);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains(KEY_GROWTH_SPEED)) {
            growthSpeed = tag.getInt(KEY_GROWTH_SPEED);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(KEY_GROWTH_SPEED, growthSpeed);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public boolean tryActivate(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide()) return false;

        ItemStack heldStack = player.getItemInHand(hand);
        if (!(heldStack.getItem() instanceof OrbItem)) return false;

        ReactorSessionManager manager = ReactorEvent.getManager();
        if (manager == null || manager.isActive(worldPosition)) return false;

        boolean started = manager.startSession((ServerLevel) level, worldPosition);
        if (!started) return false;

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        coraline$onSessionStart((ServerLevel) level, worldPosition);

        if (player instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.REACTOR_ORB_INSERT.trigger(serverPlayer);
        }

        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ReactorBlockEntity reactor) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (!state.getValue(ReactorBlock.ACTIVE)) {
            reactor.audioTick = 0;
            return;
        }

        RandomSource rand = serverLevel.getRandom();

        BlockPos spreadPos = pos.offset(
                rand.nextIntBetweenInclusive(-OBSIDIAN_SPREAD_RADIUS, OBSIDIAN_SPREAD_RADIUS),
                rand.nextIntBetweenInclusive(-OBSIDIAN_SPREAD_RADIUS, OBSIDIAN_SPREAD_RADIUS),
                rand.nextIntBetweenInclusive(-OBSIDIAN_SPREAD_RADIUS, OBSIDIAN_SPREAD_RADIUS));
        BlockState spreadTarget = serverLevel.getBlockState(spreadPos);

        if (!spreadTarget.is(RediscoveredBlocks.glowing_obsidian)
                && spreadTarget.is(CoralineTags.REACTOR_CONVERTIBLE)) {
            serverLevel.removeBlock(spreadPos, false);
            serverLevel.setBlock(spreadPos, RediscoveredBlocks.glowing_obsidian.defaultBlockState(), 3);
            Vec3 p = Vec3.atCenterOf(spreadPos);
            serverLevel.sendParticles(RediscoveredParticles.NETHER_REACTOR_GROWTH,
                    p.x, p.y, p.z, 20, 0.4, 0.4, 0.4, 0.001);
        }

        for (int i = 0; i < reactor.growthSpeed; i++) {
            BlockPos growthPos = pos.offset(
                    rand.nextIntBetweenInclusive(-GROWTH_RADIUS_H, GROWTH_RADIUS_H),
                    rand.nextIntBetweenInclusive(-GROWTH_RADIUS_V, GROWTH_RADIUS_V),
                    rand.nextIntBetweenInclusive(-GROWTH_RADIUS_H, GROWTH_RADIUS_H));
            BlockState growthTarget = serverLevel.getBlockState(growthPos);
            INetherReactorGrowable nrg = INetherReactorGrowable.get(growthTarget);
            nrg.onNetherReactorGrow(growthTarget, serverLevel, growthPos, rand, null)
                    .ifPresent(px -> nrg.displayNetherReactorGrowth(growthTarget, serverLevel, px, rand, null));
        }

        if (level.getGameTime() % EFFECT_TICK_INTERVAL == 0L) {
            ReactorSessionManager manager = ReactorEvent.getManager();
            int amplifier = manager != null ? manager.activeCount() - 1 : 0;

            AABB effectBox = new AABB(pos).inflate(EFFECT_RADIUS);
            for (Player player : level.getEntitiesOfClass(Player.class, effectBox)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        EFFECT_DURATION_TICKS,
                        amplifier,
                        true,
                        true));
            }
        }

        if (reactor.audioTick % 102 == 0) {
            level.playSound(null, pos, RediscoveredSounds.BLOCK_NETHER_REACTOR_IDLE,
                    SoundSource.BLOCKS, 6.0f, 1.0f);
            reactor.audioTick = 0;
        }

        if ((reactor.audioTick - 5) % 51 == 0) {
            Vec3 center = Vec3.atCenterOf(pos);
            for (ServerPlayer player : serverLevel.players()) {
                serverLevel.sendParticles(player,
                        new NetherReactorPulseData(2, 20, 5.0f), true,
                        center.x, center.y, center.z,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        reactor.audioTick++;
        reactor.setChanged();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ReactorBlockEntity reactor) {
        if (!state.getValue(ReactorBlock.ACTIVE)) return;

        RandomSource rand = level.getRandom();
        Vec3 center = Vec3.atCenterOf(pos);
        int range = 10;

        for (int i = rand.nextInt(3); i > 0; i--) {
            level.addParticle(ParticleTypes.PORTAL,
                    center.x + rand.nextInt(range) - rand.nextInt(range),
                    center.y + rand.nextInt(range) - rand.nextInt(range),
                    center.z + rand.nextInt(range) - rand.nextInt(range),
                    0.0, 0.0, 0.0);
        }

        double d = 1.4;
        double dy = 0.5;
        for (int i = 5; i > 0; i--) {
            level.addParticle(RediscoveredParticles.NETHER_REACTOR_GROWTH,
                    center.x + (rand.nextFloat() - 0.5f) * d,
                    center.y + (rand.nextFloat() - 0.5f) * dy,
                    center.z + (rand.nextFloat() - 0.5f) * d,
                    1e-4, 1e-4, 1e-4);
        }
    }

    static void coraline$onSessionStart(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, RediscoveredSounds.BLOCK_NETHER_REACTOR_ACTIVATE,
                SoundSource.BLOCKS, 3.5f, 1.0f);

        Vec3 center = Vec3.atCenterOf(pos);
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player,
                    new NetherReactorPulseData(0, 50, 35.0f), true,
                    center.x, center.y, center.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        level.sendParticles(ParticleTypes.FLASH,
                center.x, center.y + 1.0, center.z,
                1, 0.0, 0.0, 0.0, 0.0);
    }
}