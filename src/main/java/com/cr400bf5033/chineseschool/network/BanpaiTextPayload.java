package com.cr400bf5033.chineseschool.network;

import com.cr400bf5033.chineseschool.block.BanpaiBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端提交班级名称给服务端。
 */
public record BanpaiTextPayload(BlockPos pos, String text) implements CustomPacketPayload {

    public static final Type<BanpaiTextPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("chinese_school", "banpai_text"));

    public static final StreamCodec<FriendlyByteBuf, BanpaiTextPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeUtf(payload.text(), BanpaiBlockEntity.MAX_LENGTH * 4);
            },
            buf -> new BanpaiTextPayload(buf.readBlockPos(), buf.readUtf(BanpaiBlockEntity.MAX_LENGTH * 4)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 服务端处理：校验距离后写入方块实体。 */
    public static void handle(BanpaiTextPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            BlockPos pos = payload.pos();
            if (!level.isLoaded(pos)) {
                return;
            }
            if (context.player().distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) {
                return;
            }
            if (level.getBlockEntity(pos) instanceof BanpaiBlockEntity banpai) {
                banpai.setText(payload.text());
            }
        });
    }
}
