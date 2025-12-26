package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.zharok01.coralinesystems.content.block.LimitedLightBlock;
import net.zharok01.coralinesystems.content.block.LimitedTorchBlock;
import net.zharok01.coralinesystems.content.block.LimitedWallTorchBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Blocks.class)
public class BlocksMixin {

	@Redirect(
		method = "<clinit>",
		at = @At(
			value = "NEW",
			target = "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Lnet/minecraft/core/particles/ParticleOptions;)Lnet/minecraft/world/level/block/TorchBlock;"
		)
	)
	private static TorchBlock redirectTorchBlock(BlockBehaviour.Properties properties, net.minecraft.core.particles.ParticleOptions particleOptions) {
		if (particleOptions != ParticleTypes.FLAME) {
			return new TorchBlock(properties, particleOptions);
		}
		return new LimitedTorchBlock(properties.lightLevel(state -> state.getValue(LimitedLightBlock.BURN) < 15 ? 15 : 0), particleOptions);
	}

	@Redirect(
		method = "<clinit>",
		at = @At(
			value = "NEW",
			target = "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Lnet/minecraft/core/particles/ParticleOptions;)Lnet/minecraft/world/level/block/WallTorchBlock;"
		)
	)
	private static WallTorchBlock redirectWallTorchBlock(BlockBehaviour.Properties properties, net.minecraft.core.particles.ParticleOptions particleOptions) {
		if (particleOptions != ParticleTypes.FLAME) {
			return new WallTorchBlock(properties, particleOptions);
		}
		return new LimitedWallTorchBlock(properties.lightLevel(state -> state.getValue(LimitedLightBlock.BURN) < 15 ? 15 : 0), particleOptions);
	}

}
