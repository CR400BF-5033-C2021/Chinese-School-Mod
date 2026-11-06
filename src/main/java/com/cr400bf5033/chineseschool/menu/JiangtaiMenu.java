package com.cr400bf5033.chineseschool.menu;

import com.cr400bf5033.chineseschool.block.JiangtaiBlockEntity;
import com.cr400bf5033.chineseschool.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 讲台容器菜单：3 行 9 列共 27 格，布局与原版箱子一致。
 */
public class JiangtaiMenu extends AbstractContainerMenu {

    public static final int CONTAINER_SIZE = 27;

    private static final int ROWS = 3;
    private static final int SLOT_START_X = 8;
    private static final int SLOT_START_Y = 18;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final Container container;
    private final BlockPos pos;

    /** 客户端创建菜单时使用（额外数据中带有方块坐标）。 */
    public JiangtaiMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    private JiangtaiMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, findContainer(playerInventory, pos), pos);
    }

    /** 服务端由方块实体创建菜单时使用。 */
    public JiangtaiMenu(int containerId, Inventory playerInventory, Container container, BlockPos pos) {
        super(ModMenus.JIANGTAI.get(), containerId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        this.pos = pos;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(container, col + row * 9,
                        SLOT_START_X + col * 18, SLOT_START_Y + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        INVENTORY_X + col * 18, INVENTORY_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, INVENTORY_X + col * 18, HOTBAR_Y));
        }
    }

    private static Container findContainer(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof JiangtaiBlockEntity jiangtai) {
            return jiangtai.getContainer();
        }
        return new SimpleContainer(CONTAINER_SIZE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();

        if (index < CONTAINER_SIZE) {
            if (!this.moveItemStackTo(stack, CONTAINER_SIZE, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, CONTAINER_SIZE, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player)
                && player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
    }
}
