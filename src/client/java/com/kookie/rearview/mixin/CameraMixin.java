package com.kookie.rearview.mixin;

import com.kookie.rearview.RearViewMirrorClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    /*
     Exposes Camera.setRotation so the mirror pass can point the camera backward.
     Runs after vanilla positions the camera, then mirror passes flips it 180 degrees.

     */
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);


    @Inject(method = "update", at = @At("TAIL"))
    private void rearviewmirror$turnCameraAround(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        if (RearViewMirrorClient.getRenderer() != null && RearViewMirrorClient.getRenderer().isRenderingMirrorPass()) {
            setRotation(focusedEntity.getYaw(tickProgress) + 180.0f, 0.0f);
        }
    }
}
