package net.zharok01.coralinesystems.content.block;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface LimitedLightBlock {

	IntegerProperty BURN = IntegerProperty.create("burn", 0, 15);

	default float getBurnUpChance() {
		return 0.5;
	}

}
