package net.zharok01.coralinesystems.util.helper;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.registry.CoralineSounds;

public final class BagBreakHelper {
    private static final int ITEM_PARTICLE_COUNT = 30;
    private static final double PARTICLE_SPREAD = 0.3D;
    private static final double PARTICLE_SPEED = 0.05D;

    private static final int DROP_PICKUP_DELAY_TICKS = 40;
    private static final int DROP_MIN_CHUNK_SIZE = 10;
    private static final int DROP_CHUNK_SIZE_VARIATION = 21;
    private static final float DROP_MAX_HORIZONTAL_SPEED = 0.1F;
    private static final double DROP_UPWARD_SPEED = 0.3D;

    private BagBreakHelper() {
    }

    public static List<ItemEntity> destroy(ServerPlayer player, ItemStack bag, List<ItemStack> contents) {
        ServerLevel level = player.serverLevel();
        RandomSource random = level.getRandom();
        double x = player.getX();
        double y = player.getY(0.5D);
        double z = player.getZ();
        List<ItemEntity> drops = new ArrayList<>();

        for (ItemStack stack : contents) {
            while (!stack.isEmpty()) {
                ItemStack chunk = stack.split(random.nextInt(DROP_CHUNK_SIZE_VARIATION) + DROP_MIN_CHUNK_SIZE);
                ItemEntity drop = new ItemEntity(level, x, y, z, chunk);
                float speed = random.nextFloat() * DROP_MAX_HORIZONTAL_SPEED;
                float angle = random.nextFloat() * Mth.TWO_PI;
                drop.setDeltaMovement(-Mth.sin(angle) * speed, DROP_UPWARD_SPEED, Mth.cos(angle) * speed);
                drop.setPickUpDelay(DROP_PICKUP_DELAY_TICKS);
                level.addFreshEntity(drop);
                drops.add(drop);
            }
        }

        level.playSound(null, player.blockPosition(), CoralineSounds.BAG_BREAK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, bag.copy()), x, y, z, ITEM_PARTICLE_COUNT, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPEED);

        bag.shrink(1);
        return drops;
    }
}