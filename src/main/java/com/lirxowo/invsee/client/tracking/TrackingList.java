package com.lirxowo.invsee.client.tracking;

import com.lirxowo.invsee.config.InvseeConfig;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

/**
 * 追踪列表管理类 - 管理正在被高亮追踪的物品
 * 使用 ItemStack.isSameItemSameComponents 进行精确匹配
 */
public class TrackingList {
    // 当前正在追踪的物品
    private static ItemStack trackedStack = ItemStack.EMPTY;

    // 高亮颜色 (默认金色，带透明度)
    private static Color highlightColor = new Color(255, 228, 32, 255);

    // 追踪开始时间 (游戏tick)
    private static long trackStartTime = 0;

    /**
     * 检查物品是否正在被追踪
     * @param stack 要检查的物品
     * @return 如果物品正在被追踪则返回true
     */
    public static boolean beingTracked(ItemStack stack) {
        if (trackedStack.isEmpty()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(stack, trackedStack);
    }

    /**
     * 开始追踪指定物品
     * @param stack 要追踪的物品
     * @param gameTime 当前游戏时间(tick)
     */
    public static void startTracking(ItemStack stack, long gameTime) {
        trackedStack = stack.copyWithCount(1);
        trackStartTime = gameTime;
    }

    /**
     * 清除追踪状态
     */
    public static void clear() {
        trackedStack = ItemStack.EMPTY;
        trackStartTime = 0;
    }

    /**
     * 检查追踪是否过期，如果过期则自动清除
     * @param currentGameTime 当前游戏时间
     */
    public static void tickTracking(long currentGameTime) {
        if (!trackedStack.isEmpty() && (currentGameTime - trackStartTime) > InvseeConfig.getHighlightDurationTicks()) {
            clear();
        }
    }

    /**
     * 获取当前正在追踪的物品
     * @return 当前追踪的物品副本，如果没有则返回 EMPTY
     */
    public static ItemStack getTrackedStack() {
        return trackedStack.copy();
    }

    /**
     * 检查是否正在追踪任何物品
     * @return 如果正在追踪则返回true
     */
    public static boolean isTracking() {
        return !trackedStack.isEmpty();
    }

    /**
     * 获取高亮颜色
     * @return 高亮颜色
     */
    public static Color getHighlightColor() {
        return highlightColor;
    }

    /**
     * 设置高亮颜色
     * @param color 新的高亮颜色
     */
    public static void setHighlightColor(Color color) {
        highlightColor = color;
    }

    /**
     * 获取追踪进度 (0.0 - 1.0)
     * @param currentGameTime 当前游戏时间
     * @return 追踪进度，用于淡出效果
     */
    public static float getTrackProgress(long currentGameTime) {
        if (trackedStack.isEmpty()) {
            return 0f;
        }
        float progress = (float)(currentGameTime - trackStartTime) / InvseeConfig.getHighlightDurationTicks();
        return Math.min(1.0f, Math.max(0.0f, progress));
    }
}
