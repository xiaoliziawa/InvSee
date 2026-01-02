package com.lirxowo.invsee.client.event;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.network.ItemMarkPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端输入事件处理器 - 处理容器界面中的鼠标中键点击
 */
@EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class ContainerInputHandler {
    // 视线检测范围
    private static final double PICK_RANGE = 8.0;

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
        // 如果是玩家背包的槽位，不应该高亮任何容器
        boolean isPlayerInventorySlot = hoveredSlot.container == mc.player.getInventory();

        // 如果是玩家背包界面（E键打开的），永远不高亮容器
        boolean isPlayerInventoryScreen = mc.screen instanceof InventoryScreen;

        BlockPos containerPos = null;

        // 只有当不是玩家背包的槽位，且不是玩家背包界面时，才检测视线指向的容器
        if (!isPlayerInventorySlot && !isPlayerInventoryScreen) {
            HitResult hitResult = mc.player.pick(PICK_RANGE, 0, false);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos hitPos = blockHitResult.getBlockPos();
                BlockEntity blockEntity = mc.level.getBlockEntity(hitPos);

                // 只有当视线指向的方块是容器时才使用
                if (blockEntity instanceof Container) {
                    containerPos = hitPos;
                }
            }
        }

        // 发送网络包到服务器
        ItemMarkPayload payload;
        if (containerPos != null) {
            payload = ItemMarkPayload.withContainer(containerPos, itemStack);
        } else {
            payload = ItemMarkPayload.withoutContainer(itemStack);
        }
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
