package net.zharok01.coralinesystems.block;

import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RadiantSaplingBlock extends SaplingBlock {

    public RadiantSaplingBlock(BlockBehaviour.Properties properties) {
        super(new RadiantMegaTreeGrower(), properties);
    }
}