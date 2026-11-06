package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.menu.JiangtaiMenu;
import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 讲台的方块实体：27 格容器（与原版箱子一致）。
 */
public class JiangtaiBlockEntity extends BlockEntity implements MenuProvider {

    /** 容器槽位数量（3 行 9 列）。 */
    public static final int CONTAINER_SIZE = 27;

    private static final Codec<List<ItemStack>> ITEMS_CODEC = ItemStack.OPTIONAL_CODEC.listOf();

    private final SimpleContainer container = new SimpleContainer(CONTAINER_SIZE) {
        @Override
        public void setChanged() {
            JiangtaiBlockEntity.this.setChanged();
        }
    };

    public JiangtaiBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JIANGTAI.get(), pos, state);
    }

    public SimpleContainer getContainer() {
        return this.container;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.chinese_school.jiangtai");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new JiangtaiMenu(containerId, playerInventory, this.container, this.worldPosition);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        List<ItemStack> items = new ArrayList<>(CONTAINER_SIZE);
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            items.add(this.container.getItem(i));
        }
        output.store("Items", ITEMS_CODEC, items);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        List<ItemStack> items = input.read("Items", ITEMS_CODEC).orElse(List.of());
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            this.container.setItem(i, i < items.size() ? items.get(i) : ItemStack.EMPTY);
        }
    }

    /** 把容器内容全部掉落出来（破坏方块时调用）。 */
    public void dropContents() {
        if (this.level == null) {
            return;
        }
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = this.container.getItem(i);
            if (!stack.isEmpty()) {
                Block.popResource(this.level, this.worldPosition, stack);
                this.container.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
