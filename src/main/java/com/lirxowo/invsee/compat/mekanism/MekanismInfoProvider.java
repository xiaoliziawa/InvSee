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

public class MekanismInfoProvider {

    public static List<Component> getChemicalInfo(ItemStack stack) {
        List<Component> lines = new ArrayList<>();

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

    private static ChatFormatting getChemicalColor(float percent) {
        if (percent > 0.75f) return ChatFormatting.GREEN;
        if (percent > 0.5f) return ChatFormatting.YELLOW;
        if (percent > 0.25f) return ChatFormatting.GOLD;
        return ChatFormatting.RED;
    }
}
