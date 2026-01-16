package com.lirxowo.invsee.compat.mekanism;

import com.lirxowo.invsee.client.util.ItemInfoHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;

public class MekanismInfoProvider {

    public static List<Component> getChemicalInfo(ItemStack stack) {
        List<Component> lines = new ArrayList<>();

        LazyOptional<IGasHandler> gasCapability = stack.getCapability(Capabilities.GAS_HANDLER);
        gasCapability.ifPresent(handler -> {
            int tanks = handler.getTanks();
            for (int i = 0; i < tanks; i++) {
                GasStack gasStack = handler.getChemicalInTank(i);
                long capacity = handler.getTankCapacity(i);

                if (capacity > 0) {
                    if (gasStack.isEmpty()) {
                        lines.add(Component.literal("⚗ Empty / " +
                                ItemInfoHelper.formatNumber(capacity) + " mB")
                                .withStyle(ChatFormatting.GRAY));
                    } else {
                        long amount = gasStack.getAmount();
                        float percent = (float) amount / capacity;
                        ChatFormatting color = getChemicalColor(percent);

                        Component gasName = gasStack.getTextComponent();

                        lines.add(Component.literal("⚗ ").withStyle(color)
                                .append(gasName)
                                .append(Component.literal(" " +
                                        ItemInfoHelper.formatNumber(amount) + " / " +
                                        ItemInfoHelper.formatNumber(capacity) + " mB")
                                        .withStyle(color)));
                    }
                }
            }
        });

        return lines;
    }

    private static ChatFormatting getChemicalColor(float percent) {
        return ItemInfoHelper.getPercentColor(percent);
    }
}
