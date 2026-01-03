package com.lirxowo.invsee.client.event;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.client.util.GuiUtil;
import com.lirxowo.invsee.client.util.ItemInfoHelper;
import com.lirxowo.invsee.config.InvseeConfig;
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

@EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class RenderGuiEventHandler {
    private static final float FRAME_PROTECT = 10;
    private static final float ICON_WIDTH_WITH_MARGIN = 18;
    private static final float MIN_REF_WIDTH = 60;
    private static final int MAX_NAME_WIDTH = 150;
    private static final int LINE_HEIGHT = 10;
    private static final int TOOLTIP_GAP = 8;
    private static final int MAX_TOOLTIP_WIDTH = 200;

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

        float markDisplayRange = InvseeConfig.getMarkDisplayRange();
        double markDisplayRangeSq = InvseeConfig.getMarkDisplayRangeSq();
        List<ItemMarkEntity> list = level.getEntitiesOfClass(
                ItemMarkEntity.class,
                player.getBoundingBox().inflate(markDisplayRange),
                e -> e.distanceToSqr(player) <= markDisplayRangeSq
        );

        for (ItemMarkEntity markEntity : list) {
            // Filter by team visibility
            if (!markEntity.shouldBeVisibleTo(player)) {
                continue;
            }

            ItemStack itemStack = markEntity.getMarkedItem();
            if (itemStack.isEmpty()) continue;

            float lifeProgress = markEntity.getLifeProgress();
            int alpha = lifeProgress > 0.8f ? (int) ((1f - lifeProgress) / 0.2f * 255) : 255;
            if (alpha <= 0) continue;

            Component itemName = itemStack.getHoverName();
            Component ownerName = Component.literal("[" + markEntity.getOwnerName() + "]");

            // Only get extra info if config allows
            boolean showExtraInfo = InvseeConfig.isShowExtraInfo();
            List<Component> infoLines = showExtraInfo ? ItemInfoHelper.getItemInfoLines(itemStack) : List.of();
            List<Component> tooltipLines = InvseeConfig.isShowTooltip()
                    ? ItemInfoHelper.getItemTooltipLines(itemStack) : List.of();
            Rarity rarity = itemStack.getRarity();

            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            float bobOffset = Mth.sin(((float) markEntity.tickCount + partialTick) / 10.0F) * 0.1F + 0.1F;

            Vector3f labelPos = new Vector3f(
                    (float) (markEntity.getX() - cameraPos.x),
                    (float) (markEntity.getY() + bobOffset + 0.8 - cameraPos.y),
                    (float) (markEntity.getZ() - cameraPos.z)
            );

            float itemNameWidth = font.width(itemName);
            float ownerNameWidth = font.width(ownerName);
            float maxInfoWidth = 0;
            for (Component line : infoLines) {
                maxInfoWidth = Math.max(maxInfoWidth, font.width(line));
            }

            float mainWidth = ICON_WIDTH_WITH_MARGIN + Math.min(Math.max(itemNameWidth, maxInfoWidth), MAX_NAME_WIDTH);
            float refWidth = Math.max(MIN_REF_WIDTH, Math.max(ownerNameWidth * 0.8F, mainWidth));

            int totalLines = 1 + infoLines.size();
            float labelHeight = 24 + totalLines * LINE_HEIGHT;

            rotMat.transform(labelPos);

            float rx = labelPos.x / -labelPos.z / tanHalfFovx;
            float ry = labelPos.y / -labelPos.z / tanHalfFovy;

            if (labelPos.z >= 0) continue;

            float xScreen = Mth.clamp(guiWidth * 0.5F * (1 + rx), refWidth / 2 + FRAME_PROTECT, guiWidth - (refWidth / 2 + FRAME_PROTECT));
            float yScreen = Mth.clamp(guiHeight * 0.5F * (1 - ry), labelHeight / 2 + FRAME_PROTECT, guiHeight - (labelHeight / 2 + FRAME_PROTECT));

            int bgColor = (alpha * 64 / 255) << 24;
            int borderColor = (alpha << 24) | ItemInfoHelper.getRarityColor(rarity);

            float bgTop = yScreen - 14;
            float bgBottom = yScreen + 10 + infoLines.size() * LINE_HEIGHT;

            GuiUtil.fill(guiGraphics, xScreen - refWidth / 2 - 2, bgTop, xScreen + refWidth / 2 + 2, bgBottom, bgColor);

            if (rarity != Rarity.COMMON) {
                GuiUtil.fill(guiGraphics, xScreen - refWidth / 2 - 2, bgTop - 1, xScreen + refWidth / 2 + 2, bgTop, borderColor);
                GuiUtil.fill(guiGraphics, xScreen - refWidth / 2 - 2, bgBottom, xScreen + refWidth / 2 + 2, bgBottom + 1, borderColor);
                GuiUtil.fill(guiGraphics, xScreen - refWidth / 2 - 3, bgTop - 1, xScreen - refWidth / 2 - 2, bgBottom + 1, borderColor);
                GuiUtil.fill(guiGraphics, xScreen + refWidth / 2 + 2, bgTop - 1, xScreen + refWidth / 2 + 3, bgBottom + 1, borderColor);
            }

            GuiUtil.renderItem(guiGraphics, itemStack, xScreen - refWidth / 2, yScreen - 10);

            float textStartX = xScreen - refWidth / 2 + ICON_WIDTH_WITH_MARGIN;
            int nameColor = (alpha << 24) | ItemInfoHelper.getRarityColor(rarity);
            guiGraphics.drawString(font, itemName, (int) textStartX, (int) (yScreen - 10), nameColor, true);

            int yOffset = 0;
            for (Component line : infoLines) {
                int lineColor = (alpha << 24) | 0xAAAAAA;
                guiGraphics.drawString(font, line, (int) textStartX, (int) (yScreen + yOffset), lineColor, true);
                yOffset += LINE_HEIGHT;
            }

            poseStack.pushPose();
            poseStack.scale(0.8F, 0.8F, 0.8F);
            float scaledX = xScreen * 1.25F;
            float scaledY = (yScreen + yOffset + 8) * 1.25F;
            int ownerColor = (alpha << 24) | 0xFFFF00;
            GuiUtil.drawCenteredString(guiGraphics, font, ownerName, scaledX, scaledY, ownerColor);
            poseStack.popPose();

            if (InvseeConfig.isShowTooltip() && !tooltipLines.isEmpty()) {
                renderTooltipBox(guiGraphics, font, tooltipLines, xScreen, yScreen, refWidth, bgTop, bgBottom, alpha, guiWidth);
            }
        }

        poseStack.popPose();
    }

    private static void renderTooltipBox(GuiGraphics guiGraphics, Font font, List<Component> tooltipLines,
                                         float mainX, float mainY, float mainWidth, float mainTop, float mainBottom,
                                         int alpha, int guiWidth) {
        if (tooltipLines.isEmpty()) return;

        float tooltipWidth = 0;
        for (Component line : tooltipLines) {
            tooltipWidth = Math.max(tooltipWidth, font.width(line));
        }
        tooltipWidth = Math.min(tooltipWidth + 8, MAX_TOOLTIP_WIDTH);

        float tooltipHeight = tooltipLines.size() * LINE_HEIGHT + 8;

        float mainRight = mainX + mainWidth / 2 + 2;
        float mainLeft = mainX - mainWidth / 2 - 2;

        float tooltipX;
        boolean placeOnRight = (mainRight + TOOLTIP_GAP + tooltipWidth) < guiWidth;

        if (placeOnRight) {
            tooltipX = mainRight + TOOLTIP_GAP;
        } else {
            tooltipX = mainLeft - TOOLTIP_GAP - tooltipWidth;
        }

        float tooltipY = mainTop;

        int bgColor = (alpha * 48 / 255) << 24;
        int borderColor = (alpha << 24) | 0x5555FF;

        float tooltipLeft = tooltipX;
        float tooltipRight = tooltipX + tooltipWidth;
        float tooltipTop = tooltipY;
        float tooltipBottom = tooltipY + tooltipHeight;

        GuiUtil.fill(guiGraphics, tooltipLeft, tooltipTop, tooltipRight, tooltipBottom, bgColor);

        GuiUtil.fill(guiGraphics, tooltipLeft, tooltipTop - 1, tooltipRight, tooltipTop, borderColor);
        GuiUtil.fill(guiGraphics, tooltipLeft, tooltipBottom, tooltipRight, tooltipBottom + 1, borderColor);
        GuiUtil.fill(guiGraphics, tooltipLeft - 1, tooltipTop - 1, tooltipLeft, tooltipBottom + 1, borderColor);
        GuiUtil.fill(guiGraphics, tooltipRight, tooltipTop - 1, tooltipRight + 1, tooltipBottom + 1, borderColor);

        float textX = tooltipLeft + 4;
        float textY = tooltipTop + 4;
        for (Component line : tooltipLines) {
            int lineColor = (alpha << 24) | 0xCCCCCC;
            guiGraphics.drawString(font, line, (int) textX, (int) textY, lineColor, true);
            textY += LINE_HEIGHT;
        }
    }
}
