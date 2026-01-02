package com.lirxowo.invsee.client;

import com.lirxowo.invsee.Invsee;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 键位绑定注册类
 * 注册模组使用的自定义按键，允许玩家在游戏设置中自定义
 */
@EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class KeyBindings {

    /**
     * 标记物品键位
     * 默认: 鼠标中键
     * 用于在容器界面中标记/追踪物品
     */
    public static final KeyMapping MARK_ITEM = new KeyMapping(
            "key.invsee.mark_item",  // 翻译键
            InputConstants.Type.MOUSE,  // 输入类型：鼠标
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,  // 默认按键：鼠标中键
            "key.categories.invsee"  // 键位分类
    );

    /**
     * 注册键位映射到游戏控制设置
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MARK_ITEM);
        Invsee.LOGGER.info("InvSee key mappings registered!");
    }

    /**
     * 检查标记物品键位是否被按下
     * 支持键盘和鼠标按键
     *
     * @param keyCode 键码 (对于鼠标为按钮ID)
     * @param scanCode 扫描码 (仅用于键盘)
     * @param isMouse 是否为鼠标按键
     * @return 如果匹配则返回true
     */
    public static boolean isMarkKeyPressed(int keyCode, int scanCode, boolean isMouse) {
        InputConstants.Key boundKey = MARK_ITEM.getKey();

        if (isMouse) {
            // 鼠标按键检查
            return boundKey.getType() == InputConstants.Type.MOUSE
                    && boundKey.getValue() == keyCode;
        } else {
            // 键盘按键检查
            return MARK_ITEM.matches(keyCode, scanCode);
        }
    }

    /**
     * 检查鼠标按钮是否匹配标记键位
     * @param button 鼠标按钮ID
     * @return 如果匹配则返回true
     */
    public static boolean isMarkMouseButton(int button) {
        InputConstants.Key boundKey = MARK_ITEM.getKey();
        return boundKey.getType() == InputConstants.Type.MOUSE
                && boundKey.getValue() == button;
    }

    /**
     * 检查键盘按键是否匹配标记键位
     * @param keyCode 键码
     * @param scanCode 扫描码
     * @return 如果匹配则返回true
     */
    public static boolean isMarkKeyboardKey(int keyCode, int scanCode) {
        InputConstants.Key boundKey = MARK_ITEM.getKey();
        if (boundKey.getType() == InputConstants.Type.KEYSYM) {
            return MARK_ITEM.matches(keyCode, scanCode);
        }
        return false;
    }
}
