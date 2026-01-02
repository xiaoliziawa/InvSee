package com.lirxowo.invsee.client.tracking;

import com.lirxowo.invsee.config.InvseeConfig;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class TrackingList {
    private static ItemStack trackedStack = ItemStack.EMPTY;
    private static Color highlightColor = new Color(255, 228, 32, 255);
    private static long trackStartTime = 0;

    public static boolean beingTracked(ItemStack stack) {
        if (trackedStack.isEmpty()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(stack, trackedStack);
    }

    public static void startTracking(ItemStack stack, long gameTime) {
        trackedStack = stack.copyWithCount(1);
        trackStartTime = gameTime;
    }

    public static void clear() {
        trackedStack = ItemStack.EMPTY;
        trackStartTime = 0;
    }

    public static void tickTracking(long currentGameTime) {
        if (!trackedStack.isEmpty() && (currentGameTime - trackStartTime) > InvseeConfig.getHighlightDurationTicks()) {
            clear();
        }
    }

    public static ItemStack getTrackedStack() {
        return trackedStack.copy();
    }

    public static boolean isTracking() {
        return !trackedStack.isEmpty();
    }

    public static Color getHighlightColor() {
        return highlightColor;
    }

    public static void setHighlightColor(Color color) {
        highlightColor = color;
    }

    public static float getTrackProgress(long currentGameTime) {
        if (trackedStack.isEmpty()) {
            return 0f;
        }
        float progress = (float)(currentGameTime - trackStartTime) / InvseeConfig.getHighlightDurationTicks();
        return Math.min(1.0f, Math.max(0.0f, progress));
    }
}
