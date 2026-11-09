package de.highcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.highcraft.block.CannabisCropBlock;
import de.highcraft.block.MachineBlock;
import de.highcraft.block.entity.GrowTentBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Renders the growing plant inside the grow tent - growth stage follows the progress. */
public class GrowTentRenderer implements BlockEntityRenderer<GrowTentBlockEntity> {
    private final BlockRenderDispatcher blocks;

    public GrowTentRenderer(BlockEntityRendererProvider.Context ctx) {
        this.blocks = ctx.getBlockRenderDispatcher();
    }

    @Override
    public void render(GrowTentBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Block block = be.displayBlock();
        if (block == Blocks.AIR) return;
        BlockState state = block.defaultBlockState();
        Property<?> ageProp = block.getStateDefinition().getProperty("age");
        int age = 0;
        if (ageProp instanceof IntegerProperty ip) {
            int max = ip.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
            age = Math.min(max, (int) Math.floor(be.displayProgress() * (max + 1)));
            state = state.setValue(ip, age);
        }
        boolean lit = be.getBlockState().getValue(MachineBlock.LIT);
        int packed = lit ? LightTexture.FULL_BRIGHT : light;
        boolean tall = block instanceof CannabisCropBlock;
        float scale = tall ? 0.44f : 0.72f;

        pose.pushPose();
        pose.translate(0.5, 0.0625, 0.5);
        pose.scale(scale, scale, scale);
        pose.translate(-0.5, 0, -0.5);
        blocks.renderSingleBlock(state, pose, buffers, packed, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        if (tall && age >= CannabisCropBlock.DOUBLE_AGE) {
            pose.translate(0, 1, 0);
            blocks.renderSingleBlock(state.setValue(CannabisCropBlock.HALF, DoubleBlockHalf.UPPER), pose, buffers, packed,
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        }
        pose.popPose();
    }
}
