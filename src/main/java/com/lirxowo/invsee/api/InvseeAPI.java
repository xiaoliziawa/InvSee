package com.lirxowo.invsee.api;

import com.lirxowo.invsee.api.provider.IItemInfoProvider;
import com.lirxowo.invsee.api.provider.ISlotHighlightProvider;
import com.lirxowo.invsee.api.provider.IMarkEventListener;
import com.lirxowo.invsee.api.provider.IContainerPositionProvider;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * InvSee API - 主入口类
 *
 * 允许其他模组开发者：
 * 1. 注册自定义的物品信息提供者
 * 2. 注册自定义的槽位高亮提供者
 * 3. 注册容器位置提供者
 * 4. 监听物品标记事件
 *
 * @since 1.0.0
 */
public final class InvseeAPI {

    private static final List<IItemInfoProvider> itemInfoProviders = new CopyOnWriteArrayList<>();
    private static final List<ISlotHighlightProvider> slotHighlightProviders = new CopyOnWriteArrayList<>();
    private static final List<IContainerPositionProvider> containerPositionProviders = new CopyOnWriteArrayList<>();
    private static final List<IMarkEventListener> markEventListeners = new CopyOnWriteArrayList<>();

    private InvseeAPI() {}

    public static void registerItemInfoProvider(IItemInfoProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        if (!itemInfoProviders.contains(provider)) {
            itemInfoProviders.add(provider);
            itemInfoProviders.sort(Comparator.comparingInt(IItemInfoProvider::getPriority).reversed());
        }
    }

    public static boolean unregisterItemInfoProvider(IItemInfoProvider provider) {
        return itemInfoProviders.remove(provider);
    }

    public static List<IItemInfoProvider> getItemInfoProviders() {
        return new ArrayList<>(itemInfoProviders);
    }

    public static void registerSlotHighlightProvider(ISlotHighlightProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        if (!slotHighlightProviders.contains(provider)) {
            slotHighlightProviders.add(provider);
            slotHighlightProviders.sort(Comparator.comparingInt(ISlotHighlightProvider::getPriority).reversed());
        }
    }

    public static boolean unregisterSlotHighlightProvider(ISlotHighlightProvider provider) {
        return slotHighlightProviders.remove(provider);
    }

    public static List<ISlotHighlightProvider> getSlotHighlightProviders() {
        return new ArrayList<>(slotHighlightProviders);
    }

    public static void registerContainerPositionProvider(IContainerPositionProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        if (!containerPositionProviders.contains(provider)) {
            containerPositionProviders.add(provider);
            containerPositionProviders.sort(Comparator.comparingInt(IContainerPositionProvider::getPriority).reversed());
        }
    }

    public static boolean unregisterContainerPositionProvider(IContainerPositionProvider provider) {
        return containerPositionProviders.remove(provider);
    }

    public static List<IContainerPositionProvider> getContainerPositionProviders() {
        return new ArrayList<>(containerPositionProviders);
    }

    public static void registerMarkEventListener(IMarkEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!markEventListeners.contains(listener)) {
            markEventListeners.add(listener);
        }
    }

    public static boolean unregisterMarkEventListener(IMarkEventListener listener) {
        return markEventListeners.remove(listener);
    }

    public static List<IMarkEventListener> getMarkEventListeners() {
        return new ArrayList<>(markEventListeners);
    }

    public static boolean isItemBeingTracked(ItemStack stack) {
        return com.lirxowo.invsee.client.tracking.TrackingList.beingTracked(stack);
    }

    public static ItemStack getTrackedItem() {
        return com.lirxowo.invsee.client.tracking.TrackingList.getTrackedStack();
    }

    public static boolean isTracking() {
        return com.lirxowo.invsee.client.tracking.TrackingList.isTracking();
    }

    public static String getAPIVersion() {
        return "1.0.0";
    }
}
