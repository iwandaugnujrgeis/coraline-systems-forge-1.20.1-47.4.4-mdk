package net.zharok01.coralinesystems.weather;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FogWeatherCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("weather")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("fog")
                        .executes(context -> setFog(context.getSource(), -1))
                        .then(Commands.argument("duration", TimeArgument.time(1))
                                .executes(context -> setFog(context.getSource(), IntegerArgumentType.getInteger(context, "duration"))))));
    }

    private static int setFog(CommandSourceStack source, int time) {
        ServerLevel overworld = source.getServer().overworld();
        int duration = time == -1 ? ServerLevel.RAIN_DURATION.sample(overworld.getRandom()) : time;
        FogWeatherManager.startFog(overworld, duration);
        source.sendSuccess(() -> Component.translatable("commands.coraline_systems.weather.set.fog"), true);
        return duration;
    }
}