package com.kookie.rearview.mixin;

import com.kookie.rearview.RearViewMirrorClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    public abstract float getFarPlaneDistance();

    /*
     Updates the mirror framebuffer just before vanilla renders the main world view.
     */
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void rearviewmirror$updateMirrorAfterMainWorld(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (RearViewMirrorClient.getRenderer() != null && !RearViewMirrorClient.getRenderer().isRenderingMirrorPass()) {
            RearViewMirrorClient.getRenderer().updateMirrorFramebuffers(tickCounter);
        }
    }

    // Remove floating hand;
    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void rearviewmirror$skipHandInMirror(float tickProgress, boolean sleeping, Matrix4f positionMatrix, CallbackInfo ci) {
        if (RearViewMirrorClient.getRenderer() != null && RearViewMirrorClient.getRenderer().isRenderingMirrorPass()) {
            ci.cancel();
        }
    }


    // Remove instance of rearview mirror within the mirror image
    @Redirect(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameOverlayRenderer;renderOverlays(ZFLnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V"
            )
    )
    private void rearviewmirror$skipScreenOverlaysInMirror(InGameOverlayRenderer overlayRenderer, boolean allowSleepOverlay, float tickProgress, OrderedRenderCommandQueue orderedRenderCommandQueue) {
        if (RearViewMirrorClient.getRenderer() == null || !RearViewMirrorClient.getRenderer().isRenderingMirrorPass()) {
            overlayRenderer.renderOverlays(allowSleepOverlay, tickProgress, orderedRenderCommandQueue);
        }
    }

    // Adjust FOV when mirror stretches to prevent super warping.
    @Inject(method = "getBasicProjectionMatrix", at = @At("HEAD"), cancellable = true)
    private void rearviewmirror$useMirrorProjection(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        if (RearViewMirrorClient.getRenderer() != null && RearViewMirrorClient.getRenderer().isRenderingMirrorPass()) {
            Matrix4f projection = new Matrix4f().perspective(
                    RearViewMirrorClient.getRenderer().getMirrorFovDegrees() * ((float) Math.PI / 180.0f),
                    RearViewMirrorClient.getRenderer().getMirrorAspectRatio(),
                    0.05f,
                    getFarPlaneDistance()
            );
            cir.setReturnValue(projection);
        }
    }
}
