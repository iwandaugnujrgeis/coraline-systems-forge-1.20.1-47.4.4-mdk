package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

public class LimitedTorchBlock extends TorchBlock implements LimitedLightBlock {

	public LimitedTorchBlock(Properties properties, ParticleOptions flameParticle) {
		super(properties, flameParticle);
		this.registerDefaultState(this.stateDefinition.any().setValue(BURN, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BURN);
	}

	@Override
	public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
		if (state.getValue(BURN) < 15) super.animateTick(state, level, pos, random);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return state.getValue(BURN) < 15;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void randomTick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
		int age = state.getValue(BURN);
		if (age >= 15 || this.getBurnUpChance() <= random.nextFloat()) return;

		int nextAge = age + 1;
		level.setBlockAndUpdate(pos, state.setValue(BURN, nextAge));
		if (nextAge == 15) this.coraline$playBurnOutEffects(level, pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public @NotNull InteractionResult use(BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (state.getValue(BURN) == 15 && stack.is(Items.FLINT_AND_STEEL)) {
			level.setBlockAndUpdate(pos, state.setValue(BURN, 0));
			if (level instanceof ServerLevel serverLevel) this.coraline$playRelightEffects(serverLevel, pos);
			level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, level.random.nextFloat() * 0.4f + 0.8f);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

			stack.hurtAndBreak(1, player, pPlayer -> pPlayer.broadcastBreakEvent(hand));
			return InteractionResult.sidedSuccess(level.isClientSide());
		}
		return super.use(state, level, pos, player, hand, hit);
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		if (!state.hasProperty(BURN)) return 15;
		return state.getValue(BURN) < 15 ? 15 : 0;
	}

}
