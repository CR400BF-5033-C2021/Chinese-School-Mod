package com.cr400bf5033.chineseschool.menu;

import com.cr400bf5033.chineseschool.block.DeskBlock;
import com.cr400bf5033.chineseschool.block.DeskBlockEntity;
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
 * 课桌的容器菜单：上方 3x3 为课桌的 9 个槽位，下方是玩家背包与快捷栏。
 */
public class DeskMenu extends AbstractContainerMenu {

    public static final int CONTAINER_SIZE = 9;

    /** 3x3 槽位左上角在 GUI 中的坐标。 */
    private static final int SLOT_X = 62;
    private static final int SLOT_Y = 18;

    /** 玩家背包左上角坐标。 */
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;

    private final Container container;
    private final BlockPos pos;

    /** 客户端创建菜单时使用（额外数据中带有方块坐标）。 */
    public DeskMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    private DeskMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, findContainer(playerInventory, pos), pos);
    }

    /** 服务端由方块实体创建菜单时使用。 */
    public DeskMenu(int containerId, Inventory playerInventory, Container container, BlockPos pos) {
        super(ModMenus.DESK.get(), containerId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        this.pos = pos;

        // 课桌的 3x3 槽位
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(container, col + row * 3, SLOT_X + col * 18, SLOT_Y + row * 18));
            }
        }

        // 玩家背包 9x3
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, INVENTORY_X + col * 18, INVENTORY_Y + row * 18));
            }
        }

        // 快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, INVENTORY_X + col * 18, 142));
        }
    }

    private static Container findContainer(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof DeskBlockEntity desk) {
            return desk.getContainer();
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
            // 课桌 -> 玩家背包
            if (!this.moveItemStackTo(stack, CONTAINER_SIZE, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, CONTAINER_SIZE, false)) {
            // 玩家背包 -> 课桌
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
        return player.level().getBlockState(this.pos).getBlock() instanceof DeskBlock
                && player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
    }
}
