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

@Mixin(AbstractContainerScreen.class)
public class SlotHighlightMixin {

    @Inject(
            at = @At("HEAD"),
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"
    )
    private void invsee$renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (slot.hasItem() && TrackingList.beingTracked(slot.getItem())) {
            Color highlightColor = TrackingList.getHighlightColor();

            Minecraft mc = Minecraft.getInstance();
            float progress = 0f;
            if (mc.level != null) {
                progress = TrackingList.getTrackProgress(mc.level.getGameTime());
            }

            float alpha;
            if (progress > 0.8f) {
                alpha = (1.0f - progress) / 0.2f;
            } else {
                alpha = 1.0f;
            }

            int colorWithAlpha = new Color(
                    highlightColor.getRed(),
                    highlightColor.getGreen(),
                    highlightColor.getBlue(),
                    (int)(highlightColor.getAlpha() * alpha)
            ).getRGB();

            RenderSystem.disableDepthTest();
            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, colorWithAlpha);
            RenderSystem.enableDepthTest();
        }
    }
}
