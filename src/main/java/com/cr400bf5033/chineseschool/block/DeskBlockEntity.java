package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.menu.DeskMenu;
import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
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
 * 课桌的方块实体，负责保存 9 格物品栏、桌面上的书，并提供容器界面。
 */
public class DeskBlockEntity extends BlockEntity implements MenuProvider {

    /** 物品栏槽位数量。 */
    public static final int CONTAINER_SIZE = 9;

    private static final Codec<List<ItemStack>> ITEMS_CODEC = ItemStack.OPTIONAL_CODEC.listOf();

    /** 物品栏；内容变化时把方块实体标记为需要保存。 */
    private final SimpleContainer container = new SimpleContainer(CONTAINER_SIZE) {
        @Override
        public void setChanged() {
            DeskBlockEntity.this.setChanged();
        }
    };

    /** 摆在桌面上的书；显示与否由方块状态 has_book 决定。 */
    private ItemStack book = ItemStack.EMPTY;

    public DeskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DESK.get(), pos, state);
    }

    public Container getContainer() {
        return this.container;
    }

    public ItemStack getBook() {
        return this.book;
    }

    public void setBook(ItemStack book) {
        this.book = book;
        this.setChanged();
    }

    /** 取走桌面上的书，返回取到的物品（可能为空）。 */
    public ItemStack takeBook() {
        ItemStack result = this.book;
        this.book = ItemStack.EMPTY;
        this.setChanged();
        return result;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.chinese_school.desk");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DeskMenu(containerId, playerInventory, this.container, this.worldPosition);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        List<ItemStack> items = new ArrayList<>(CONTAINER_SIZE);
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            items.add(this.container.getItem(i));
        }
        output.store("Items", ITEMS_CODEC, items);
        output.store("Book", ItemStack.OPTIONAL_CODEC, this.book);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        List<ItemStack> items = input.read("Items", ITEMS_CODEC).orElse(List.of());
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            this.container.setItem(i, i < items.size() ? items.get(i) : ItemStack.EMPTY);
        }
        this.book = input.read("Book", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    /** 把容器内的物品和桌面上的书全部掉落出来（破坏方块时调用）。 */
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
        if (!this.book.isEmpty()) {
            Block.popResource(this.level, this.worldPosition, this.book);
            this.book = ItemStack.EMPTY;
        }
    }
}
