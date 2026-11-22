package net.zharok01.coralinesystems.block;

import einstein.subtle_effects.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineParticles;

public class SuspiciousBlock extends FallingBlock {
    private static final int MIN_CHIME_INTERVAL_TICKS = 200;
    private static final int MAX_CHIME_INTERVAL_TICKS = 600;
    private static final int SHARDS_PER_SAMPLE = 2;

    private static long coraline$nextChimeTick;

    public SuspiciousBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < SHARDS_PER_SAMPLE; i++) {
            Direction face = Direction.getRandom(random);
            BlockPos neighborPos = pos.relative(face);
            if (level.getBlockState(neighborPos).isSolidRender(level, neighborPos)) continue;

            double x = face.getStepX() == 0 ? pos.getX() + random.nextDouble() : pos.getX() + 0.5 + face.getStepX() * 0.5625;
            double y = face.getStepY() == 0 ? pos.getY() + random.nextDouble() : pos.getY() + 0.5 + face.getStepY() * 0.5625;
            double z = face.getStepZ() == 0 ? pos.getZ() + random.nextDouble() : pos.getZ() + 0.5 + face.getStepZ() * 0.5625;
            level.addParticle(CoralineParticles.SHARD_BURST.get(), x, y, z, 0.0, 0.0, 0.0);
        }

        long gameTime = level.getGameTime();
        if (gameTime < coraline$nextChimeTick) return;

        SoundEvent sound;
        float volume;
        float pitch;
        if (random.nextInt(6) == 0) {
            sound = ModSounds.AMETHYST_CLUSTER_CHIME.get();
            volume = Mth.nextFloat(random, 0.15F, 0.3F);
            pitch = 1.0F;
        } else {
            sound = SoundEvents.AMETHYST_BLOCK_CHIME;
            volume = Mth.nextFloat(random, 0.07F, 1.5F);
            pitch = Mth.nextFloat(random, 0.07F, 1.3F);
        }

        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound, SoundSource.BLOCKS, volume, pitch, false);
        coraline$nextChimeTick = gameTime + MIN_CHIME_INTERVAL_TICKS
                + random.nextInt(MAX_CHIME_INTERVAL_TICKS - MIN_CHIME_INTERVAL_TICKS + 1);
    }
}