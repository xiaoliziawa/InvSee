package com.lirxowo.invsee.api.provider;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

/**
 * 槽位高亮提供者接口
 * 实现此接口以自定义 InvSee 的槽位高亮行为。
 *
 * @since 1.0.0
 */
public interface ISlotHighlightProvider {

    String getProviderId();

    boolean shouldHighlight(Slot slot, ItemStack trackedItem);

    Color getHighlightColor(Slot slot, ItemStack trackedItem, float progress);

    default boolean renderCustomHighlight(GuiGraphics guiGraphics, Slot slot, ItemStack trackedItem, float progress) {
        return false;
    }

    default int getPriority() {
        return 500;
    }

    default boolean isExclusive() {
        return true;
    }
}
