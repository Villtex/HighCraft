package de.highcraft.client;

import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/**
 * Optional Iris detection without a compile dependency (reflection on the stable Iris API v0).
 */
public final class IrisCompat {
    private static final boolean LOADED = ModList.get() != null && ModList.get().isLoaded("iris");
    private static Object api;
    private static Method isShaderPackInUse;
    private static boolean failed;
    private static boolean cached;
    private static long lastCheck;

    private IrisCompat() {}

    public static boolean isLoaded() {
        return LOADED;
    }

    /** true if Iris is loaded and a shader pack is currently active (checked once per second). */
    public static boolean isShaderPackActive() {
        if (!LOADED || failed) return false;
        long now = System.currentTimeMillis();
        if (now - lastCheck < 1000) return cached;
        lastCheck = now;
        try {
            if (api == null) {
                Class<?> c = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                api = c.getMethod("getInstance").invoke(null);
                isShaderPackInUse = c.getMethod("isShaderPackInUse");
            }
            cached = (Boolean) isShaderPackInUse.invoke(api);
        } catch (Throwable t) {
            failed = true;
            cached = false;
        }
        return cached;
    }
}
