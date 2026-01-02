package com.lirxowo.invsee.api.provider;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nullable;

/**
 * 容器位置提供者接口
 * 实现此接口以支持自定义容器类型的位置检测。
 *
 * @since 1.0.0
 */
public interface IContainerPositionProvider {

    String getProviderId();

    boolean canHandle(AbstractContainerMenu menu);

    @Nullable
    BlockPos getContainerPosition(AbstractContainerMenu menu, Player player);

    default int getPriority() {
        return 500;
    }
}
