package de.highcraft.client.screen;

import de.highcraft.HighCraft;
import de.highcraft.block.MachineType;
import de.highcraft.block.entity.GrowTentBlockEntity;
import de.highcraft.menu.MachineMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared screen of all machines. Texture layout ({@code textures/gui/<machine>.png}):
 * <ul>
 *     <li>(176, 0)  14x14 flame (full)</li>
 *     <li>(176, 14) 24x17 arrow (full)</li>
 *     <li>(176, 31) 10x52 energy bar (full)</li>
 *     <li>(186, 31) 10x52 water tank (full)</li>
 * </ul>
 */
public class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    public static final int ENERGY_X = 8, ENERGY_Y = 17, BAR_W = 10, BAR_H = 52;
    public static final int WATER_X = 24, WATER_Y = 17;
    private final ResourceLocation texture;

    public MachineScreen(MachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.texture = HighCraft.id("textures/gui/" + menu.machine().id + ".png");
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public MachineType machine() { return menu.machine(); }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.blit(texture, x, y, 0, 0, imageWidth, imageHeight);
        MachineType m = menu.machine();

        // Flame
        if (m.flamePos != null && menu.burn() > 0) {
            int h = Math.max(1, Math.round(menu.burn() * 13)) ;
            g.blit(texture, x + m.flamePos[0], y + m.flamePos[1] + 13 - h, 176, 13 - h, 14, h + 1);
        }
        // Progress arrow
        int w = Math.round(menu.progress() * 24);
        if (w > 0) g.blit(texture, x + m.arrowPos[0], y + m.arrowPos[1], 176, 14, w, 17);
        // Energy
        int eh = Math.round(menu.energyFraction() * BAR_H);
        if (eh > 0) g.blit(texture, x + ENERGY_X, y + ENERGY_Y + BAR_H - eh, 176, 31 + BAR_H - eh, BAR_W, eh);
        // Water (grow tent)
        if (m == MachineType.GROW_TENT) {
            int wh = Math.round(menu.waterFraction() * BAR_H);
            if (wh > 0) g.blit(texture, x + WATER_X, y + WATER_Y + BAR_H - wh, 186, 31 + BAR_H - wh, BAR_W, wh);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        if (menu.machine() == MachineType.REACTOR) {
            boolean vent = menu.data(MachineType.DATA_STATUS) == 1;
            g.drawString(font, Component.translatable(vent ? "gui.highcraft.ventilated" : "gui.highcraft.not_ventilated"),
                    98, 64, vent ? 0x2E7D32 : 0xB02020, false);
        } else if (menu.machine() == MachineType.CRYSTALLIZER) {
            int cool = menu.data(MachineType.DATA_STATUS);
            g.drawString(font, Component.translatable("gui.highcraft.cooling." + cool), 80, 64, cool == 0 ? 0xB02020 : 0x1E5AA8, false);
        }
        if (menu.machine() == MachineType.GROW_TENT) {
            int light = menu.data(MachineType.DATA_LIGHT);
            Component text = Component.translatable("gui.highcraft.light." + light);
            int color = light == GrowTentBlockEntity.LIGHT_NONE ? 0xB02020 : 0x2E7D32;
            g.drawString(font, text, 98, 64, color, false);
            if (menu.data(MachineType.DATA_FERTILIZED) == 1) {
                g.drawString(font, Component.translatable("gui.highcraft.fertilized"), 62, 18, 0x6D4C41, false);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        int rx = mouseX - leftPos, ry = mouseY - topPos;
        MachineType m = menu.machine();
        List<Component> tip = new ArrayList<>();
        if (in(rx, ry, ENERGY_X, ENERGY_Y, BAR_W, BAR_H)) {
            tip.add(Component.translatable("gui.highcraft.energy", menu.data(MachineType.DATA_ENERGY), MachineType.ENERGY_CAPACITY));
            tip.add(Component.translatable("gui.highcraft.energy_hint").withStyle(ChatFormatting.GRAY));
        } else if (m == MachineType.GROW_TENT && in(rx, ry, WATER_X, WATER_Y, BAR_W, BAR_H)) {
            tip.add(Component.translatable("gui.highcraft.water", menu.data(MachineType.DATA_WATER), MachineType.WATER_CAPACITY));
        } else if (in(rx, ry, m.arrowPos[0], m.arrowPos[1], 24, 17)) {
            tip.add(Component.translatable("gui.highcraft.progress", Math.round(menu.progress() * 100)));
        }
        if (!tip.isEmpty()) g.renderComponentTooltip(font, tip, mouseX, mouseY);
    }

    private static boolean in(int x, int y, int rx, int ry, int w, int h) {
        return x >= rx && x < rx + w && y >= ry && y < ry + h;
    }
}
