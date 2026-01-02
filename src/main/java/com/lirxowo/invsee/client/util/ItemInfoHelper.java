package com.lirxowo.invsee.client.util;

import com.lirxowo.invsee.compat.ae2.AE2Compat;
import com.lirxowo.invsee.compat.mekanism.MekanismCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品信息工具类 - 获取物品的详细信息用于显示
 * 包括：附魔、耐久、稀有度、能量、流体等
 * 支持模组兼容：Mekanism化学品、AE2存储原件
 */
public class ItemInfoHelper {

    /**
     * 获取物品的所有额外信息行
     */
    public static List<Component> getItemInfoLines(ItemStack stack) {
        List<Component> lines = new ArrayList<>();

        if (stack.isEmpty()) {
            return lines;
        }

        // 1. 稀有度（如果不是普通的话）
        Rarity rarity = stack.getRarity();
        if (rarity != Rarity.COMMON) {
            lines.add(getRarityComponent(rarity));
        }

        // 2. 耐久度（如果物品有耐久的话）
        if (stack.isDamageableItem()) {
            int current = stack.getMaxDamage() - stack.getDamageValue();
            int max = stack.getMaxDamage();
            float percent = (float) current / max;
            ChatFormatting color = getDurabilityColor(percent);
            lines.add(Component.translatable("item.durability", current, max).withStyle(color));
        }

        // 3. 附魔信息
        ItemEnchantments enchantments = stack.getEnchantments();
        if (!enchantments.isEmpty()) {
            List<Component> enchantList = new ArrayList<>();
            for (Holder<Enchantment> holder : enchantments.keySet()) {
                int level = enchantments.getLevel(holder);
                if (level > 0) {
                    enchantList.add(Enchantment.getFullname(holder, level));
                }
            }
            // 如果附魔太多，只显示前3个，然后显示 +N more
            if (enchantList.size() <= 3) {
                lines.addAll(enchantList);
            } else {
                for (int i = 0; i < 3; i++) {
                    lines.add(enchantList.get(i));
                }
                lines.add(Component.literal("+" + (enchantList.size() - 3) + " more...").withStyle(ChatFormatting.GRAY));
            }
        }

        // 4. 能量信息（NeoForge Capability）
        IEnergyStorage energyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage != null) {
            int stored = energyStorage.getEnergyStored();
            int max = energyStorage.getMaxEnergyStored();
            float percent = max > 0 ? (float) stored / max : 0;
            ChatFormatting color = getEnergyColor(percent);
            lines.add(Component.literal("⚡ " + formatNumber(stored) + " / " + formatNumber(max) + " FE").withStyle(color));
        }

        // 5. 流体信息（NeoForge Capability）
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            int tanks = fluidHandler.getTanks();
            for (int i = 0; i < tanks; i++) {
                FluidStack fluidStack = fluidHandler.getFluidInTank(i);
                int capacity = fluidHandler.getTankCapacity(i);
                if (capacity > 0) {
                    if (fluidStack.isEmpty()) {
                        lines.add(Component.literal("💧 Empty / " + formatNumber(capacity) + " mB").withStyle(ChatFormatting.GRAY));
                    } else {
                        int amount = fluidStack.getAmount();
                        float percent = (float) amount / capacity;
                        ChatFormatting color = getFluidColor(percent);
                        Component fluidName = fluidStack.getHoverName();
                        lines.add(Component.literal("💧 ").withStyle(color)
                                .append(fluidName)
                                .append(Component.literal(" " + formatNumber(amount) + " / " + formatNumber(capacity) + " mB").withStyle(color)));
                    }
                }
            }
        }

        // 6. Mekanism 化学品信息（如果模组加载）
        if (MekanismCompat.isLoaded()) {
            lines.addAll(MekanismCompat.getChemicalInfo(stack));
        }

        // 7. AE2 存储原件信息（如果模组加载）
        if (AE2Compat.isLoaded()) {
            lines.addAll(AE2Compat.getStorageCellInfo(stack));
        }

        // 8. 数量（如果堆叠数大于1）
        if (stack.getCount() > 1) {
            lines.add(Component.literal("x" + stack.getCount()).withStyle(ChatFormatting.YELLOW));
        }

        return lines;
    }

    /**
     * 获取稀有度显示组件
     */
    private static Component getRarityComponent(Rarity rarity) {
        String name = switch (rarity) {
            case UNCOMMON -> "Uncommon";
            case RARE -> "Rare";
            case EPIC -> "Epic";
            default -> rarity.name();
        };
        return Component.literal("✦ " + name).withStyle(rarity.getStyleModifier());
    }

    /**
     * 根据耐久百分比获取颜色
     */
    private static ChatFormatting getDurabilityColor(float percent) {
        if (percent > 0.75f) return ChatFormatting.GREEN;
        if (percent > 0.5f) return ChatFormatting.YELLOW;
        if (percent > 0.25f) return ChatFormatting.GOLD;
        return ChatFormatting.RED;
    }

    /**
     * 根据能量百分比获取颜色
     */
    private static ChatFormatting getEnergyColor(float percent) {
        if (percent > 0.75f) return ChatFormatting.GREEN;
        if (percent > 0.5f) return ChatFormatting.YELLOW;
        if (percent > 0.25f) return ChatFormatting.GOLD;
        return ChatFormatting.RED;
    }

    /**
     * 根据流体百分比获取颜色
     */
    private static ChatFormatting getFluidColor(float percent) {
        if (percent > 0.75f) return ChatFormatting.AQUA;
        if (percent > 0.5f) return ChatFormatting.BLUE;
        if (percent > 0.25f) return ChatFormatting.DARK_AQUA;
        return ChatFormatting.GRAY;
    }

    /**
     * 格式化大数字（K, M, B）
     */
    public static String formatNumber(int number) {
        if (number < 1000) return String.valueOf(number);
        if (number < 1000000) return String.format("%.1fK", number / 1000.0);
        if (number < 1000000000) return String.format("%.1fM", number / 1000000.0);
        return String.format("%.1fB", number / 1000000000.0);
    }

    /**
     * 格式化大数字（long版本）
     */
    public static String formatNumber(long number) {
        if (number < 1000) return String.valueOf(number);
        if (number < 1000000) return String.format("%.1fK", number / 1000.0);
        if (number < 1000000000) return String.format("%.1fM", number / 1000000.0);
        if (number < 1000000000000L) return String.format("%.1fB", number / 1000000000.0);
        return String.format("%.1fT", number / 1000000000000.0);
    }

    /**
     * 获取物品稀有度对应的颜色
     */
    public static int getRarityColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFFFFFF;   // 白色
            case UNCOMMON -> 0xFFFF55; // 黄色
            case RARE -> 0x55FFFF;     // 青色
            case EPIC -> 0xFF55FF;     // 紫色
        };
    }
}
