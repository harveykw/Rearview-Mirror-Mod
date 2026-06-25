package com.kookie.rearview.mixin;

import com.kookie.rearview.FramebufferSwapAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientFramebufferAccessor implements FramebufferSwapAccess {
    /*
     GameRenderer writes to MinecraftClient.framebuffer in order to render the normal view. For the rearmirror pass we temporarily
     point that field at our offscreen SimpleFramebuffer, render the world once, then restore it.
     */
    @Mutable
    @Final
    @Shadow
    private Framebuffer framebuffer;


    @Override
    public void rearviewmirror$setFramebuffer(Framebuffer framebuffer) {
        this.framebuffer = framebuffer;
    }
}
