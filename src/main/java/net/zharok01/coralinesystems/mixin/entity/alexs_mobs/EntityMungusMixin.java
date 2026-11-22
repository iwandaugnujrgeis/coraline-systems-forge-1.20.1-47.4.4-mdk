package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntityMungus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.zharok01.coralinesystems.CoralineSystems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityMungus.class, remap = false)
public abstract class EntityMungusMixin {

    @Shadow
    public int timeUntilNextEgg;

    @Unique
    private static final ResourceLocation WHITE_MUSHROOM = new ResourceLocation(CoralineSystems.MOD_ID, "white_mushroom");

    @Unique
    private static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_WHITE_MUSHROOM = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            new ResourceLocation(CoralineSystems.MOD_ID, "huge_white_mushroom"));

    @Inject(method = "m_8119_()V", at = @At("HEAD"))
    private void coraline$suppressMungalSpores(CallbackInfo ci) {
        this.timeUntilNextEgg = Integer.MAX_VALUE;
    }

    @Redirect(
            method = "m_8107_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/BonemealableBlock;m_214148_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    private void coraline$growHugeMushroom(
            BonemealableBlock bonemealable, ServerLevel level, RandomSource random, BlockPos pos, BlockState state
    ) {
        ResourceKey<ConfiguredFeature<?, ?>> featureKey;
        if (state.is(Blocks.BROWN_MUSHROOM)) {
            featureKey = TreeFeatures.HUGE_BROWN_MUSHROOM;
        } else if (state.is(Blocks.RED_MUSHROOM)) {
            featureKey = TreeFeatures.HUGE_RED_MUSHROOM;
        } else if (net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock()).equals(WHITE_MUSHROOM)) {
            featureKey = HUGE_WHITE_MUSHROOM;
        } else {
            bonemealable.performBonemeal(level, random, pos, state);
            return;
        }

        Holder<ConfiguredFeature<?, ?>> feature = level.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolderOrThrow(featureKey);

        level.removeBlock(pos, false);
        if (!feature.value().place(level, level.getChunkSource().getGenerator(), random, pos)) {
            level.setBlock(pos, state, 3);
        }
    }
}