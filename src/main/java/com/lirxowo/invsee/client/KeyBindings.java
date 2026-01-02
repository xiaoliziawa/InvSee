package com.lirxowo.invsee.client;

import com.lirxowo.invsee.Invsee;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class KeyBindings {

    public static final KeyMapping MARK_ITEM = new KeyMapping(
            "key.invsee.mark_item",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            "key.categories.invsee"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MARK_ITEM);
        Invsee.LOGGER.info("InvSee key mappings registered!");
    }

    public static boolean isMarkKeyPressed(int keyCode, int scanCode, boolean isMouse) {
        InputConstants.Key boundKey = MARK_ITEM.getKey();

        if (isMouse) {
            return boundKey.getType() == InputConstants.Type.MOUSE
                    && boundKey.getValue() == keyCode;
        } else {
            return MARK_ITEM.matches(keyCode, scanCode);
        }
    }

    public static boolean isMarkMouseButton(int button) {
        InputConstants.Key boundKey = MARK_ITEM.getKey();
        return boundKey.getType() == InputConstants.Type.MOUSE
                && boundKey.getValue() == button;
    }

    public static boolean isMarkKeyboardKey(int keyCode, int scanCode) {
        InputConstants.Key boundKey = MARK_ITEM.getKey();
        if (boundKey.getType() == InputConstants.Type.KEYSYM) {
            return MARK_ITEM.matches(keyCode, scanCode);
        }
        return false;
    }
}
