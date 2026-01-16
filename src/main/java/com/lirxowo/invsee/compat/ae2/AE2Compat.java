package com.lirxowo.invsee.compat.ae2;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class AE2Compat {
    private static final String AE2_MODID = "ae2";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(AE2_MODID);
    }

    public static List<Component> getStorageCellInfo(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        if (!isLoaded() || stack.isEmpty()) {
            return lines;
        }

        try {
            return AE2InfoProvider.getStorageCellInfo(stack);
        } catch (Throwable e) {
            return lines;
        }
    }
}
