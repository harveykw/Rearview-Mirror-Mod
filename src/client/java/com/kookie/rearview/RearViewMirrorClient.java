package com.kookie.rearview;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RearViewMirrorClient implements ClientModInitializer {
    public static final String MOD_ID = "rearviewmirror";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static RearViewConfig config;
    private static MirrorRenderer renderer;

    private static KeyBinding toggleKey;
    private static KeyBinding configureKey;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static RearViewConfig getConfig() {
        return config;
    }

    public static MirrorRenderer getRenderer() {
        return renderer;
    }

    @Override
    public void onInitializeClient() {
        config = RearViewConfig.load();
        renderer = new MirrorRenderer(config);

        KeyBinding.Category category = KeyBinding.Category.create(id("controls"));

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rearviewmirror.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                category
        ));

        configureKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rearviewmirror.configure",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                config.enabled = !config.enabled;
                config.save();
            }

            while (configureKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new RearViewConfigScreen(config, client.currentScreen));
                }
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> renderer.close());
    }

    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
}
