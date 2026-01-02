package com.lirxowo.invsee.client.event;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.network.ItemMarkPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端输入事件处理器 - 处理容器界面中的鼠标中键点击
 * 容器位置的获取由服务端完成，因为有些模组不会把BlockEntity同步到客户端
 */
@EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class ContainerInputHandler {

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        // 只处理鼠标中键按下事件
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // 检查当前是否在容器界面中
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        // 获取鼠标悬停的槽位
        Slot hoveredSlot = containerScreen.getSlotUnderMouse();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            return;
        }

        ItemStack itemStack = hoveredSlot.getItem();
        if (itemStack.isEmpty()) {
            return;
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

        // 播放音效反馈
        mc.player.playSound(
                net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                0.5f, 1.5f
        );

        // 阻止默认行为
        event.setCanceled(true);
    }
}
