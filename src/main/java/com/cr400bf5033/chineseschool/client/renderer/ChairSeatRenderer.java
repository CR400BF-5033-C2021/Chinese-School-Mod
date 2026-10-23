package com.cr400bf5033.chineseschool.client.renderer;

import com.cr400bf5033.chineseschool.entity.ChairSeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * 座位实体的渲染器：什么都不画（座位本身是不可见的）。
 */
public class ChairSeatRenderer extends EntityRenderer<ChairSeatEntity, EntityRenderState> {

    public ChairSeatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
