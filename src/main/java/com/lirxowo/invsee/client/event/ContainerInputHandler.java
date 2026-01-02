package com.lirxowo.invsee.client.event;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.client.KeyBindings;
import com.lirxowo.invsee.client.tracking.TrackingList;
import com.lirxowo.invsee.network.ItemMarkPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端输入事件处理器 - 处理容器界面中的按键输入
 * 支持自定义键位绑定，默认为鼠标中键
 * 容器位置的获取由服务端完成，因为有些模组不会把BlockEntity同步到客户端
 */
@EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class ContainerInputHandler {

    /**
     * 处理鼠标按键事件
     * 当自定义键位为鼠标按键时触发
     */
    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        // 只处理按下事件
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        // 检查是否为绑定的鼠标按键
        if (!KeyBindings.isMarkMouseButton(event.getButton())) {
            return;
        }

        // 执行标记逻辑
        if (handleMarkItem()) {
            event.setCanceled(true);
        }
    }

    /**
     * 处理键盘按键事件
     * 当自定义键位为键盘按键时触发
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // 只处理按下事件
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        // 检查是否为绑定的键盘按键
        if (!KeyBindings.isMarkKeyboardKey(event.getKey(), event.getScanCode())) {
            return;
        }

        // 执行标记逻辑
        handleMarkItem();
        // 注意：键盘事件通常不需要取消，除非有冲突
    }

    /**
     * 客户端Tick事件 - 用于更新追踪状态
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            // 检查追踪是否过期
            TrackingList.tickTracking(mc.level.getGameTime());
        }
    }

    /**
     * 处理物品标记的核心逻辑
     * @return 如果成功处理则返回true
     */
    private static boolean handleMarkItem() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }

        // 检查当前是否在容器界面中
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }

        // 获取鼠标悬停的槽位
        Slot hoveredSlot = containerScreen.getSlotUnderMouse();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            return false;
        }

        ItemStack itemStack = hoveredSlot.getItem();
        if (itemStack.isEmpty()) {
            return false;
        }

        // 判断悬停的槽位是否属于玩家背包
        boolean isPlayerInventorySlot = hoveredSlot.container == mc.player.getInventory();

        // 如果是玩家背包界面（E键打开的），标记为玩家背包
        boolean isPlayerInventoryScreen = mc.screen instanceof InventoryScreen;

        // 发送网络包到服务器
        // isFromPlayerInventory: 如果是玩家背包槽位或玩家背包界面，则为true
        boolean isFromPlayerInventory = isPlayerInventorySlot || isPlayerInventoryScreen;
        ItemMarkPayload payload = new ItemMarkPayload(isFromPlayerInventory, itemStack);
        PacketDistributor.sendToServer(payload);

        // 开始客户端追踪 - 用于槽位高亮
        TrackingList.startTracking(itemStack, mc.level.getGameTime());

        // 播放音效反馈
        mc.player.playSound(
                net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                0.5f, 1.5f
        );

        return true;
    }
}
