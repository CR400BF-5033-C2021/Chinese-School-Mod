package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 班牌的方块实体：保存班级名称，并同步给客户端用于渲染。
 */
public class BanpaiBlockEntity extends BlockEntity {

    /** 名称最大长度。 */
    public static final int MAX_LENGTH = 12;

    private String text = "";

    public BanpaiBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANPAI.get(), pos, state);
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        String trimmed = text == null ? "" : text.strip();
        if (trimmed.length() > MAX_LENGTH) {
            trimmed = trimmed.substring(0, MAX_LENGTH);
        }
        this.text = trimmed;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Text", this.text);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.text = input.getStringOr("Text", "");
    }

    /** 让客户端在区块加载/更新时拿到文字。 */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Text", this.text);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
