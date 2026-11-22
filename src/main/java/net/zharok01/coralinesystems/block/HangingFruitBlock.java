package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class HangingFruitBlock extends Block implements BonemealableBlock {

    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;

    private final VoxelShape shapeAge0;
    private final VoxelShape shapeAge1;
    private final VoxelShape shapeAge2;

    private final TagKey<Block> validLeafTag;
    private final RegistryObject<Item> fruitItem;

    private final int ripeDropMin;
    private final int ripeDropMax;

    public HangingFruitBlock(BlockBehaviour.Properties properties,
                             TagKey<Block> validLeafTag,
                             RegistryObject<Item> fruitItem) {
        this(properties, validLeafTag, fruitItem, 1, 1,
                Block.box(5, 8, 5, 11, 16, 11),
                Block.box(4, 6, 4, 12, 16, 12),
                Block.box(3, 4, 3, 13, 16, 13));
    }

    public HangingFruitBlock(BlockBehaviour.Properties properties,
                             TagKey<Block> validLeafTag,
                             RegistryObject<Item> fruitItem,
                             int ripeDropMin, int ripeDropMax) {
        this(properties, validLeafTag, fruitItem, ripeDropMin, ripeDropMax,
                Block.box(5, 8, 5, 11, 16, 11),
                Block.box(4, 6, 4, 12, 16, 12),
                Block.box(3, 4, 3, 13, 16, 13));
    }

    public HangingFruitBlock(BlockBehaviour.Properties properties,
                             TagKey<Block> validLeafTag,
                             RegistryObject<Item> fruitItem,
                             int ripeDropMin, int ripeDropMax,
                             VoxelShape shapeAge0,
                             VoxelShape shapeAge1,
                             VoxelShape shapeAge2) {
        super(properties);
        this.validLeafTag = validLeafTag;
        this.fruitItem = fruitItem;
        this.ripeDropMin = ripeDropMin;
        this.ripeDropMax = ripeDropMax;
        this.shapeAge0 = shapeAge0;
        this.shapeAge1 = shapeAge1;
        this.shapeAge2 = shapeAge2;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    public TagKey<Block> getValidLeafTag() {
        return validLeafTag;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(AGE)) {
            case 0 -> shapeAge0;
            case 1 -> shapeAge1;
            default -> shapeAge2;
        };
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(validLeafTag);
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                           @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (direction == Direction.UP && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE && ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(5) == 0)) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    @Override
    public boolean isValidBonemealTarget(@NotNull LevelReader level, @NotNull BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(@NotNull Level level, @NotNull RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(@NotNull ServerLevel level, @NotNull RandomSource random, @NotNull BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (state.getValue(AGE) < MAX_AGE) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            int count = ripeDropMin == ripeDropMax
                    ? ripeDropMin
                    : ripeDropMin + level.random.nextInt(ripeDropMax - ripeDropMin + 1);
            popResource(level, pos, new ItemStack(fruitItem.get(), count));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(AGE, 0), Block.UPDATE_CLIENTS);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull PathComputationType type) {
        return false;
    }
}
