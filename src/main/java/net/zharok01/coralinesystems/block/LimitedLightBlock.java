package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.zharok01.coralinesystems.registry.CoralineSounds;

public interface LimitedLightBlock {

	IntegerProperty BURN = IntegerProperty.create("burn", 0, 15);
	default float getBurnUpChance() {
		return 1.0f / 3.0f;
	}

	default void coraline$playBurnOutEffects(ServerLevel level, BlockPos pos) {
		level.playSound(null, pos, CoralineSounds.TORCH_BURN_OFF.get(), SoundSource.BLOCKS, 0.45f, 1.0f);
		level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 5, 0.0, 0.0, 0.0, 0.0);
	}

	default void coraline$playBurnOutEffects(ServerLevel level, BlockPos pos, Direction facing) {
		level.playSound(null, pos, CoralineSounds.TORCH_BURN_OFF.get(), SoundSource.BLOCKS, 0.45f, 1.0f);
		Direction particleOffset = facing.getOpposite();
		level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5 + 0.27 * particleOffset.getStepX(), pos.getY() + 0.92, pos.getZ() + 0.5 + 0.27 * particleOffset.getStepZ(), 5, 0.0, 0.0, 0.0, 0.0);
	}

	default void coraline$playRelightEffects(ServerLevel level, BlockPos pos) {
		level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 3, 0.08, 0.08, 0.08, 0.01);
	}

	default void coraline$playRelightEffects(ServerLevel level, BlockPos pos, Direction facing) {
		Direction particleOffset = facing.getOpposite();
		level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5 + 0.27 * particleOffset.getStepX(), pos.getY() + 0.92, pos.getZ() + 0.5 + 0.27 * particleOffset.getStepZ(), 3, 0.08, 0.08, 0.08, 0.01);
	}
}
