package com.lirxowo.invsee.api.provider;

import com.lirxowo.invsee.api.event.MarkEvent;

/**
 * 标记事件监听器接口
 * 实现此接口以在物品被标记时接收通知。
 *
 * @since 1.0.0
 */
public interface IMarkEventListener {

    default void onItemMarked(MarkEvent event) {}

    default void onTrackingStarted(MarkEvent event) {}

    default void onTrackingEnded(MarkEvent event) {}

    default void onMarkEntityCreated(MarkEvent event) {}

    default void onMarkEntityRemoved(MarkEvent event) {}
}
