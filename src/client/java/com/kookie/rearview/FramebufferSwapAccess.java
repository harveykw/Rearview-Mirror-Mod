package com.kookie.rearview;

import net.minecraft.client.gl.Framebuffer;

public interface FramebufferSwapAccess {

    // Replaces Minecraft Client's active framebuffer during the offscreen mirror render.
    void rearviewmirror$setFramebuffer(Framebuffer framebuffer);
}
