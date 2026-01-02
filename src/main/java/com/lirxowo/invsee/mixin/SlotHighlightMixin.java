package com.lirxowo.invsee.mixin;

import com.lirxowo.invsee.client.tracking.TrackingList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

/**
 * Mixin 注入到 AbstractContainerScreen，在槽位渲染时添加高亮效果
 * 仿照 FindMe 模组的实现方式
 */
@Mixin(AbstractContainerScreen.class)
public class SlotHighlightMixin {

    /**
     * 在槽位渲染前注入高亮效果
     * 如果该槽位中的物品正在被追踪，则绘制高亮背景
     */
    @Inject(
            at = @At("HEAD"),
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"
    )
    private void invsee$renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        // 检查槽位是否有物品，且是否正在被追踪
        if (slot.hasItem() && TrackingList.beingTracked(slot.getItem())) {
            // 获取高亮颜色
            Color highlightColor = TrackingList.getHighlightColor();

            // 计算淡出效果的透明度
            Minecraft mc = Minecraft.getInstance();
            float progress = 0f;
            if (mc.level != null) {
                progress = TrackingList.getTrackProgress(mc.level.getGameTime());
            }

            // 淡出效果：最后20%时间内线性淡出
            float alpha;
            if (progress > 0.8f) {
                alpha = (1.0f - progress) / 0.2f;
            } else {
                alpha = 1.0f;
            }

            // 计算最终颜色 (保持原色的RGB，但应用淡出的alpha)
            int colorWithAlpha = new Color(
                    highlightColor.getRed(),
                    highlightColor.getGreen(),
                    highlightColor.getBlue(),
                    (int)(highlightColor.getAlpha() * alpha)
            ).getRGB();

            // 禁用深度测试，确保高亮显示在最上层
            RenderSystem.disableDepthTest();

            // 绘制高亮矩形 (16x16 覆盖整个槽位)
            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, colorWithAlpha);

            // 恢复深度测试
            RenderSystem.enableDepthTest();
        }
    }
}
