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

public class AE2InfoProvider {

    public static List<Component> getStorageCellInfo(ItemStack stack) {
        List<Component> lines = new ArrayList<>();

        if (stack.getItem() instanceof IBasicCellItem cellItem) {
            BasicCellInventory inventory = BasicCellInventory.createInventory(stack, null);
            if (inventory != null) {
                long usedBytes = inventory.getUsedBytes();
                long totalBytes = inventory.getTotalBytes();
                long storedTypes = inventory.getStoredItemTypes();
                long totalTypes = inventory.getTotalItemTypes();
                long storedItemCount = inventory.getStoredItemCount();
                CellState state = inventory.getStatus();

                float bytePercent = totalBytes > 0 ? (float) usedBytes / totalBytes : 0;
                float typePercent = totalTypes > 0 ? (float) storedTypes / totalTypes : 0;

                ChatFormatting stateColor = getStateColor(state);

                String stateText = switch (state) {
                    case ABSENT -> "No Cell";
                    case EMPTY -> "Empty";
                    case NOT_EMPTY -> "In Use";
                    case TYPES_FULL -> "Types Full";
                    case FULL -> "Full";
                };
                lines.add(Component.literal("📦 " + stateText).withStyle(stateColor));

                ChatFormatting byteColor = getPercentColor(bytePercent);
                lines.add(Component.literal("💾 " + ItemInfoHelper.formatNumber((int) usedBytes) + " / "
                        + ItemInfoHelper.formatNumber((int) totalBytes) + " B").withStyle(byteColor));

                ChatFormatting typeColor = getPercentColor(typePercent);
                lines.add(Component.literal("📋 " + storedTypes + " / " + totalTypes + " Types").withStyle(typeColor));

                if (storedItemCount > 0) {
                    lines.add(Component.literal("📊 " + ItemInfoHelper.formatNumber((int) Math.min(storedItemCount, Integer.MAX_VALUE)) + " Items")
                            .withStyle(ChatFormatting.AQUA));
                }

                return lines;
            }
        }

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

            double idleDrain = storageCell.getIdleDrain();
            if (idleDrain > 0) {
                lines.add(Component.literal("⚡ " + String.format("%.1f", idleDrain) + " AE/t")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }

        return lines;
    }

    private static ChatFormatting getStateColor(CellState state) {
        return switch (state) {
            case ABSENT -> ChatFormatting.DARK_GRAY;
            case EMPTY -> ChatFormatting.GRAY;
            case NOT_EMPTY -> ChatFormatting.GREEN;
            case TYPES_FULL -> ChatFormatting.YELLOW;
            case FULL -> ChatFormatting.RED;
        };
    }

    private static ChatFormatting getPercentColor(float percent) {
        if (percent < 0.5f) return ChatFormatting.GREEN;
        if (percent < 0.75f) return ChatFormatting.YELLOW;
        if (percent < 0.9f) return ChatFormatting.GOLD;
        return ChatFormatting.RED;
    }
}
