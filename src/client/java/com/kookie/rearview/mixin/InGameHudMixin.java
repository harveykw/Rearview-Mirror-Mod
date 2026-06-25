package com.kookie.rearview.mixin;

import com.kookie.rearview.RearViewMirrorClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void rearviewmirror$render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (RearViewMirrorClient.getRenderer() != null && !RearViewMirrorClient.getRenderer().isRenderingMirrorPass()) {
            RearViewMirrorClient.getRenderer().renderHud(context, tickCounter);
        }
    }
}
