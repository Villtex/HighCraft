package de.highcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.highcraft.block.DryingRackBlock;
import de.highcraft.block.entity.DryingRackBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the plant parts hanging on the drying rack (gently swinging). */
public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {
    private static final float[] OFFSETS = {-0.29f, -0.097f, 0.097f, 0.29f};
    private final ItemRenderer itemRenderer;

    public DryingRackRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(DryingRackBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (be.getLevel() == null) return;
        Direction facing = be.getBlockState().getValue(DryingRackBlock.FACING);
        float time = (be.getLevel().getGameTime() + partialTick) * 0.05f;
        for (int i = 0; i < DryingRackBlockEntity.SLOTS; i++) {
            ItemStack stack = be.items().get(i);
            if (stack.isEmpty()) continue;
            pose.pushPose();
            pose.translate(0.5, 0, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            pose.translate(OFFSETS[i], 0.84, 0);
            // Swing on the rod
            pose.mulPose(Axis.XP.rotationDegrees(Mth.sin(time + i * 1.7f) * 4f));
            pose.translate(0, -0.24, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(180)); // hang upside down
            pose.scale(0.42f, 0.42f, 0.42f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, be.getLevel(), (int) be.getBlockPos().asLong() + i);
            pose.popPose();
        }
    }
}
