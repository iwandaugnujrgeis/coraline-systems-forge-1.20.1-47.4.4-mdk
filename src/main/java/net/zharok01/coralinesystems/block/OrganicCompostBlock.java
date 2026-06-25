package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.zharok01.coralinesystems.registry.CoralineBlocks;

import javax.annotation.Nullable;

/**
 * Organic Compost block.
 *
 * <p>Replaces both {@code OrganicCompostBlock} and {@code RichSoilBlock} from Farmer's Delight.
 * On a random tick it has a 25% chance to boost any {@link BonemealableBlock} sitting directly
 * above it. It can be tilled with a hoe to produce {@link CompostFarmlandBlock}.</p>
 *
 * <p>Removed from the original FD design:
 * <ul>
 *   <li>Multi-stage composting (no COMPOSTING block state property).</li>
 *   <li>Mushroom → Mushroom Colony conversion.</li>
 *   <li>Tag-gated boost exclusions (UNAFFECTED_BY_RICH_SOIL, PLANTED_FROM_BELOW).</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("deprecation")
public class OrganicCompostBlock extends Block {

    /** Probability that the random tick will attempt to boost the crop above. */
    private static final float BOOST_CHANCE = 0.25f;

    public OrganicCompostBlock(Properties properties) {
        super(properties);
    }

    // -------------------------------------------------------------------------
    // Random-tick crop boosting
    // -------------------------------------------------------------------------

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < BOOST_CHANCE) {
            tryBoostPlantAbove(level, pos);
        }
    }

    /**
     * Attempts to apply one bonemeal growth tick to the block directly above {@code pos}.
     * Mirrors {@code RichSoilBlock.boostPlant()} but without any tag blacklist.
     */
    private static void tryBoostPlantAbove(ServerLevel level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        if (aboveState.getBlock() instanceof BonemealableBlock growable) {
            if (growable.isValidBonemealTarget(level, abovePos, aboveState, false)
                    && ForgeHooks.onCropsGrowPre(level, abovePos, aboveState, true)) {
                growable.performBonemeal(level, level.random, abovePos, aboveState);
                level.levelEvent(LevelEvent.PARTICLES_PLANT_GROWTH, abovePos, 0);
                ForgeHooks.onCropsGrowPost(level, abovePos, aboveState);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Hoe interaction → CompostFarmlandBlock
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockState getToolModifiedState(BlockState state, UseOnContext context,
                                           ToolAction toolAction, boolean simulate) {
        if (toolAction.equals(ToolActions.HOE_TILL)
                && context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
            return CoralineBlocks.COMPOST_FARMLAND.get().defaultBlockState();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Plant support — identical to RichSoilBlock: supports everything except
    // CROP, NETHER and WATER plant types (those need proper farmland).
    // -------------------------------------------------------------------------

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos,
                                   Direction facing, net.minecraftforge.common.IPlantable plantable) {
        PlantType plantType = plantable.getPlantType(level, pos.relative(facing));
        return plantType != PlantType.CROP
                && plantType != PlantType.NETHER
                && plantType != PlantType.WATER;
    }
}