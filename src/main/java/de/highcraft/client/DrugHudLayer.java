package de.highcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.highcraft.HighCraft;
import de.highcraft.config.HighCraftConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Coloured, pulsing vignette depending on the drug state (also works without shaders). */
public final class DrugHudLayer {
    private static final ResourceLocation VIGNETTE = HighCraft.id("textures/misc/drug_vignette.png");

    private DrugHudLayer() {}

    public static void render(GuiGraphics g, DeltaTracker delta) {
        if (!HighCraftConfig.ENABLE_HUD_OVERLAY.get() || !DrugVisuals.anyActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        // With an active Iris shader pack and screen shaders turned off, the vignette stays on as a fallback
        float pt = delta.getGameTimeDeltaPartialTick(false);
        float t = DrugVisuals.time(pt);
        int w = g.guiWidth(), h = g.guiHeight();

        float stoned = DrugVisuals.get(DrugVisuals.STONED, pt);
        float trip = DrugVisuals.get(DrugVisuals.TRIP, pt);
        float rush = DrugVisuals.get(DrugVisuals.RUSH, pt);
        float sedated = DrugVisuals.get(DrugVisuals.SEDATED, pt);
        float withdrawal = Math.max(DrugVisuals.get(DrugVisuals.WITHDRAWAL, pt),
                Math.max(DrugVisuals.get(DrugVisuals.CRASH, pt) * 0.6f, DrugVisuals.get(DrugVisuals.COMEDOWN, pt) * 0.7f));
        float overdose = DrugVisuals.get(DrugVisuals.OVERDOSE, pt);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        if (stoned > 0.01f) vignette(g, w, h, 0.35f, 0.75f, 0.25f, stoned * (0.28f + 0.06f * Mth.sin(t * 0.9f)));
        if (trip > 0.01f) {
            float hue = (t * 0.08f) % 1f;
            int rgb = Mth.hsvToRgb(hue, 0.8f, 1f);
            vignette(g, w, h, ((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, trip * 0.35f);
        }
        if (rush > 0.01f) {
            float beat = (float) Math.pow(Math.max(0, Mth.sin(t * 8f)), 8);
            vignette(g, w, h, 1f, 1f, 1f, rush * 0.25f * beat);
        }
        float acid = DrugVisuals.get(DrugVisuals.ACID, pt);
        if (acid > 0.01f) {
            for (int i = 0; i < 3; i++) {
                int rgb = Mth.hsvToRgb((t * 0.15f + i / 3f) % 1f, 0.9f, 1f);
                vignette(g, w, h, ((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f,
                        acid * 0.18f * (0.6f + 0.4f * Mth.sin(t * 2f + i * 2.1f)));
            }
        }
        float meth = DrugVisuals.get(DrugVisuals.METH, pt);
        if (meth > 0.01f) {
            float flicker = 0.5f + 0.5f * Mth.sin(t * 23f) * Mth.sin(t * 7.3f);
            vignette(g, w, h, 0.55f, 0.95f, 1f, meth * (0.18f + 0.12f * flicker));
        }
        float ecstasy = DrugVisuals.get(DrugVisuals.ECSTASY, pt);
        if (ecstasy > 0.01f) {
            float beat = (float) Math.pow(Math.max(0, Mth.sin(t * 6.7f)), 6);
            vignette(g, w, h, 1f, 0.45f, 0.8f, ecstasy * (0.22f + 0.2f * beat));
        }
        if (sedated > 0.01f) vignette(g, w, h, 0.25f, 0.1f, 0.3f, sedated * 0.5f);
        if (withdrawal > 0.01f) vignette(g, w, h, 0.2f, 0.25f, 0.35f, withdrawal * (0.35f + 0.1f * Mth.sin(t * 3f)));
        if (overdose > 0.01f) {
            float beat = (float) Math.pow(Math.max(0, Mth.sin(t * 5f)), 4);
            vignette(g, w, h, 0.7f, 0f, 0f, overdose * (0.45f + 0.35f * beat));
        }

        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void vignette(GuiGraphics g, int w, int h, float r, float gr, float b, float a) {
        g.setColor(r, gr, b, Mth.clamp(a, 0f, 1f));
        g.blit(VIGNETTE, 0, 0, -90, 0.0F, 0.0F, w, h, w, h);
    }
}
