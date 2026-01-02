package com.lirxowo.invsee.compat.ae2;

import com.lirxowo.invsee.client.util.ItemInfoHelper;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.StorageCell;
import appeng.api.storage.cells.CellState;
import appeng.me.cells.BasicCellInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * AE2 信息提供者 - 实际获取存储原件数据
 * 这个类只有在 AE2 加载时才会被调用
 */
public class AE2InfoProvider {

    /**
     * 获取存储原件信息
     */
    public static List<Component> getStorageCellInfo(ItemStack stack) {
        List<Component> lines = new ArrayList<>();

        // 检查是否是基础存储原件
        if (stack.getItem() instanceof IBasicCellItem cellItem) {
            // 创建原件库存
            BasicCellInventory inventory = BasicCellInventory.createInventory(stack, null);
            if (inventory != null) {
                // 获取存储信息
                long usedBytes = inventory.getUsedBytes();
                long totalBytes = inventory.getTotalBytes();
                long storedTypes = inventory.getStoredItemTypes();
                long totalTypes = inventory.getTotalItemTypes();
                long storedItemCount = inventory.getStoredItemCount();
                CellState state = inventory.getStatus();

                // 计算百分比
                float bytePercent = totalBytes > 0 ? (float) usedBytes / totalBytes : 0;
                float typePercent = totalTypes > 0 ? (float) storedTypes / totalTypes : 0;

                // 状态颜色
                ChatFormatting stateColor = getStateColor(state);

                // 存储状态
                String stateText = switch (state) {
                    case ABSENT -> "No Cell";
                    case EMPTY -> "Empty";
                    case NOT_EMPTY -> "In Use";
                    case TYPES_FULL -> "Types Full";
                    case FULL -> "Full";
                };
                lines.add(Component.literal("📦 " + stateText).withStyle(stateColor));

                // 字节使用情况
                ChatFormatting byteColor = getPercentColor(bytePercent);
                lines.add(Component.literal("💾 " + ItemInfoHelper.formatNumber((int) usedBytes) + " / "
                        + ItemInfoHelper.formatNumber((int) totalBytes) + " B").withStyle(byteColor));

                // 类型使用情况
                ChatFormatting typeColor = getPercentColor(typePercent);
                lines.add(Component.literal("📋 " + storedTypes + " / " + totalTypes + " Types").withStyle(typeColor));

                // 存储物品总数
                if (storedItemCount > 0) {
                    lines.add(Component.literal("📊 " + ItemInfoHelper.formatNumber((int) Math.min(storedItemCount, Integer.MAX_VALUE)) + " Items")
                            .withStyle(ChatFormatting.AQUA));
                }

                return lines;
            }
        }

        // 尝试通用的存储原件处理
        StorageCell storageCell = StorageCells.getCellInventory(stack, null);
        if (storageCell != null) {
            CellState state = storageCell.getStatus();
            ChatFormatting stateColor = getStateColor(state);

            String stateText = switch (state) {
                case ABSENT -> "No Cell";
                case EMPTY -> "Empty";
                case NOT_EMPTY -> "In Use";
                case TYPES_FULL -> "Types Full";
                case FULL -> "Full";
            };
            lines.add(Component.literal("📦 " + stateText).withStyle(stateColor));

            // 空闲耗电
            double idleDrain = storageCell.getIdleDrain();
            if (idleDrain > 0) {
                lines.add(Component.literal("⚡ " + String.format("%.1f", idleDrain) + " AE/t")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }

        return lines;
    }

    /**
     * 根据状态获取颜色
     */
    private static ChatFormatting getStateColor(CellState state) {
        return switch (state) {
            case ABSENT -> ChatFormatting.DARK_GRAY;
            case EMPTY -> ChatFormatting.GRAY;
            case NOT_EMPTY -> ChatFormatting.GREEN;
            case TYPES_FULL -> ChatFormatting.YELLOW;
            case FULL -> ChatFormatting.RED;
        };
    }

    /**
     * 根据百分比获取颜色
     */
    private static ChatFormatting getPercentColor(float percent) {
        if (percent < 0.5f) return ChatFormatting.GREEN;
        if (percent < 0.75f) return ChatFormatting.YELLOW;
        if (percent < 0.9f) return ChatFormatting.GOLD;
        return ChatFormatting.RED;
    }
}
