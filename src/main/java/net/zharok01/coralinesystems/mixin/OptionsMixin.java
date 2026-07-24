package net.zharok01.coralinesystems.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Mixin(Options.class)
public class OptionsMixin {

	// Shadow fields we replace after <init> runs.
	@Mutable @Final @Shadow
	private OptionInstance<Double> entityDistanceScaling;

	@Mutable @Final @Shadow
	private OptionInstance<GraphicsStatus> graphicsMode;

	@Mutable @Final @Shadow
	private OptionInstance<CloudStatus> cloudStatus;

	// =========================================================================
	// 1. REMOVE FABULOUS GRAPHICS (unchanged)
	// =========================================================================
	@Redirect(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/GraphicsStatus;values()[Lnet/minecraft/client/GraphicsStatus;"
			)
	)
	private GraphicsStatus[] removeFabulousGraphics() {
		GraphicsStatus[] array = GraphicsStatus.values();
		return Arrays.copyOfRange(array, 0, array.length - 1);
	}

	// =========================================================================
	// 2. CAP RENDER DISTANCE + FOV SLIDER (unchanged)
	// =========================================================================
	@Redirect(
			method = "<init>",
			at = @At(
					value = "NEW",
					target = "net/minecraft/client/OptionInstance$IntRange"
			)
	)
	private OptionInstance.IntRange coraline$limitIntRangeSliders(int min, int max) {
		// Cap Render Distance (Vanilla: 2 to 16 or 32)
		if (min == 2 && (max == 16 || max == 32)) {
			return new OptionInstance.IntRange(2, 10);
		}
		// Cap FOV (Vanilla: 30 to 110)
		if (min == 30 && max == 110) {
			return new OptionInstance.IntRange(30, 80);
		}
		return new OptionInstance.IntRange(min, max);
	}

	// =========================================================================
	// 3. FOV LABEL: "Quake Semi-Pro" at max (unchanged)
	// =========================================================================
	@Inject(
			method = "genericValueLabel(Lnet/minecraft/network/chat/Component;I)Lnet/minecraft/network/chat/Component;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void coraline$customFovLabel(Component text, int value, CallbackInfoReturnable<Component> cir) {
		if (value == 80 && text instanceof MutableComponent mutable) {
			if (mutable.getContents() instanceof TranslatableContents translatable) {
				if ("options.fov".equals(translatable.getKey())) {
					cir.setReturnValue(Options.genericValueLabel(text, Component.literal("Quake Semi-Pro")));
				}
			}
		}
	}

	// =========================================================================
	// 4. ENTITY DISTANCE: replace with a 4-step cycle button (unchanged)
	//    Steps: 0.5 (Tiny), 1.0 (Short), 1.5 (Decent), 2.0 (Default)
	// =========================================================================
	@Inject(method = "<init>", at = @At("TAIL"))
	private void coraline$replaceOptionsAtTail(Minecraft minecraft, File gameDirectory, CallbackInfo ci) {
		// ---- 4a. Entity Distance cycle button --------------------------------
		this.entityDistanceScaling = new OptionInstance<>(
				"options.entityDistanceScaling",
				OptionInstance.noTooltip(),
				(caption, value) -> {
					if (value == 0.5) return Component.literal("Tiny");
					if (value == 1.0) return Component.literal("Short");
					if (value == 1.5) return Component.literal("Decent");
					return Component.literal("Default");
				},
				new OptionInstance.Enum<>(
						List.of(0.5, 1.0, 1.5, 2.0),
						Codec.DOUBLE
				),
				2.0,
				value -> {}
		);

		// ---- 4b. Graphics Mode: re-build with an onValueUpdate that syncs clouds ---
		//
		// We preserve the exact same AltEnum setup that Vanilla uses (Fast/Fancy only,
		// since Fabulous is already stripped by the @Redirect above), but now the
		// onValueUpdate consumer also pushes the matching CloudStatus automatically.
		//
		// NOTE: The Codec and tooltip suppliers are copied verbatim from Options.java.
		// The AltEnum's altCondition (isSkippingFabulous) is kept so nothing else breaks.

		Options self = (Options)(Object)this;

		this.graphicsMode = new OptionInstance<>(
				"options.graphics",
				OptionInstance.noTooltip(),
				(caption, status) -> {
					net.minecraft.network.chat.MutableComponent label =
							Component.translatable(status.getKey());
					return status == GraphicsStatus.FABULOUS
							? label.withStyle(net.minecraft.ChatFormatting.ITALIC)
							: label;
				},
				new OptionInstance.AltEnum<>(
						List.of(GraphicsStatus.FAST, GraphicsStatus.FANCY),
						List.of(GraphicsStatus.FAST, GraphicsStatus.FANCY),
						() -> Minecraft.getInstance().isRunning()
								&& Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous(),
						(opt, status) -> {
							net.minecraft.client.renderer.GpuWarnlistManager gpuWarn =
									Minecraft.getInstance().getGpuWarnlistManager();
							if (status == GraphicsStatus.FABULOUS && gpuWarn.willShowWarning()) {
								gpuWarn.showWarning();
							} else {
								opt.set(status);
								Minecraft.getInstance().levelRenderer.allChanged();
							}
						},
						Codec.INT.xmap(GraphicsStatus::byId, GraphicsStatus::getId)
				),
				GraphicsStatus.FANCY,
				// onValueUpdate: sync clouds to match the chosen graphics quality
				status -> {
					CloudStatus target = (status == GraphicsStatus.FAST)
							? CloudStatus.FAST
							: CloudStatus.FANCY;
					self.cloudStatus().set(target);
				}
		);

		// Sync clouds once immediately so the saved value matches on first load.
		GraphicsStatus current = this.graphicsMode.get();
		this.cloudStatus.set(current == GraphicsStatus.FAST ? CloudStatus.FAST : CloudStatus.FANCY);
	}
}