package com.lirxowo.invsee.client.event;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.client.KeyBindings;
import com.lirxowo.invsee.client.tracking.TrackingList;
import com.lirxowo.invsee.compat.jei.JEICompat;
import com.lirxowo.invsee.network.ItemMarkPacket;
import com.lirxowo.invsee.network.ItemMarkPacket.MarkSource;
import com.lirxowo.invsee.registry.NetworkRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
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

        if (mc.screen == null) {
            return false;
        }

        ItemStack itemStack = null;
        MarkSource source = MarkSource.VIRTUAL;

        if (JEICompat.isLoaded()) {
            itemStack = JEICompat.getItemUnderMouse();
            if (itemStack != null && !itemStack.isEmpty()) {
                source = MarkSource.VIRTUAL;
            }
        }

        if ((itemStack == null || itemStack.isEmpty())
                && mc.screen instanceof AbstractContainerScreen<?> containerScreen) {
            Slot hoveredSlot = containerScreen.getSlotUnderMouse();
            if (hoveredSlot != null && hoveredSlot.hasItem()) {
                itemStack = hoveredSlot.getItem();
                boolean isPlayerInventorySlot = hoveredSlot.container == mc.player.getInventory();
                boolean isPlayerInventoryScreen = mc.screen instanceof InventoryScreen;

                if (isPlayerInventorySlot || isPlayerInventoryScreen) {
                    source = MarkSource.PLAYER_INVENTORY;
                } else {
                    source = MarkSource.CONTAINER;
                }
            }
        }

        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }

        ItemMarkPacket packet = new ItemMarkPacket(source, itemStack);
        NetworkRegister.sendToServer(packet);

        TrackingList.startTracking(itemStack, mc.level.getGameTime());

        mc.player.playSound(
                net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                0.5f, 1.5f
        );

        return true;
    }
}
