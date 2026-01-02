package com.lirxowo.invsee.client.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

public class GuiUtil {

    public static void fill(GuiGraphics guiGraphics, float minX, float minY, float maxX, float maxY, int color) {
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        float j;
        if (minX < maxX) {
            j = minX;
            minX = maxX;
            maxX = j;
        }
        if (minY < maxY) {
            j = minY;
            minY = maxY;
            maxY = j;
        }
        VertexConsumer vertexconsumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        vertexconsumer.addVertex(matrix4f, minX, minY, 0).setColor(color);
        vertexconsumer.addVertex(matrix4f, minX, maxY, 0).setColor(color);
        vertexconsumer.addVertex(matrix4f, maxX, maxY, 0).setColor(color);
        vertexconsumer.addVertex(matrix4f, maxX, minY, 0).setColor(color);
        guiGraphics.flush();
    }

    public static void renderItem(GuiGraphics guiGraphics, ItemStack stack, float x, float y) {
        Minecraft mc = Minecraft.getInstance();
        renderItem(guiGraphics, mc.player, mc.level, stack, x, y, 0, 0);
    }

    public static void renderItem(GuiGraphics guiGraphics, @Nullable LivingEntity entity, @Nullable Level level,
                                  ItemStack stack, float x, float y, int seed, int guiOffset) {
        if (!stack.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            BakedModel bakedmodel = mc.getItemRenderer().getModel(stack, level, entity, seed);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + 8, y + 8, (float) (150 + (bakedmodel.isGui3d() ? guiOffset : 0)));

            guiGraphics.pose().scale(16.0F, -16.0F, 16.0F);
            boolean flag = !bakedmodel.usesBlockLight();
            if (flag) {
                Lighting.setupForFlatItems();
            }

            mc.getItemRenderer().render(stack, ItemDisplayContext.GUI, false,
                    guiGraphics.pose(), guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
            guiGraphics.flush();

            if (flag) {
                Lighting.setupFor3DItems();
            }
            guiGraphics.pose().popPose();
        }
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, Component text, float x, float y, int color) {
        FormattedCharSequence formattedcharsequence = text.getVisualOrderText();
        guiGraphics.drawString(font, formattedcharsequence, (int)(x - font.width(formattedcharsequence) / 2), (int)y, color, true);
    }

    public static void drawString(GuiGraphics guiGraphics, Font font, Component text, float x, float y, int color) {
        guiGraphics.drawString(font, text, (int)x, (int)y, color, true);
    }
}
