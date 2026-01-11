package com.lirxowo.invsee.client.recipe;

import com.lirxowo.invsee.client.util.GuiUtil;
import com.lirxowo.invsee.compat.jei.JEICompat;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;

public class RecipePanelRenderer {

    private static final int PANEL_PADDING = 4;
    private static final int PANEL_GAP = 6;

    private static final int BG_COLOR = 0xCC100010;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private static final int[] RAINBOW_COLORS = {
            0xFFFF0000, // Red
            0xFFFF7F00, // Orange
            0xFFFFFF00, // Yellow
            0xFF00FF00, // Green
            0xFF00FFFF, // Cyan
            0xFF0000FF, // Blue
            0xFF8B00FF
    };

    public static void renderRecipePanelNextToLabel(
            GuiGraphics guiGraphics,
            Minecraft mc,
            float labelX,
            float labelY,
            float labelWidth,
            float labelTop,
            float labelBottom,
            int alpha,
            float animProgress
    ) {
        if (!JEICompat.isLoaded() || !RecipePanelState.isVisible()) {
            return;
        }

        Font font = mc.font;
        IRecipeLayoutDrawable<?> layout = RecipePanelState.getCachedLayout();

        float labelLeft = labelX - labelWidth / 2 - 2;

        if (layout == null) {
            String message = RecipePanelState.hasRecipes() ? "Loading..." : "No recipes";
            renderNoRecipePanel(guiGraphics, font, labelLeft, labelTop, labelBottom, alpha, animProgress, message);
            return;
        }

        renderRecipePanel(guiGraphics, mc, layout, labelLeft, labelTop, labelBottom, alpha, animProgress);
    }

    private static void renderNoRecipePanel(
            GuiGraphics guiGraphics,
            Font font,
            float labelLeft,
            float labelTop,
            float labelBottom,
            int alpha,
            float animProgress,
            String message
    ) {
        int panelWidth = 80;
        float panelHeight = labelBottom - labelTop;

        float targetX = labelLeft - PANEL_GAP - panelWidth;
        float startX = labelLeft - PANEL_GAP;
        float panelX = startX + (targetX - startX) * easeOutCubic(animProgress);

        int adjustedAlpha = (int) (alpha * animProgress);
        int bgAlpha = adjustedAlpha * 64 / 255;
        int bgColor = (bgAlpha << 24) | (BG_COLOR & 0x00FFFFFF);

        GuiUtil.fill(guiGraphics, panelX, labelTop, panelX + panelWidth, labelBottom, bgColor);

        drawRainbowBorder(guiGraphics, panelX, labelTop, panelWidth, panelHeight, adjustedAlpha);

        int textColor = (adjustedAlpha << 24) | (TEXT_COLOR & 0x00FFFFFF);
        float centerX = panelX + panelWidth / 2;
        float centerY = (labelTop + labelBottom) / 2 - 4;
        GuiUtil.drawCenteredString(guiGraphics, font, net.minecraft.network.chat.Component.literal(message),
                centerX, centerY, textColor);
    }

