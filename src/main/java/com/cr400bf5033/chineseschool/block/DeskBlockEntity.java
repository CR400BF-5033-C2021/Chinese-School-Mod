package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.menu.DeskMenu;
import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 课桌的方块实体。
 *
 * <p>保存 5 格物品栏、桌面上摆着的书以及书的页码；桌上是否有书由方块状态
 * {@code has_book} 表示，供模型切换使用。</p>
 */
public class DeskBlockEntity extends BlockEntity implements MenuProvider {

    /** 物品栏槽位数量。 */
    public static final int CONTAINER_SIZE = 5;

    private static final Codec<List<ItemStack>> ITEMS_CODEC = ItemStack.OPTIONAL_CODEC.listOf();

    /** 物品栏；内容变化时把方块实体标记为需要保存。 */
    private final SimpleContainer container = new SimpleContainer(CONTAINER_SIZE) {
        @Override
        public void setChanged() {
            DeskBlockEntity.this.setChanged();
        }
    };

    /** 桌面上的书（1 格容器，直接复用讲台的菜单来阅读）。 */
    private final SimpleContainer bookContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            DeskBlockEntity.this.onBookChanged();
        }
    };

    private int page;

    private final ContainerData pageData = new ContainerData() {
        @Override
        public int get(int index) {
            return DeskBlockEntity.this.page;
        }

        @Override
        public void set(int index, int value) {
            if (DeskBlockEntity.this.page != value) {
                DeskBlockEntity.this.page = value;
                // 原版讲台界面关掉了翻页音效（BookViewScreen 无参构造器），这里在服务端补上
                DeskBlockEntity.this.playPageTurnSound();
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    private void playPageTurnSound() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.playSound(null, this.worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public DeskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DESK.get(), pos, state);
    }

    public Container getContainer() {
        return this.container;
    }

    public Container getBookContainer() {
        return this.bookContainer;
    }

    public ContainerData getPageData() {
        return this.pageData;
    }

    public ItemStack getBook() {
        return this.bookContainer.getItem(0);
    }

    public void setBook(ItemStack book) {
        this.bookContainer.setItem(0, book);
    }

    /** 取走桌面上的书，返回取到的物品（可能为空）。 */
    public ItemStack takeBook() {
        ItemStack book = this.bookContainer.getItem(0).copy();
        this.bookContainer.setItem(0, ItemStack.EMPTY);
        return book;
    }

    /** 书被放上/取下时同步 {@code has_book} 方块状态，让模型跟着切换。 */
    private void onBookChanged() {
        this.setChanged();
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        BlockState state = this.getBlockState();
        boolean hasBook = !this.bookContainer.getItem(0).isEmpty();
        if (state.getBlock() instanceof DeskBlock && state.getValue(BlockStateProperties.HAS_BOOK) != hasBook) {
            this.level.setBlock(this.worldPosition, state.setValue(BlockStateProperties.HAS_BOOK, hasBook), Block.UPDATE_ALL);
        }
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
        output.store("Book", ItemStack.OPTIONAL_CODEC, this.bookContainer.getItem(0));
        output.store("Page", Codec.INT, this.page);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        List<ItemStack> items = input.read("Items", ITEMS_CODEC).orElse(List.of());
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            this.container.setItem(i, i < items.size() ? items.get(i) : ItemStack.EMPTY);
        }
        this.bookContainer.setItem(0, input.read("Book", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        this.page = input.read("Page", Codec.INT).orElse(0);
    }

    /** 把物品栏和桌面上的书全部掉落出来（破坏方块时调用）。 */
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
        ItemStack book = this.bookContainer.getItem(0);
        if (!book.isEmpty()) {
            Block.popResource(this.level, this.worldPosition, book);
            this.bookContainer.setItem(0, ItemStack.EMPTY);
        }
    }
}
