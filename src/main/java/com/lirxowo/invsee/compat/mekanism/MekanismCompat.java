package com.lirxowo.invsee.compat.mekanism;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Mekanism 模组兼容 - 获取化学品信息
 */
public class MekanismCompat {
    private static final String MEKANISM_MODID = "mekanism";

    /**
     * 检查 Mekanism 是否已加载
     */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(MEKANISM_MODID);
    }

    /**
     * 获取物品的化学品信息
     */
    public static List<Component> getChemicalInfo(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        if (!isLoaded() || stack.isEmpty()) {
            return lines;
        }

        try {
            return MekanismInfoProvider.getChemicalInfo(stack);
        } catch (Throwable e) {
            return lines;
        }
    }
}
