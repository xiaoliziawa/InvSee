package com.lirxowo.invsee.client.event;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.client.util.GuiUtil;
import com.lirxowo.invsee.client.util.ItemInfoHelper;
import com.lirxowo.invsee.entity.ItemMarkEntity;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * GUI渲染事件处理器 - 在屏幕上渲染物品标记的文字和图标
 * 参考 DRG-Laser-Pointer-Mod 的 RenderGuiEventHandler
 */
@EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class RenderGuiEventHandler {
    private static final float MARK_DISPLAY_RANGE = 64.0F;
    private static final float FRAME_PROTECT = 10;
    private static final float ICON_WIDTH_WITH_MARGIN = 18;
    private static final float MIN_REF_WIDTH = 60;
    private static final int MAX_NAME_WIDTH = 150;
    private static final int LINE_HEIGHT = 10;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.level == null) {
            return;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Matrix3f rotMat = new Matrix3f().rotation(camera.rotation().conjugate(new Quaternionf()));

        Window window = mc.getWindow();
        // 使用 Minecraft 设置中的 FOV
        float fov = mc.options.fov().get().floatValue();
        float fovy = fov * Mth.DEG_TO_RAD;
        float tanHalfFovy = (float) Math.tan(fovy * 0.5F);
        float tanHalfFovx = tanHalfFovy * (float) window.getWidth() / (float) window.getHeight();

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0F, 0F, 4901F);

        int guiWidth, guiHeight;
        float guiScale = (float) window.getGuiScale();
        if (guiScale > 3F) {
            float factor = 3F / guiScale;
            poseStack.scale(factor, factor, factor);
            guiWidth = Math.round((float) window.getWidth() / 3F);
            guiHeight = Math.round((float) window.getHeight() / 3F);
        } else {
            guiWidth = guiGraphics.guiWidth();
            guiHeight = guiGraphics.guiHeight();
        }

        Player player = mc.player;
        Font font = mc.font;
        Level level = mc.level;

        double markDisplayRangeSq = MARK_DISPLAY_RANGE * MARK_DISPLAY_RANGE;
        List<ItemMarkEntity> list = level.getEntitiesOfClass(
                ItemMarkEntity.class,
                player.getBoundingBox().inflate(MARK_DISPLAY_RANGE),
                e -> e.distanceToSqr(player) <= markDisplayRangeSq
        );

        for (ItemMarkEntity markEntity : list) {
            ItemStack itemStack = markEntity.getMarkedItem();
            if (itemStack.isEmpty()) continue;

            float lifeProgress = markEntity.getLifeProgress();
            int alpha = lifeProgress > 0.8f ? (int) ((1f - lifeProgress) / 0.2f * 255) : 255;
            if (alpha <= 0) continue;

            // 获取物品信息
            Component itemName = itemStack.getHoverName();
            Component ownerName = Component.literal("[" + markEntity.getOwnerName() + "]");
            List<Component> infoLines = ItemInfoHelper.getItemInfoLines(itemStack);
            Rarity rarity = itemStack.getRarity();

            // 计算实体在世界中的位置（相对于相机）
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            float bobOffset = Mth.sin(((float) markEntity.tickCount + partialTick) / 10.0F) * 0.1F + 0.1F;

            Vector3f labelPos = new Vector3f(
                    (float) (markEntity.getX() - cameraPos.x),
                    (float) (markEntity.getY() + bobOffset + 0.8 - cameraPos.y),
                    (float) (markEntity.getZ() - cameraPos.z)
            );

            // 计算标签宽度（根据所有文本行计算最大宽度）
            float itemNameWidth = font.width(itemName);
            float ownerNameWidth = font.width(ownerName);
            float maxInfoWidth = 0;
            for (Component line : infoLines) {
                maxInfoWidth = Math.max(maxInfoWidth, font.width(line));
            }

            float mainWidth = ICON_WIDTH_WITH_MARGIN + Math.min(Math.max(itemNameWidth, maxInfoWidth), MAX_NAME_WIDTH);
            float refWidth = Math.max(MIN_REF_WIDTH, Math.max(ownerNameWidth * 0.8F, mainWidth));

            // 计算标签高度
            int totalLines = 1 + infoLines.size(); // 物品名 + 额外信息行
            float labelHeight = 24 + totalLines * LINE_HEIGHT;

            // 旋转到相机空间
            rotMat.transform(labelPos);

            // 计算屏幕坐标
            float rx = labelPos.x / -labelPos.z / tanHalfFovx;
            float ry = labelPos.y / -labelPos.z / tanHalfFovy;

            // 如果在相机后面，跳过
            if (labelPos.z >= 0) continue;

            float xScreen = Mth.clamp(guiWidth * 0.5F * (1 + rx), refWidth / 2 + FRAME_PROTECT, guiWidth - (refWidth / 2 + FRAME_PROTECT));
            float yScreen = Mth.clamp(guiHeight * 0.5F * (1 - ry), labelHeight / 2 + FRAME_PROTECT, guiHeight - (labelHeight / 2 + FRAME_PROTECT));

            // 渲染背景（根据稀有度添加边框颜色）
            int bgColor = (alpha * 64 / 255) << 24;
            int borderColor = (alpha << 24) | ItemInfoHelper.getRarityColor(rarity);

            float bgTop = yScreen - 14;
            float bgBottom = yScreen + 10 + infoLines.size() * LINE_HEIGHT;

            // 背景
            GuiUtil.fill(guiGraphics, xScreen - refWidth / 2 - 2, bgTop, xScreen + refWidth / 2 + 2, bgBottom, bgColor);

            // 稀有度边框（只在非普通稀有度时显示）
            if (rarity != Rarity.COMMON) {
                // 顶部边框
                GuiUtil.fill(guiGraphics, xScreen - refWidth / 2 - 2, bgTop - 1, xScreen + refWidth / 2 + 2, bgTop, borderColor);
                // 底部边框
                GuiUtil.fill(guiGraphics, xScreen - refWidth / 2 - 2, bgBottom, xScreen + refWidth / 2 + 2, bgBottom + 1, borderColor);
                // 左边框
                GuiUtil.fill(guiGraphics, xScreen - refWidth / 2 - 3, bgTop - 1, xScreen - refWidth / 2 - 2, bgBottom + 1, borderColor);
                // 右边框
                GuiUtil.fill(guiGraphics, xScreen + refWidth / 2 + 2, bgTop - 1, xScreen + refWidth / 2 + 3, bgBottom + 1, borderColor);
            }

            // 渲染物品图标
            GuiUtil.renderItem(guiGraphics, itemStack, xScreen - refWidth / 2, yScreen - 10);

            // 渲染物品名称（使用稀有度颜色）
            float textStartX = xScreen - refWidth / 2 + ICON_WIDTH_WITH_MARGIN;
            int nameColor = (alpha << 24) | ItemInfoHelper.getRarityColor(rarity);
            guiGraphics.drawString(font, itemName, (int) textStartX, (int) (yScreen - 10), nameColor, true);

            // 渲染额外信息行
            int yOffset = 0;
            for (Component line : infoLines) {
                int lineColor = (alpha << 24) | 0xAAAAAA;
                guiGraphics.drawString(font, line, (int) textStartX, (int) (yScreen + yOffset), lineColor, true);
                yOffset += LINE_HEIGHT;
            }

            // 渲染玩家名称（缩小，在最底部）
            poseStack.pushPose();
            poseStack.scale(0.8F, 0.8F, 0.8F);
            float scaledX = xScreen * 1.25F;
            float scaledY = (yScreen + yOffset + 8) * 1.25F;
            int ownerColor = (alpha << 24) | 0xFFFF00;
            GuiUtil.drawCenteredString(guiGraphics, font, ownerName, scaledX, scaledY, ownerColor);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
