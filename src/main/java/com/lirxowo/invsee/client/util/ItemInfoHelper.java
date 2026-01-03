package com.lirxowo.invsee.client.util;

import com.lirxowo.invsee.api.InvseeAPIHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class ItemInfoHelper {

    public static List<Component> getItemInfoLines(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ArrayList<>();
        }
        return InvseeAPIHandler.collectItemInfo(stack);
    }

    public static List<Component> getItemTooltipLines(ItemStack stack) {
        List<Component> tooltipLines = new ArrayList<>();

        if (stack.isEmpty()) {
            return tooltipLines;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return tooltipLines;
        }

        try {
            TooltipFlag flag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
            Item.TooltipContext context = Item.TooltipContext.of(mc.level);
            List<Component> fullTooltip = stack.getTooltipLines(context, mc.player, flag);

            if (fullTooltip.size() > 1) {
                for (int i = 1; i < fullTooltip.size(); i++) {
                    Component line = fullTooltip.get(i);
                    String text = line.getString();
                    if (text.isEmpty()) continue;
                    tooltipLines.add(line);
                }
            }
        } catch (Exception ignored) {
        }

        return tooltipLines;
    }

    public static String formatNumber(int number) {
        if (number < 1000) return String.valueOf(number);
        if (number < 1000000) return String.format("%.1fK", number / 1000.0);
        if (number < 1000000000) return String.format("%.1fM", number / 1000000.0);
        return String.format("%.1fB", number / 1000000000.0);
    }

    public static String formatNumber(long number) {
        if (number < 1000) return String.valueOf(number);
        if (number < 1000000) return String.format("%.1fK", number / 1000.0);
        if (number < 1000000000) return String.format("%.1fM", number / 1000000.0);
        if (number < 1000000000000L) return String.format("%.1fB", number / 1000000000.0);
        return String.format("%.1fT", number / 1000000000000.0);
    }

    public static int getRarityColor(Rarity rarity) {
        // Handle vanilla rarities
        if (rarity == Rarity.COMMON) return 0xFFFFFF;
        if (rarity == Rarity.UNCOMMON) return 0xFFFF55;
        if (rarity == Rarity.RARE) return 0x55FFFF;
        if (rarity == Rarity.EPIC) return 0xFF55FF;

        // Handle custom rarities from mods (e.g., AvaritiaNeo's COSMIC)
        // Why do you register a rarity yourself?
        // Try to get color from the rarity's style modifier
        try {
            ChatFormatting formatting = rarity.getStyleModifier().apply(Style.EMPTY).getColor() != null
                ? null : ChatFormatting.WHITE;
            var style = rarity.getStyleModifier().apply(Style.EMPTY);
            var color = style.getColor();
            if (color != null) {
                return color.getValue();
            }
        } catch (Exception ignored) {
        }

        return 0xFFFFFF;
    }

    public static ChatFormatting getPercentColor(float percent) {
        if (percent > 0.75f) return ChatFormatting.GREEN;
        if (percent > 0.5f) return ChatFormatting.YELLOW;
        if (percent > 0.25f) return ChatFormatting.GOLD;
        return ChatFormatting.RED;
    }
}
