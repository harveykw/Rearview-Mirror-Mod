package com.kookie.rearview.mixin;

import com.kookie.rearview.RearViewMirrorClient;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



/*
Frankly, I'm not sure if this has a measurable impact. Generating extra frames is a constant multiplier on the complexity regardless.
 */

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void rearviewmirror$skipCloudsInLowMirrorSettings(
            FrameGraphBuilder frameGraphBuilder,
            CloudRenderMode cloudRenderMode,
            Vec3d cameraPos,
            long ticks,
            float tickProgress,
            int color,
            float cloudHeight,
            CallbackInfo ci
    ) {
        if (rearviewmirror$useLowMirrorSettings()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void rearviewmirror$skipWeatherInLowMirrorSettings(
            FrameGraphBuilder frameGraphBuilder,
            GpuBufferSlice fog,
            CallbackInfo ci
    ) {
        if (rearviewmirror$useLowMirrorSettings()) {
            ci.cancel();
        }
    }

    @Inject(method = "canDrawEntityOutlines", at = @At("HEAD"), cancellable = true)
    private void rearviewmirror$skipEntityOutlinesInLowMirrorSettings(CallbackInfoReturnable<Boolean> cir) {
        if (rearviewmirror$useLowMirrorSettings()) {
            cir.setReturnValue(false);
        }
    }

    private static boolean rearviewmirror$useLowMirrorSettings() {
        return RearViewMirrorClient.getRenderer() != null
                && RearViewMirrorClient.getRenderer().isRenderingMirrorPass()
                && RearViewMirrorClient.getConfig() != null
                && RearViewMirrorClient.getConfig().lowMirrorSettings;
    }
}
