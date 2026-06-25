package com.kookie.rearview;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.kookie.rearview.mixin.DrawContextAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.ColorHelper;


public final class MirrorRenderer implements AutoCloseable {
    private final RearViewConfig config;
    private SimpleFramebuffer framebuffer;
    private int framebufferWidth;
    private int framebufferHeight;
    private boolean hasFrame;

    private boolean renderingMirrorPass;
    private int frameCounter;
    private long lastFailureNanos;
    private long lastSuccessfulMirrorFrameNanos;


    public MirrorRenderer(RearViewConfig config) {
        this.config = config;
    }


    public boolean isRenderingMirrorPass() {
        return renderingMirrorPass;
    }

    public float getMirrorAspectRatio() {
        int width = framebufferWidth > 0 ? framebufferWidth : config.width;
        int height = framebufferHeight > 0 ? framebufferHeight : config.height;
        return Math.max(1.0f, (float) width / Math.max(1, height));
    }

    public float getMirrorFovDegrees() {
        config.sanitize();
        return config.mirrorFovDegrees;
    }



    /*
    We are sure to call this immediately before the normal minecraft render pass.
     */
    public void updateMirrorFramebuffers(RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (renderingMirrorPass || !config.enabled || client.world == null || client.player == null) {
            return;
        }

        config.clampToScreen(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());

        frameCounter++;
        if (frameCounter % Math.max(1, config.updateEveryNFrames) != 0) {
            return;
        }

        renderWorldToMirror(client, tickCounter);
    }


    public void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (renderingMirrorPass || !config.enabled || client.world == null || client.player == null || client.options.hudHidden) {
            return;
        }

        config.clampToScreen(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        drawMirrorBox(client, context);
    }

    // This is a single render call.
    private void renderWorldToMirror(MinecraftClient client, RenderTickCounter tickCounter) {
        double scale = client.getWindow().getScaleFactor();
        int renderWidth = Math.max(1, (int) Math.round(config.width * scale));
        int renderHeight = Math.max(1, (int) Math.round(config.height * scale));

        SimpleFramebuffer mirrorFramebuffer = ensureFramebuffer(renderWidth, renderHeight);
        Framebuffer originalFramebuffer = client.getFramebuffer();

        renderingMirrorPass = true;
        try {
            ((FramebufferSwapAccess) client).rearviewmirror$setFramebuffer(mirrorFramebuffer);
            GlStateManager._viewport(0, 0, renderWidth, renderHeight);
            clearMirrorFramebuffer(mirrorFramebuffer);

            client.gameRenderer.updateCamera(tickCounter);
            client.gameRenderer.renderWorld(tickCounter);

            hasFrame = true;
            lastSuccessfulMirrorFrameNanos = System.nanoTime();
        } catch (Throwable throwable) {
            long now = System.nanoTime();
            if (now - lastFailureNanos > 3_000_000_000L) {
                RearViewMirrorClient.LOGGER.error("Rearview mirror offscreen world pass failed.", throwable);
                lastFailureNanos = now;
            }
        } finally {
            ((FramebufferSwapAccess) client).rearviewmirror$setFramebuffer(originalFramebuffer);
            GlStateManager._viewport(0, 0, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
            renderingMirrorPass = false;
            client.gameRenderer.updateCamera(tickCounter);
        }
    }


    // Ensures the existence of a frame buffer (with the right settings) in order to store the mirror image.
    private SimpleFramebuffer ensureFramebuffer(int width, int height) {
        width = Math.max(1, width);
        height = Math.max(1, height);

        if (framebuffer == null || framebufferWidth != width || framebufferHeight != height) {
            close();
            framebuffer = new SimpleFramebuffer("rearviewmirror_rear", width, height, true);
            framebufferWidth = width;
            framebufferHeight = height;
            hasFrame = false;
        }

        return framebuffer;
    }


    // Janitor for fuckups.
    private static void clearMirrorFramebuffer(SimpleFramebuffer framebuffer) {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                framebuffer.getColorAttachment(),
                ColorHelper.getArgb(255, 0, 0, 0),
                framebuffer.getDepthAttachment(),
                1.0
        );
    }


    private void drawMirrorBox(MinecraftClient client, DrawContext context) {
        int x = config.x;
        int y = config.y;
        int width = config.width;
        int height = config.height;

        context.fill(x, y, x + width, y + height, 0xAA000000);

        //https://docs.fabricmc.net/develop/rendering/basic-conceptss
        if (framebuffer != null && hasFrame && framebuffer.getColorAttachmentView() != null) {
            GpuSampler sampler = RenderSystem.getSamplerCache().get(FilterMode.NEAREST);
            ((DrawContextAccessor) context).rearviewmirror$drawTexturedQuad(
                    RenderPipelines.GUI_TEXTURED,
                    framebuffer.getColorAttachmentView(),
                    sampler,
                    x,
                    y,
                    x + width,
                    y + height,
                    0.0f,
                    1.0f,
                    1.0f,
                    0.0f,
                    0xFFFFFFFF
            );
        }

        if (config.drawBorder) {
            drawBorder(context, x, y, width, height, 0xFFFFFFFF);
        }

        if (config.drawLabels && (framebuffer == null || !hasFrame)) {
            context.drawTextWithShadow(client.textRenderer, "NO FRAME", x + 5, y + height - 13, 0xFF5555);
        }
    }


    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int borderColor) {
        context.fill(x, y, x + width, y + 1, borderColor);
        context.fill(x, y + height - 1, x + width, y + height, borderColor);
        context.fill(x, y, x + 1, y + height, borderColor);
        context.fill(x + width - 1, y, x + width, y + height, borderColor);
    }


    @Override
    public void close() {
        if (framebuffer != null) {
            framebuffer.delete();
            framebuffer = null;
        }
        hasFrame = false;
    }
}
