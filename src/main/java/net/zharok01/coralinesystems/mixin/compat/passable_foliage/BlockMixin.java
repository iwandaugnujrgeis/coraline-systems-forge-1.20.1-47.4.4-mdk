package net.zharok01.coralinesystems.mixin.compat.passable_foliage;

import net.minecraft.world.level.block.Block;
import net.zharok01.coralinesystems.block.PassableFoliageBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class BlockMixin implements PassableFoliageBlock {

    @Unique
    private boolean coraline$passable;

    @Override
    public void coraline$setPassable(boolean passable) {
        coraline$passable = passable;
    }

    @Override
    public boolean coraline$isPassable() {
        return coraline$passable;
    }
}
