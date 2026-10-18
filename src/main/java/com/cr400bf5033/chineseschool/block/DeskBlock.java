package com.cr400bf5033.chineseschool.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 课桌。
 *
 * <p>拥有东、南、西、北四个朝向（{@link #FACING}），放置时正面朝向玩家；
 * 右键可以打开 9 格容器的界面。潜行右键可以把书放到桌面上、或把书取回。</p>
 */
public class DeskBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public DeskBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(BlockStateProperties.HAS_BOOK, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.HAS_BOOK);
    }

    /** 放置时正面朝向玩家（与熔炉、箱子一致）。 */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.HAS_BOOK, Boolean.FALSE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeskBlockEntity(pos, state);
    }

    /** 潜行右键：桌上没有书且手里是书 -> 把书放上去。 */
    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit) {
        boolean hasBook = state.getValue(BlockStateProperties.HAS_BOOK);

        // 潜行 + 手持书 + 桌上没书：把书放到桌面上
        if (player.isShiftKeyDown() && !hasBook && isBook(stack)) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (level.getBlockEntity(pos) instanceof DeskBlockEntity desk) {
                ItemStack placed = stack.copy();
                placed.setCount(1);
                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
                desk.setBook(placed);
                level.setBlock(pos, state.setValue(BlockStateProperties.HAS_BOOK, Boolean.TRUE), Block.UPDATE_ALL);
            }
            return InteractionResult.CONSUME;
        }

        // 手持方块时不抢交互，否则玩家没法贴着课桌放方块
        if (stack.getItem() instanceof BlockItem) {
            return InteractionResult.PASS;
        }

        // 其余情况交给 useWithoutItem 处理（开界面 / 取书）。
        // 必须返回 TRY_WITH_EMPTY_HAND：返回 PASS 时原版不会再调用 useWithoutItem。
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        boolean hasBook = state.getValue(BlockStateProperties.HAS_BOOK);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof DeskBlockEntity desk)) {
            return InteractionResult.CONSUME;
        }

        // 潜行右键：把桌面上的书取回
        if (player.isShiftKeyDown() && hasBook) {
            ItemStack book = desk.takeBook();
            if (!book.isEmpty() && !player.getInventory().add(book)) {
                Block.popResource(level, pos, book);
            }
            return InteractionResult.CONSUME;
        }

        // 桌上有书：像讲台一样打开书本界面（阅读 / 翻页 / 取书）
        if (hasBook) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, p) -> new LecternMenu(containerId, desk.getBookContainer(), desk.getPageData()),
                    desk.getDisplayName()));
            return InteractionResult.CONSUME;
        }

        // 普通右键：打开 5 格容器界面
        player.openMenu(desk, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // 方块被破坏时把容器里的物品和桌面上的书都掉出来，避免物品丢失
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DeskBlockEntity desk) {
            desk.dropContents();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** 与讲台一致：只有书与笔、成书可以放置。 */
    public static boolean isBook(ItemStack stack) {
        return stack.getItem() instanceof WrittenBookItem || stack.getItem() instanceof WritableBookItem;
    }
}
