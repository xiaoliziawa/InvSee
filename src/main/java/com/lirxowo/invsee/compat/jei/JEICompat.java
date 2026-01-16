package com.lirxowo.invsee.compat.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

public class JEICompat {
    private static final String JEI_MODID = "jei";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(JEI_MODID);
    }

    @Nullable
    public static ItemStack getItemUnderMouse() {
        if (!isLoaded()) {
            return null;
        }
        try {
            return JEIRuntimeHelper.getItemUnderMouse();
        } catch (Throwable e) {
            return null;
        }
    }

    public static boolean hasItemUnderMouse() {
        ItemStack stack = getItemUnderMouse();
        return stack != null && !stack.isEmpty();
    }
}
