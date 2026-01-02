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

@EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class ContainerInputHandler {

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        if (!KeyBindings.isMarkMouseButton(event.getButton())) {
            return;
        }

        if (handleMarkItem()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        if (!KeyBindings.isMarkKeyboardKey(event.getKey(), event.getScanCode())) {
            return;
        }

        handleMarkItem();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            TrackingList.tickTracking(mc.level.getGameTime());
        }
    }

    private static boolean handleMarkItem() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }

        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }

        Slot hoveredSlot = containerScreen.getSlotUnderMouse();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            return false;
        }

        ItemStack itemStack = hoveredSlot.getItem();
        if (itemStack.isEmpty()) {
            return false;
        }

        boolean isPlayerInventorySlot = hoveredSlot.container == mc.player.getInventory();
        boolean isPlayerInventoryScreen = mc.screen instanceof InventoryScreen;
        boolean isFromPlayerInventory = isPlayerInventorySlot || isPlayerInventoryScreen;
        ItemMarkPayload payload = new ItemMarkPayload(isFromPlayerInventory, itemStack);
        PacketDistributor.sendToServer(payload);

        TrackingList.startTracking(itemStack, mc.level.getGameTime());

        mc.player.playSound(
                net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                0.5f, 1.5f
        );

        return true;
    }
}
