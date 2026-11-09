package de.highcraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import de.highcraft.HighCraft;
import de.highcraft.config.HighCraftConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;

/**
 * Full-screen shader for drug states.
 * <p>
 * Applied as the lowest HUD layer - i.e. after the world including the hand has been rendered.
 * With Iris, the shader pack has already written its final image into the main framebuffer
 * at this point; with Sodium the chunk pipeline is long done. Only the vanilla PostChain is used.
 */
public final class DrugPostEffect {
    private static PostChain chain;
    private static boolean failed;
    private static int width, height;

    private DrugPostEffect() {}

    public static void invalidate() {
        if (chain != null) {
            chain.close();
            chain = null;
        }
        failed = false;
    }

    /** HUD layer (registerBelowAll) - before all other HUD elements. */
    public static void renderLayer(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.DeltaTracker delta) {
        if (!DrugVisuals.anyActive()) return;
        if (IrisCompat.isShaderPackActive() && !HighCraftConfig.SHADERS_WITH_SHADERPACK.get()) return;
        g.flush();
        render(delta.getGameTimeDeltaPartialTick(false));
    }

    public static void render(float partialTick) {
        if (!HighCraftConfig.ENABLE_SHADERS.get() || failed || !DrugVisuals.anyActive()) return;
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        try {
            if (chain == null) {
                chain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), main, HighCraft.id("shaders/post/drugs.json"));
                width = height = -1;
            }
            if (main.width != width || main.height != height) {
                width = main.width;
                height = main.height;
                chain.resize(width, height);
            }
            float k = HighCraftConfig.SHADER_INTENSITY.get().floatValue();
            chain.setUniform("HcTime", DrugVisuals.time(partialTick));
            chain.setUniform("Stoned", DrugVisuals.get(DrugVisuals.STONED, partialTick) * k);
            chain.setUniform("Trip", DrugVisuals.get(DrugVisuals.TRIP, partialTick) * k);
            chain.setUniform("Rush", (DrugVisuals.get(DrugVisuals.RUSH, partialTick) + DrugVisuals.get(DrugVisuals.NICOTINE, partialTick) * 0.25f) * k);
            chain.setUniform("Sedated", DrugVisuals.get(DrugVisuals.SEDATED, partialTick) * k);
            chain.setUniform("Withdrawal", (DrugVisuals.get(DrugVisuals.WITHDRAWAL, partialTick) + DrugVisuals.get(DrugVisuals.CRASH, partialTick) * 0.6f
                    + DrugVisuals.get(DrugVisuals.COMEDOWN, partialTick) * 0.7f) * k);
            chain.setUniform("Overdose", DrugVisuals.get(DrugVisuals.OVERDOSE, partialTick) * k);
            chain.setUniform("Acid", DrugVisuals.get(DrugVisuals.ACID, partialTick) * k);
            chain.setUniform("Meth", DrugVisuals.get(DrugVisuals.METH, partialTick) * k);
            chain.setUniform("Ecstasy", DrugVisuals.get(DrugVisuals.ECSTASY, partialTick) * k);

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            chain.process(partialTick);
            main.bindWrite(true);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        } catch (Exception e) {
            HighCraft.LOGGER.error("Drug shader could not be loaded/applied - disabling shaders", e);
            failed = true;
            invalidate();
            failed = true;
        }
    }
}
