package com.lirxowo.invsee.compat.mekanism;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class MekanismCompat {
    private static final String MEKANISM_MODID = "mekanism";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MEKANISM_MODID);
    }

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
