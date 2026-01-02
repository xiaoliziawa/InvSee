package com.lirxowo.invsee.compat.mekanism;

import com.lirxowo.invsee.client.util.ItemInfoHelper;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Mekanism 信息提供者 - 实际获取化学品数据
 * 这个类只有在 Mekanism 加载时才会被调用
 */
public class MekanismInfoProvider {

    /**
     * 获取物品的化学品信息
     */
    public static List<Component> getChemicalInfo(ItemStack stack) {
        List<Component> lines = new ArrayList<>();

        // 获取化学品处理器 Capability
        IChemicalHandler chemicalHandler = stack.getCapability(Capabilities.CHEMICAL.item());
        if (chemicalHandler == null) {
            return lines;
        }

        int tanks = chemicalHandler.getChemicalTanks();
        for (int i = 0; i < tanks; i++) {
            ChemicalStack chemicalStack = chemicalHandler.getChemicalInTank(i);
            long capacity = chemicalHandler.getChemicalTankCapacity(i);

            if (capacity > 0) {
                if (chemicalStack.isEmpty()) {
                    lines.add(Component.literal("⚗ Empty / " + ItemInfoHelper.formatNumber((int) Math.min(capacity, Integer.MAX_VALUE)) + " mB")
                            .withStyle(ChatFormatting.GRAY));
                } else {
                    long amount = chemicalStack.getAmount();
                    float percent = (float) amount / capacity;
                    ChatFormatting color = getChemicalColor(percent);

                    // 获取化学品名称
                    Component chemicalName = chemicalStack.getTextComponent();

                    lines.add(Component.literal("⚗ ").withStyle(color)
                            .append(chemicalName)
                            .append(Component.literal(" " + ItemInfoHelper.formatNumber((int) Math.min(amount, Integer.MAX_VALUE))
                                    + " / " + ItemInfoHelper.formatNumber((int) Math.min(capacity, Integer.MAX_VALUE)) + " mB").withStyle(color)));
                }
            }
        }

        return lines;
    }

    /**
     * 根据化学品百分比获取颜色
     */
    private static ChatFormatting getChemicalColor(float percent) {
        if (percent > 0.75f) return ChatFormatting.GREEN;
        if (percent > 0.5f) return ChatFormatting.YELLOW;
        if (percent > 0.25f) return ChatFormatting.GOLD;
        return ChatFormatting.RED;
    }
}
