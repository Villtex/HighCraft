package de.highcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.highcraft.block.CannabisCropBlock;
import de.highcraft.block.MachineBlock;
import de.highcraft.block.entity.GrowTentBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Renders the growing plant inside the (two blocks tall) grow tent - growth stage follows the progress.
 * The plant state is cached in the block entity, so a frame only renders the block model.
 */
public class GrowTentRenderer implements BlockEntityRenderer<GrowTentBlockEntity> {
    /** Fits the 14 px wide interior; a mature cannabis plant (2 blocks) ends just below the lamp in the roof. */
    private static final float SCALE = 0.84f;
    private static final float FLOOR = 1 / 16f;

    private final BlockRenderDispatcher blocks;

    public GrowTentRenderer(BlockEntityRendererProvider.Context ctx) {
        this.blocks = ctx.getBlockRenderDispatcher();
    }

    @Override
    public void render(GrowTentBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.renderState();
        if (state == null) return;
        boolean lit = be.getBlockState().getValue(MachineBlock.LIT);
        int packed = lit ? LightTexture.FULL_BRIGHT : light;

        pose.pushPose();
        pose.translate(0.5, FLOOR, 0.5);
        pose.scale(SCALE, SCALE, SCALE);
        pose.translate(-0.5, 0, -0.5);
        blocks.renderSingleBlock(state, pose, buffers, packed, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        if (state.getBlock() instanceof CannabisCropBlock && state.getValue(CannabisCropBlock.AGE) >= CannabisCropBlock.DOUBLE_AGE) {
            pose.translate(0, 1, 0);
            blocks.renderSingleBlock(state.setValue(CannabisCropBlock.HALF, DoubleBlockHalf.UPPER), pose, buffers, packed,
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        }
        pose.popPose();
    }

    /** The plant reaches into the upper half - don't cull it when only the roof is on screen. */
    @Override
    public AABB getRenderBoundingBox(GrowTentBlockEntity be) {
        BlockPos p = be.getBlockPos();
        return new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 2, p.getZ() + 1);
    }
}
