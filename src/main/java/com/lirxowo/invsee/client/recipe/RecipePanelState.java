package com.lirxowo.invsee.client.recipe;

import com.lirxowo.invsee.compat.jei.JEICompat;
import com.lirxowo.invsee.compat.jei.JEIRecipeHelper;
import com.lirxowo.invsee.entity.ItemMarkEntity;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class RecipePanelState {
    @Nullable
    private static UUID expandedEntityId = null;

    private static int currentRecipeIndex = 0;

    private static List<JEIRecipeHelper.RecipeInfo<?>> cachedRecipes = List.of();

    @Nullable
    private static IRecipeLayoutDrawable<?> cachedLayout = null;

    private static float animationProgress = 0.0f;

    private static final float ANIMATION_SPEED = 0.15f;

    private static boolean targetOpen = false;

    private static ItemStack displayedItem = ItemStack.EMPTY;

    public static void togglePanel(ItemMarkEntity entity) {
        if (!JEICompat.isLoaded()) {
            return;
        }

        UUID entityId = entity.getUUID();

        if (entityId.equals(expandedEntityId) && targetOpen) {
            targetOpen = false;
        } else {
            expandedEntityId = entityId;
            targetOpen = true;
            currentRecipeIndex = 0;
            displayedItem = entity.getMarkedItem().copy();

            refreshRecipes();
        }
    }

    public static void closePanel() {
        targetOpen = false;
    }

    private static void refreshRecipes() {
        if (displayedItem.isEmpty() || !JEICompat.isLoaded()) {
            cachedRecipes = List.of();
            cachedLayout = null;
            return;
        }

        try {
            cachedRecipes = JEIRecipeHelper.getRecipesForOutput(displayedItem);
            updateCachedLayout();
        } catch (Throwable e) {
            cachedRecipes = List.of();
            cachedLayout = null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void updateCachedLayout() {
        if (cachedRecipes.isEmpty() || currentRecipeIndex >= cachedRecipes.size()) {
            cachedLayout = null;
            return;
        }

        try {
            JEIRecipeHelper.RecipeInfo recipeInfo = cachedRecipes.get(currentRecipeIndex);
            cachedLayout = JEIRecipeHelper.createRecipeLayout(recipeInfo, displayedItem);
        } catch (Throwable e) {
            cachedLayout = null;
        }
    }

    public static void nextRecipe() {
        if (cachedRecipes.isEmpty()) {
            return;
        }
        currentRecipeIndex = (currentRecipeIndex + 1) % cachedRecipes.size();
        updateCachedLayout();
    }

    public static void previousRecipe() {
        if (cachedRecipes.isEmpty()) {
            return;
        }
        currentRecipeIndex = (currentRecipeIndex - 1 + cachedRecipes.size()) % cachedRecipes.size();
        updateCachedLayout();
    }

    public static void tick() {
        if (targetOpen) {
            animationProgress = Math.min(1.0f, animationProgress + ANIMATION_SPEED);
        } else {
            animationProgress = Math.max(0.0f, animationProgress - ANIMATION_SPEED);

            if (animationProgress <= 0.0f) {
                expandedEntityId = null;
                cachedRecipes = List.of();
                cachedLayout = null;
                displayedItem = ItemStack.EMPTY;
            }
        }

        if (cachedLayout != null) {
            cachedLayout.tick();
        }
    }

    public static boolean isVisible() {
        return animationProgress > 0.0f;
    }

    public static boolean isFullyOpen() {
        return animationProgress >= 1.0f;
    }

    public static float getAnimationProgress() {
        return animationProgress;
    }

    public static float getSmoothedProgress(float partialTick) {
        float target = targetOpen ? 1.0f : 0.0f;
        float current = animationProgress;
        float next = targetOpen
                ? Math.min(1.0f, current + ANIMATION_SPEED)
                : Math.max(0.0f, current - ANIMATION_SPEED);
        return current + (next - current) * partialTick;
    }

    public static boolean isExpandedFor(ItemMarkEntity entity) {
        return entity.getUUID().equals(expandedEntityId) && isVisible();
    }

    public static boolean isTargetEntity(ItemMarkEntity entity) {
        return entity.getUUID().equals(expandedEntityId);
    }

    @Nullable
    public static UUID getExpandedEntityId() {
        return expandedEntityId;
    }

    @Nullable
    public static IRecipeLayoutDrawable<?> getCachedLayout() {
        return cachedLayout;
    }

    public static int getCurrentRecipeIndex() {
        return currentRecipeIndex;
    }

    public static int getTotalRecipes() {
        return cachedRecipes.size();
    }

    public static boolean hasRecipes() {
        return !cachedRecipes.isEmpty();
    }

    public static ItemStack getDisplayedItem() {
        return displayedItem;
    }
}
