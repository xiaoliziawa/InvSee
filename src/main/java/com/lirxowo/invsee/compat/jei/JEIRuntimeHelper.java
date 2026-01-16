package com.lirxowo.invsee.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public class JEIRuntimeHelper {
    @Nullable
    private static IJeiRuntime jeiRuntime = null;

    public static void setRuntime(@Nullable IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    @Nullable
    public static IJeiRuntime getRuntime() {
        return jeiRuntime;
    }

    public static boolean isRuntimeAvailable() {
        return jeiRuntime != null;
    }

    @Nullable
    public static ItemStack getItemUnderMouse() {
        if (jeiRuntime == null) {
            return null;
        }

        ItemStack result;

        result = getItemFromBookmarkOverlay();
        if (result != null && !result.isEmpty()) {
            return result;
        }

        result = getItemFromIngredientListOverlay();
        if (result != null && !result.isEmpty()) {
            return result;
        }

        result = getItemFromRecipesGui();
        if (result != null && !result.isEmpty()) {
            return result;
        }

        return null;
    }

    @Nullable
    private static ItemStack getItemFromBookmarkOverlay() {
        if (jeiRuntime == null) {
            return null;
        }
        try {
            IBookmarkOverlay bookmarkOverlay = jeiRuntime.getBookmarkOverlay();
            return bookmarkOverlay.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
        } catch (Throwable e) {
            return null;
        }
    }

    @Nullable
    private static ItemStack getItemFromIngredientListOverlay() {
        if (jeiRuntime == null) {
            return null;
        }
        try {
            IIngredientListOverlay ingredientListOverlay = jeiRuntime.getIngredientListOverlay();
            return ingredientListOverlay.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
        } catch (Throwable e) {
            return null;
        }
    }

    @Nullable
    private static ItemStack getItemFromRecipesGui() {
        if (jeiRuntime == null) {
            return null;
        }
        try {
            IRecipesGui recipesGui = jeiRuntime.getRecipesGui();
            Optional<ItemStack> result = recipesGui.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
            return result.orElse(null);
        } catch (Throwable e) {
            return null;
        }
    }
}
