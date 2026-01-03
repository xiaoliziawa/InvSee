package com.lirxowo.invsee.api;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.api.event.MarkEvent;
import com.lirxowo.invsee.api.provider.IContainerPositionProvider;
import com.lirxowo.invsee.api.provider.IItemInfoProvider;
import com.lirxowo.invsee.api.provider.IMarkEventListener;
import com.lirxowo.invsee.api.provider.ISlotHighlightProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class InvseeAPIHandler {

    private InvseeAPIHandler() {}

    public static List<Component> collectItemInfo(ItemStack stack) {
        List<Component> allLines = new ArrayList<>();

        for (IItemInfoProvider provider : InvseeAPI.getItemInfoProviders()) {
            try {
                if (provider.canProvideInfo(stack)) {
                    List<Component> lines = provider.getInfoLines(stack);
                    if (lines != null && !lines.isEmpty()) {
                        allLines.addAll(lines);
                    }
                }
            } catch (Exception e) {
                com.lirxowo.invsee.Invsee.LOGGER.warn(
                        "Error in item info provider {}: {}",
                        provider.getProviderId(), e.getMessage()
                );
            }
        }

        return allLines;
    }

    public static boolean shouldHighlightSlot(Slot slot, ItemStack trackedItem) {
        if (trackedItem.isEmpty() || !slot.hasItem()) {
            return false;
        }

        for (ISlotHighlightProvider provider : InvseeAPI.getSlotHighlightProviders()) {
            try {
                if (provider.shouldHighlight(slot, trackedItem)) {
                    return true;
                }
            } catch (Exception e) {
                com.lirxowo.invsee.Invsee.LOGGER.warn(
                        "Error in slot highlight provider {}: {}",
                        provider.getProviderId(), e.getMessage()
                );
            }
        }

        return ItemStack.isSameItemSameComponents(slot.getItem(), trackedItem);
    }

    public static Color getHighlightColor(Slot slot, ItemStack trackedItem, float progress, Color defaultColor) {
        for (ISlotHighlightProvider provider : InvseeAPI.getSlotHighlightProviders()) {
            try {
                if (provider.shouldHighlight(slot, trackedItem)) {
                    return provider.getHighlightColor(slot, trackedItem, progress);
                }
            } catch (Exception e) {
            }
        }
        return defaultColor;
    }

    @Nullable
    public static BlockPos getContainerPosition(AbstractContainerMenu menu, Player player) {
        for (IContainerPositionProvider provider : InvseeAPI.getContainerPositionProviders()) {
            try {
                if (provider.canHandle(menu)) {
                    BlockPos pos = provider.getContainerPosition(menu, player);
                    if (pos != null) {
                        return pos;
                    }
                }
            } catch (Exception e) {
                com.lirxowo.invsee.Invsee.LOGGER.warn(
                        "Error in container position provider {}: {}",
                        provider.getProviderId(), e.getMessage()
                );
            }
        }
        return null;
    }

    public static boolean fireItemMarkedEvent(MarkEvent event) {
        for (IMarkEventListener listener : InvseeAPI.getMarkEventListeners()) {
            try {
                listener.onItemMarked(event);
                if (event.isCanceled()) {
                    return true;
                }
            } catch (Exception e) {
                Invsee.LOGGER.warn(
                        "Error in mark event listener: {}", e.getMessage()
                );
            }
        }
        return false;
    }

    public static void fireTrackingStartedEvent(MarkEvent event) {
        for (IMarkEventListener listener : InvseeAPI.getMarkEventListeners()) {
            try {
                listener.onTrackingStarted(event);
            } catch (Exception e) {
                Invsee.LOGGER.warn(
                        "Error in mark event listener: {}", e.getMessage()
                );
            }
        }
    }

    public static void fireTrackingEndedEvent(MarkEvent event) {
        for (IMarkEventListener listener : InvseeAPI.getMarkEventListeners()) {
            try {
                listener.onTrackingEnded(event);
            } catch (Exception e) {
                Invsee.LOGGER.warn(
                        "Error in mark event listener: {}", e.getMessage()
                );
            }
        }
    }

    public static void fireEntityCreatedEvent(MarkEvent event) {
        for (IMarkEventListener listener : InvseeAPI.getMarkEventListeners()) {
            try {
                listener.onMarkEntityCreated(event);
            } catch (Exception e) {
                Invsee.LOGGER.warn(
                        "Error in mark event listener: {}", e.getMessage()
                );
            }
        }
    }

    public static void fireEntityRemovedEvent(MarkEvent event) {
        for (IMarkEventListener listener : InvseeAPI.getMarkEventListeners()) {
            try {
                listener.onMarkEntityRemoved(event);
            } catch (Exception e) {
                Invsee.LOGGER.warn(
                        "Error in mark event listener: {}", e.getMessage()
                );
            }
        }
    }
}
