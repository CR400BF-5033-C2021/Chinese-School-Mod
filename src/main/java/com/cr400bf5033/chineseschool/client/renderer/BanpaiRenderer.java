package com.cr400bf5033.chineseschool.client.renderer;

import com.cr400bf5033.chineseschool.block.BanpaiBlock;
import com.cr400bf5033.chineseschool.block.BanpaiBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;


/**
 * 班牌渲染器：把班级名称画在牌面中央。
 */
public class BanpaiRenderer implements BlockEntityRenderer<BanpaiBlockEntity, BanpaiRenderer.State> {

    /** 渲染状态。 */
    public static class State extends BlockEntityRenderState {
        public String text = "";
        public Direction facing = Direction.NORTH;
    }

    public BanpaiRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BanpaiBlockEntity banpai, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(banpai, state, partialTick, cameraPos, overlay);
        state.text = banpai.getText();
        state.facing = banpai.getBlockState().getValue(BanpaiBlock.FACING);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.text == null || state.text.isEmpty()) {
            return;
        }
        Font font = Minecraft.getInstance().font;

        pose.pushPose();
        // 移到方块中心，再按朝向旋转，最后贴到牌面前方
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(new org.joml.Matrix4f().rotateY((float) Math.toRadians(-state.facing.toYRot())));
        pose.translate(0.0F, 0.0F, -0.0625F);
        float scale = 0.009F;
        pose.scale(-scale, -scale, scale);

        // 竖排：一个字一行，整体垂直居中
        String text = state.text;
        float lineHeight = font.lineHeight;
        float totalHeight = text.length() * lineHeight;
        for (int i = 0; i < text.length(); i++) {
            FormattedCharSequence piece = FormattedCharSequence.forward(String.valueOf(text.charAt(i)), Style.EMPTY);
            float width = font.width(piece);
            collector.submitText(pose, -width / 2.0F, i * lineHeight - totalHeight / 2.0F, piece,
                    false, Font.DisplayMode.NORMAL, state.lightCoords, 0xFF000000, 0, 0);
        }
        pose.popPose();
    }
}
