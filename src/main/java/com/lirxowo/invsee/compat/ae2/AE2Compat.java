package com.lirxowo.invsee.compat.ae2;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Applied Energistics 2 模组兼容 - 获取存储原件信息
 */
public class AE2Compat {
    private static final String AE2_MODID = "ae2";

    /**
     * 检查 AE2 是否已加载
     */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(AE2_MODID);
    }

    /**
     * 获取存储原件信息
     */
    public static List<Component> getStorageCellInfo(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        if (!isLoaded() || stack.isEmpty()) {
            return lines;
        }

        try {
            return AE2InfoProvider.getStorageCellInfo(stack);
        } catch (Throwable e) {
            // 如果出错，返回空列表
            return lines;
        }
    }
}
