package com.lirxowo.invsee.api.provider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 物品信息提供者接口
 * 实现此接口以在 InvSee 的物品信息框中添加自定义信息。
 *
 * @since 1.0.0
 */
public interface IItemInfoProvider {

    String getProviderId();

    boolean canProvideInfo(ItemStack stack);

    List<Component> getInfoLines(ItemStack stack);

    default int getPriority() {
        return 500;
    }

    default String getDisplayName() {
        return getProviderId();
    }

    default boolean showPlaceholderWhenUnavailable() {
        return false;
    }
}
