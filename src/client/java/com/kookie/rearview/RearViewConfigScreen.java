package com.kookie.rearview;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class RearViewConfigScreen extends Screen {
    private static final int RESIZE_HANDLE_SIZE = 10;

    private final RearViewConfig config;
    private final Screen parent;
    private boolean dragging;
    private boolean resizing;
    private int dragOffsetX;
    private int dragOffsetY;
    private FovSlider fovSlider;
    private RefreshRateSlider refreshRateSlider;

    public RearViewConfigScreen(RearViewConfig config, Screen parent) {
        super(Text.literal("Rearview Mirror Config"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        config.clampToScreen(width, height);
        fovSlider = addDrawableChild(new FovSlider(12, 120, 220, 20, config));
        refreshRateSlider = addDrawableChild(new RefreshRateSlider(12, 144, 220, 20, config));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void close() {
        config.save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Let vanilla render background first, then draw our overlay on top.
        super.render(context, mouseX, mouseY, delta);

        config.clampToScreen(width, height);
        int x = config.x;
        int y = config.y;
        int w = config.width;
        int h = config.height;

        drawOverlayAroundMirror(context, x, y, w, h);
        drawBorder(context, x, y, w, h, 0xFFFFFFFF);
        drawResizeHandle(context, x, y, w, h);

        int infoY = 12;
        context.drawTextWithShadow(textRenderer, "Rearview Mirror setup", 12, infoY, 0xFFFF55);
        infoY += 12;
        context.drawTextWithShadow(textRenderer, "Left-drag the box to move it", 12, infoY, 0xDDDDDD);
        infoY += 12;
        context.drawTextWithShadow(textRenderer, "Drag the bottom-right handle to resize it", 12, infoY, 0xDDDDDD);
        infoY += 12;
        context.drawTextWithShadow(textRenderer, "Arrow keys move it; Shift + arrows resize it", 12, infoY, 0xDDDDDD);
        infoY += 12;
        context.drawTextWithShadow(textRenderer, "Space toggles enabled", 12, infoY, 0xDDDDDD);
        infoY += 12;
        context.drawTextWithShadow(textRenderer, "PageUp/PageDown changes mirror refresh rate", 12, infoY, 0xDDDDDD);
        infoY += 34;
        context.drawTextWithShadow(textRenderer, "Mode: Rear view", 12, infoY, 0xFFFFFF);
        infoY += 12;
        context.drawTextWithShadow(textRenderer, "Enabled: " + config.enabled, 12, infoY, 0xFFFFFF);
        infoY += 12;
        context.drawTextWithShadow(textRenderer, "Box: " + config.width + "x" + config.height + " at " + config.x + "," + config.y, 12, infoY, 0xFFFFFF);
        infoY += 12;
        context.drawTextWithShadow(textRenderer, "Renderer: offscreen rear framebuffer", 12, infoY, 0xFFFFFF);

        if (fovSlider != null) {
            fovSlider.render(context, mouseX, mouseY, delta);
        }
        if (refreshRateSlider != null) {
            refreshRateSlider.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.buttonInfo().button();

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && insideResizeHandle(mouseX, mouseY)) {
            resizing = true;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inside(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = (int) mouseX - config.x;
            dragOffsetY = (int) mouseY - config.y;
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.buttonInfo().button();

        if (resizing && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            config.width = (int) mouseX - config.x;
            config.height = (int) mouseY - config.y;
            config.clampToScreen(width, height);
            return true;
        }
        if (dragging && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            config.x = (int) mouseX - dragOffsetX;
            config.y = (int) mouseY - dragOffsetY;
            config.clampToScreen(width, height);
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        int button = click.buttonInfo().button();

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && resizing) {
            resizing = false;
            config.save();
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging) {
            dragging = false;
            config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        boolean shift = input.hasShift();
        int step = shift ? 8 : 2;

        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER -> {
                close();
                return true;
            }
            case GLFW.GLFW_KEY_SPACE -> {
                config.enabled = !config.enabled;
                config.save();
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                if (shift) config.height -= step; else config.y -= step;
                config.clampToScreen(width, height);
                config.save();
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (shift) config.height += step; else config.y += step;
                config.clampToScreen(width, height);
                config.save();
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (shift) config.width -= step; else config.x -= step;
                config.clampToScreen(width, height);
                config.save();
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (shift) config.width += step; else config.x += step;
                config.clampToScreen(width, height);
                config.save();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_UP -> {
                config.updateEveryNFrames = Math.max(1, config.updateEveryNFrames - 1);
                if (refreshRateSlider != null) {
                    refreshRateSlider.syncFromConfig();
                }
                config.save();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                config.updateEveryNFrames = Math.min(20, config.updateEveryNFrames + 1);
                if (refreshRateSlider != null) {
                    refreshRateSlider.syncFromConfig();
                }
                config.save();
                return true;
            }
            default -> {
                return super.keyPressed(input);
            }
        }
    }

    private boolean inside(double mouseX, double mouseY) {
        return mouseX >= config.x && mouseX <= config.x + config.width && mouseY >= config.y && mouseY <= config.y + config.height;
    }

    private boolean insideResizeHandle(double mouseX, double mouseY) {
        return mouseX >= config.x + config.width - RESIZE_HANDLE_SIZE
                && mouseX <= config.x + config.width
                && mouseY >= config.y + config.height - RESIZE_HANDLE_SIZE
                && mouseY <= config.y + config.height;
    }

    private void drawOverlayAroundMirror(DrawContext context, int x, int y, int mirrorWidth, int mirrorHeight) {
        int right = x + mirrorWidth;
        int bottom = y + mirrorHeight;
        int overlayColor = 0xAA000000;


        context.fill(0, 0, width, y, overlayColor);
        context.fill(0, bottom, width, height, overlayColor);
        context.fill(0, y, x, bottom, overlayColor);
        context.fill(right, y, width, bottom, overlayColor);
    }

    private static void drawResizeHandle(DrawContext context, int x, int y, int width, int height) {
        int right = x + width;
        int bottom = y + height;
        context.fill(right - RESIZE_HANDLE_SIZE, bottom - 2, right, bottom, 0xFFFFFFFF);
        context.fill(right - 2, bottom - RESIZE_HANDLE_SIZE, right, bottom, 0xFFFFFFFF);
        context.fill(right - 6, bottom - 4, right - 4, bottom - 2, 0xFFFFFFFF);
        context.fill(right - 4, bottom - 6, right - 2, bottom - 4, 0xFFFFFFFF);
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static final class FovSlider extends SliderWidget {
        private static final float MIN_FOV = 30.0f;
        private static final float MAX_FOV = 140.0f;

        private final RearViewConfig config;

        private FovSlider(int x, int y, int width, int height, RearViewConfig config) {
            super(x, y, width, height, Text.empty(), toSliderValue(config.mirrorFovDegrees));
            this.config = config;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Mirror FOV: " + Math.round(config.mirrorFovDegrees) + " degrees"));
        }

        @Override
        protected void applyValue() {
            config.mirrorFovDegrees = MIN_FOV + (float) value * (MAX_FOV - MIN_FOV);
            config.save();
        }

        private static double toSliderValue(float fovDegrees) {
            return (Math.max(MIN_FOV, Math.min(MAX_FOV, fovDegrees)) - MIN_FOV) / (MAX_FOV - MIN_FOV);
        }
    }

    private static final class RefreshRateSlider extends SliderWidget {
        // Change this range to effect skipping range
        private static final int MIN_FRAMES = 1;
        private static final int MAX_FRAMES = 5;

        private final RearViewConfig config;

        private RefreshRateSlider(int x, int y, int width, int height, RearViewConfig config) {
            super(x, y, width, height, Text.empty(), toSliderValue(config.updateEveryNFrames));
            this.config = config;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (config.updateEveryNFrames <= 1) {
                setMessage(Text.literal("Mirror refresh: every frame"));
            } else {
                setMessage(Text.literal("Mirror refresh: every " + config.updateEveryNFrames + " frames"));
            }
        }

        @Override
        protected void applyValue() {
            config.updateEveryNFrames = MIN_FRAMES + (int) Math.round(value * (MAX_FRAMES - MIN_FRAMES));
            config.save();
        }

        private void syncFromConfig() {
            value = toSliderValue(config.updateEveryNFrames);
            updateMessage();
        }

        private static double toSliderValue(int updateEveryNFrames) {
            int clamped = Math.max(MIN_FRAMES, Math.min(MAX_FRAMES, updateEveryNFrames));
            return (double) (clamped - MIN_FRAMES) / (MAX_FRAMES - MIN_FRAMES);
        }
    }
}
