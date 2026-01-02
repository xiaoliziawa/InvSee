package com.lirxowo.invsee.client.event;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.network.ItemMarkPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * 客户端输入事件处理器 - 处理容器界面中的鼠标中键点击
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

        // 从菜单获取容器位置
        AbstractContainerMenu menu = containerScreen.getMenu();
        BlockPos containerPos = getContainerBlockPos(menu, mc.player);

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

    /**
     * 从容器菜单获取关联的方块位置
     * 只通过菜单的Container获取，不使用hitResult
     * 这样可以正确区分容器方块和玩家背包
     */
    @Nullable
    private static BlockPos getContainerBlockPos(AbstractContainerMenu menu, Player player) {
        // 方法1: 对于ChestMenu，获取其container
        if (menu instanceof ChestMenu chestMenu) {
            Container container = chestMenu.getContainer();
            if (container instanceof BlockEntity blockEntity) {
                return blockEntity.getBlockPos();
            }
        }

        // 方法2: 遍历所有槽位，查找属于容器的槽位
        for (Slot slot : menu.slots) {
            Container slotContainer = slot.container;
            // 跳过玩家背包
            if (slotContainer == player.getInventory()) {
                continue;
            }
            // 检查是否是BlockEntity
            if (slotContainer instanceof BlockEntity blockEntity) {
                return blockEntity.getBlockPos();
            }
        }

        // 如果没有找到容器方块（如玩家背包界面），返回null
        // 不再使用hitResult，避免错误地将玩家视线指向的容器方块作为标记位置
        return null;
    }
}
