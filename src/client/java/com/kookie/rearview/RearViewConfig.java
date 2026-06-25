package com.kookie.rearview;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RearViewConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "rearviewmirror.json";

    public boolean enabled = true;

    // Default mirror coordinates.
    public int x = 8;
    public int y = 8;
    public int width = 240;
    public int height = 80;

    // Default FOV, only applied to rear pass.
    public float mirrorFovDegrees = 90.0f;

    // Performance optimization by skipping frames and lowering animations.
    public int updateEveryNFrames = 1;

    public boolean drawBorder = true;
    public boolean drawLabels = true;
    public boolean lowMirrorSettings = true;

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public static RearViewConfig load() {
        Path path = path();
        if (!Files.exists(path)) {
            RearViewConfig created = new RearViewConfig();
            created.save();
            return created;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            RearViewConfig loaded = GSON.fromJson(reader, RearViewConfig.class);
            if (loaded == null) {
                loaded = new RearViewConfig();
            }
            loaded.sanitize();
            return loaded;
        } catch (Exception ex) {
            RearViewMirrorClient.LOGGER.warn("Could not read rearview mirror config; using defaults", ex);
            RearViewConfig fallback = new RearViewConfig();
            fallback.save();
            return fallback;
        }
    }

    public void save() {
        sanitize();
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ex) {
            RearViewMirrorClient.LOGGER.warn("Could not save rearview mirror config", ex);
        }
    }

    // Bound checking.
    public void sanitize() {
        width = clamp(width, 64, 1000);
        height = clamp(height, 36, 600);
        mirrorFovDegrees = Math.max(30.0f, Math.min(140.0f, mirrorFovDegrees));
        updateEveryNFrames = clamp(updateEveryNFrames, 1, 20);
    }

    public void clampToScreen(int screenWidth, int screenHeight) {
        sanitize();

        // We make sure we dont lose the mirror offscreen.
        width = Math.min(width, Math.max(64, screenWidth));
        height = Math.min(height, Math.max(36, screenHeight));
        x = clamp(x, 0, Math.max(0, screenWidth - width));
        y = clamp(y, 0, Math.max(0, screenHeight - height));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