    private static void renderRecipePanel(
            GuiGraphics guiGraphics,
            Minecraft mc,
            IRecipeLayoutDrawable<?> layout,
            float labelLeft,
            float labelTop,
            float labelBottom,
            int alpha,
            float animProgress
    ) {
        Font font = mc.font;

        Rect2i rectWithBorder = layout.getRectWithBorder();
        Rect2i rect = layout.getRect();

        int borderPaddingX = rect.getX() - rectWithBorder.getX();
        int borderPaddingY = rect.getY() - rectWithBorder.getY();

        int layoutWidth = rectWithBorder.getWidth();
        int layoutHeight = rectWithBorder.getHeight();

        int panelWidth = layoutWidth + PANEL_PADDING * 2;
        int panelHeight = layoutHeight + PANEL_PADDING * 2 + 14;

        float targetX = labelLeft - PANEL_GAP - panelWidth;
        float startX = labelLeft - PANEL_GAP;
        float panelX = startX + (targetX - startX) * easeOutCubic(animProgress);

        float labelCenterY = (labelTop + labelBottom) / 2;
        float panelY = labelCenterY - panelHeight / 2.0F;

        int adjustedAlpha = (int) (alpha * animProgress);
        int bgAlpha = adjustedAlpha * 200 / 255;
        int bgColor = (bgAlpha << 24) | (BG_COLOR & 0x00FFFFFF);

        GuiUtil.fill(guiGraphics, panelX, panelY, panelX + panelWidth, panelY + panelHeight, bgColor);

        drawRainbowBorder(guiGraphics, panelX, panelY, panelWidth, panelHeight, adjustedAlpha);

        int layoutX = (int) (panelX + PANEL_PADDING + borderPaddingX);
        int layoutY = (int) (panelY + PANEL_PADDING + borderPaddingY);
        layout.setPosition(layoutX, layoutY);

        guiGraphics.pose().pushPose();
        layout.drawRecipe(guiGraphics, 0, 0);
        guiGraphics.pose().popPose();

        int currentIndex = RecipePanelState.getCurrentRecipeIndex();
        int totalRecipes = RecipePanelState.getTotalRecipes();
        String pageText = (currentIndex + 1) + "/" + totalRecipes;

        int textColor = (adjustedAlpha << 24) | 0xAAAAAA;
        float textX = panelX + panelWidth / 2.0F;
        float textY = panelY + panelHeight - 12;
        GuiUtil.drawCenteredString(guiGraphics, font, net.minecraft.network.chat.Component.literal(pageText),
                textX, textY, textColor);

        if (totalRecipes > 1) {
            String scrollHint = "[Scroll]";
            int hintColor = (adjustedAlpha << 24) | 0x666666;
            GuiUtil.drawCenteredString(guiGraphics, font, net.minecraft.network.chat.Component.literal(scrollHint),
                    textX, textY + 10, hintColor);
        }
    }

    private static void drawRainbowBorder(GuiGraphics guiGraphics, float x, float y, float width, float height, int alpha) {
        float time = (System.currentTimeMillis() % 5000) / 5000.0F;

        float perimeter = 2 * (width + height);

        for (int i = 0; i < (int) width; i++) {
            float progress = i / perimeter + time;
            int color = getRainbowColor(progress, alpha);
            GuiUtil.fill(guiGraphics, x + i, y, x + i + 1, y + 1, color);
        }

        for (int i = 0; i < (int) height; i++) {
            float progress = (width + i) / perimeter + time;
            int color = getRainbowColor(progress, alpha);
            GuiUtil.fill(guiGraphics, x + width - 1, y + i, x + width, y + i + 1, color);
        }

        for (int i = 0; i < (int) width; i++) {
            float progress = (width + height + i) / perimeter + time;
            int color = getRainbowColor(progress, alpha);
            GuiUtil.fill(guiGraphics, x + width - 1 - i, y + height - 1, x + width - i, y + height, color);
        }

        for (int i = 0; i < (int) height; i++) {
            float progress = (2 * width + height + i) / perimeter + time;
            int color = getRainbowColor(progress, alpha);
            GuiUtil.fill(guiGraphics, x, y + height - 1 - i, x + 1, y + height - i, color);
        }
    }

    private static int getRainbowColor(float progress, int alpha) {
        progress = progress - (float) Math.floor(progress);

        float scaledProgress = progress * RAINBOW_COLORS.length;
        int index1 = (int) scaledProgress % RAINBOW_COLORS.length;
        int index2 = (index1 + 1) % RAINBOW_COLORS.length;
        float blend = scaledProgress - (int) scaledProgress;

        int color1 = RAINBOW_COLORS[index1];
        int color2 = RAINBOW_COLORS[index2];

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) Mth.lerp(blend, r1, r2);
        int g = (int) Mth.lerp(blend, g1, g2);
        int b = (int) Mth.lerp(blend, b1, b2);

        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    private static float easeOutCubic(float x) {
        return 1 - (float) Math.pow(1 - x, 3);
    }
}
