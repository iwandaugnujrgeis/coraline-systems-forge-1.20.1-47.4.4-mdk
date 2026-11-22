package net.zharok01.coralinesystems.client;

import einstein.subtle_effects.init.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.zharok01.coralinesystems.block.BrewState;
import net.zharok01.coralinesystems.block.BrewingCauldronBlockEntity;
import net.zharok01.coralinesystems.block.BrewingCauldronColors;
import net.zharok01.coralinesystems.block.CultureType;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineSounds;

@OnlyIn(Dist.CLIENT)
public final class BrewingCauldronClientHooks {

    private static final String SUBTLE_EFFECTS_MOD_ID = "subtle_effects";

    private BrewingCauldronClientHooks() {
    }

    private static ParticleOptions coraline$sporeParticle() {
        return ModList.get().isLoaded(SUBTLE_EFFECTS_MOD_ID) ? ModParticles.MUSHROOM_SPORE.get() : ParticleTypes.MYCELIUM;
    }

    public static void coraline$animate(BrewingCauldronBlockEntity be, BlockState state, Level level, BlockPos pos, RandomSource random, double surfaceY) {
        CultureType culture = be.getCulture();
        BrewState brewState = be.getBrewState();
        int rgb = BrewingCauldronColors.getContentColor(state, 1);

        if (culture != CultureType.NONE && brewState == BrewState.BREWING) {
            boolean darkKombucha = culture == CultureType.KOMBUCHA && level.getRawBrightness(pos, 0) <= 0;
            if (!darkKombucha) {
                if (rgb != BrewingCauldronColors.NO_TINT && random.nextInt(3) == 0) {
                    float r = ((rgb >> 16) & 0xFF) / 255.0f;
                    float g = ((rgb >> 8) & 0xFF) / 255.0f;
                    float b = (rgb & 0xFF) / 255.0f;

                    int bubbleCount = 1 + (be.getSolidStrength() >= 4 && random.nextInt(5) < 2 ? 1 : 0);
                    for (int i = 0; i < bubbleCount; i++) {
                        double bx = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                        double bz = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                        level.addParticle(CoralineParticles.CAULDRON_BUBBLE.get(), bx, surfaceY - 0.05, bz, r, g, b);
                    }
                }

                if (random.nextInt(20) == 0) {
                    level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            CoralineSounds.CAULDRON_BUBBLING.get(), SoundSource.BLOCKS,
                            0.3F, 0.6F + random.nextFloat() * 0.2F, false);
                }
            }
        }

        if (brewState == BrewState.SPOILED) {
            if (random.nextInt(8) == 0) {
                double bx = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                double bz = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                level.addParticle(coraline$sporeParticle(), bx, surfaceY, bz, 0.0, 0.01, 0.0);
            }
            if (random.nextInt(40) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        CoralineSounds.CAULDRON_DREGS_AMBIENT.get(), SoundSource.BLOCKS,
                        0.35F, 0.7F + random.nextFloat() * 0.2F, false);
            }
        }

        if (culture == CultureType.KOMBUCHA && brewState == BrewState.FINISHED) {
            if (rgb != BrewingCauldronColors.NO_TINT && random.nextInt(8) == 0) {
                float fr = ((rgb >> 16) & 0xFF) / 255.0f;
                float fg = ((rgb >> 8) & 0xFF) / 255.0f;
                float fb = (rgb & 0xFF) / 255.0f;

                int fizzCount = 1 + random.nextInt(3);
                for (int i = 0; i < fizzCount; i++) {
                    double bx = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                    double bz = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                    level.addParticle(CoralineParticles.CAULDRON_FIZZ.get(), bx, surfaceY - 0.02, bz, fr, fg, fb);
                }
            }
            if (random.nextInt(50) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        CoralineSounds.CAULDRON_KOMBUCHA_AMBIENT.get(), SoundSource.BLOCKS,
                        0.3F, 1.2F + random.nextFloat() * 0.3F, false);
            }
        }
    }
}