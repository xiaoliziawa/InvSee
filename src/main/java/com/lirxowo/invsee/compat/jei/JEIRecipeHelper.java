package com.lirxowo.invsee.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeCategoriesLookup;
import mezz.jei.api.recipe.IRecipeLookup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JEIRecipeHelper {

    public record RecipeInfo<T>(IRecipeCategory<T> category, T recipe, RecipeType<T> recipeType) {}

    public static List<RecipeInfo<?>> getRecipesForOutput(ItemStack output) {
        IJeiRuntime runtime = JEIRuntimeHelper.getRuntime();
        if (runtime == null || output.isEmpty()) {
            return List.of();
        }

        List<RecipeInfo<?>> results = new ArrayList<>();
        IRecipeManager recipeManager = runtime.getRecipeManager();
        IJeiHelpers jeiHelpers = runtime.getJeiHelpers();
        IFocusFactory focusFactory = jeiHelpers.getFocusFactory();

        IFocus<ItemStack> outputFocus = focusFactory.createFocus(
                RecipeIngredientRole.OUTPUT,
                VanillaTypes.ITEM_STACK,
                output
        );
        IFocusGroup focusGroup = focusFactory.createFocusGroup(List.of(outputFocus));

        IRecipeCategoriesLookup categoryLookup = recipeManager.createRecipeCategoryLookup()
                .limitFocus(List.of(outputFocus));

        List<IRecipeCategory<?>> categories = categoryLookup.get().collect(Collectors.toList());

        for (IRecipeCategory<?> category : categories) {
            addRecipesFromCategory(recipeManager, category, focusGroup, results);
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private static <T> void addRecipesFromCategory(
            IRecipeManager recipeManager,
            IRecipeCategory<T> category,
            IFocusGroup focusGroup,
            List<RecipeInfo<?>> results
    ) {
        RecipeType<T> recipeType = category.getRecipeType();
        IRecipeLookup<T> recipeLookup = recipeManager.createRecipeLookup(recipeType)
                .limitFocus(focusGroup.getAllFocuses());

        List<T> recipes = recipeLookup.get().collect(Collectors.toList());
        for (T recipe : recipes) {
            results.add(new RecipeInfo<>(category, recipe, recipeType));
        }
    }

    @Nullable
    public static <T> IRecipeLayoutDrawable<T> createRecipeLayout(
            RecipeInfo<T> recipeInfo,
            @Nullable ItemStack focusedItem
    ) {
        IJeiRuntime runtime = JEIRuntimeHelper.getRuntime();
        if (runtime == null) {
            return null;
        }

        IRecipeManager recipeManager = runtime.getRecipeManager();
        IJeiHelpers jeiHelpers = runtime.getJeiHelpers();
        IFocusFactory focusFactory = jeiHelpers.getFocusFactory();

        IFocusGroup focusGroup;
        if (focusedItem != null && !focusedItem.isEmpty()) {
            IFocus<ItemStack> focus = focusFactory.createFocus(
                    RecipeIngredientRole.OUTPUT,
                    VanillaTypes.ITEM_STACK,
                    focusedItem
            );
            focusGroup = focusFactory.createFocusGroup(List.of(focus));
        } else {
            focusGroup = focusFactory.getEmptyFocusGroup();
        }

        Optional<IRecipeLayoutDrawable<T>> layoutOpt = recipeManager.createRecipeLayoutDrawable(
                recipeInfo.category(),
                recipeInfo.recipe(),
                focusGroup
        );

        return layoutOpt.orElse(null);
    }

    public static boolean isAvailable() {
        return JEIRuntimeHelper.isRuntimeAvailable();
    }
}
