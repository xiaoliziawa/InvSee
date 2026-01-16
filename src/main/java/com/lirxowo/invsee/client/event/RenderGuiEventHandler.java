package com.lirxowo.invsee.client.event;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.client.recipe.RecipePanelInteractionHandler;
import com.lirxowo.invsee.client.recipe.RecipePanelRenderer;
import com.lirxowo.invsee.client.recipe.RecipePanelState;
import com.lirxowo.invsee.client.util.GuiUtil;
import com.lirxowo.invsee.client.util.ItemInfoHelper;
import com.lirxowo.invsee.compat.jei.JEICompat;
import com.lirxowo.invsee.config.InvseeConfig;
import com.lirxowo.invsee.entity.ItemMarkEntity;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.List;

@Mod.EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class RenderGuiEventHandler {
    private static final float FRAME_PROTECT = 10;
    private static final float ICON_WIDTH_WITH_MARGIN = 18;
    private static final float MIN_REF_WIDTH = 60;
    private static final int MAX_NAME_WIDTH = 150;
    private static final int LINE_HEIGHT = 10;
    private static final int TOOLTIP_GAP = 8;
    private static final int MAX_TOOLTIP_WIDTH = 200;

    private static final ResourceLocation JEI_ICON = ResourceLocation.fromNamespaceAndPath(Invsee.MODID, "textures/gui/jei_icon.png");
    private static final int JEI_ICON_SIZE = 10;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.level == null) {
            return;
        }

        Player player = mc.player;
        Font font = mc.font;
        Level level = mc.level;
        Window window = mc.getWindow();

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        Vector3f lookVec = camera.getLookVector();
        Vector3f upVec = camera.getUpVector();
        Vector3f leftVec = camera.getLeftVector();

        float fov = mc.options.fov().get().floatValue();
        float fovY = fov * Mth.DEG_TO_RAD;
        float tanHalfFovY = (float) Math.tan(fovY * 0.5);
        float tanHalfFovX = tanHalfFovY * (float) window.getWidth() / (float) window.getHeight();

        int guiWidth = guiGraphics.guiWidth();
        int guiHeight = guiGraphics.guiHeight();
        float guiScale = (float) window.getGuiScale();

        // Scale adjustment for high GUI scales
        float scaleFactor = 1.0F;
        int effectiveGuiWidth = guiWidth;
        int effectiveGuiHeight = guiHeight;

        if (guiScale > 3F) {
            scaleFactor = 3F / guiScale;
            effectiveGuiWidth = Math.round((float) window.getWidth() / 3F);
            effectiveGuiHeight = Math.round((float) window.getHeight() / 3F);
        }

        float markDisplayRange = InvseeConfig.getMarkDisplayRange();
        double markDisplayRangeSq = InvseeConfig.getMarkDisplayRangeSq();
        List<ItemMarkEntity> list = level.getEntitiesOfClass(
                ItemMarkEntity.class,
                player.getBoundingBox().inflate(markDisplayRange),
                e -> e.distanceToSqr(player) <= markDisplayRangeSq
        );

        if (list.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0F, 0F, 200F); // Z-layer for HUD elements

        if (scaleFactor != 1.0F) {
            poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
        }

        float partialTick = event.getPartialTick();

        for (ItemMarkEntity markEntity : list) {
            if (!markEntity.shouldBeVisibleTo(player)) {
                continue;
            }

            ItemStack itemStack = markEntity.getMarkedItem();
            if (itemStack.isEmpty()) continue;

            float lifeProgress = markEntity.getLifeProgress();
            int alpha = lifeProgress > 0.8f ? (int) ((1f - lifeProgress) / 0.2f * 255) : 255;
            if (alpha <= 0) continue;

            float bobOffset = Mth.sin(((float) markEntity.tickCount + partialTick) / 10.0F) * 0.1F + 0.1F;

            double entityX = Mth.lerp(partialTick, markEntity.xOld, markEntity.getX());
            double entityY = Mth.lerp(partialTick, markEntity.yOld, markEntity.getY()) + bobOffset + 0.8;
            double entityZ = Mth.lerp(partialTick, markEntity.zOld, markEntity.getZ());

            float relX = (float) (entityX - cameraPos.x);
            float relY = (float) (entityY - cameraPos.y);
            float relZ = (float) (entityZ - cameraPos.z);

            float viewZ = relX * lookVec.x + relY * lookVec.y + relZ * lookVec.z;
            float viewX = relX * leftVec.x + relY * leftVec.y + relZ * leftVec.z;
            float viewY = relX * upVec.x + relY * upVec.y + relZ * upVec.z;

            if (viewZ <= 0) {
                continue;
            }

            float ndcX = -viewX / (viewZ * tanHalfFovX);
            float ndcY = viewY / (viewZ * tanHalfFovY);

            float pixelX = (ndcX + 1.0f) * 0.5f * window.getWidth();
            float pixelY = (1.0f - ndcY) * 0.5f * window.getHeight();

            float screenX, screenY;
            if (scaleFactor != 1.0F) {
                screenX = pixelX / 3F;
                screenY = pixelY / 3F;
            } else {
                screenX = pixelX / guiScale;
                screenY = pixelY / guiScale;
            }

            Component itemName = itemStack.getHoverName();
            Component ownerName = Component.literal("[" + markEntity.getOwnerName() + "]");

            boolean showExtraInfo = InvseeConfig.isShowExtraInfo();
            List<Component> infoLines = showExtraInfo ? ItemInfoHelper.getItemInfoLines(itemStack) : List.of();
            List<Component> tooltipLines = InvseeConfig.isShowTooltip()
                    ? ItemInfoHelper.getItemTooltipLines(itemStack) : List.of();
            Rarity rarity = itemStack.getRarity();

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

            float xScreen = Mth.clamp(screenX, refWidth / 2 + FRAME_PROTECT, effectiveGuiWidth - (refWidth / 2 + FRAME_PROTECT));
            float yScreen = Mth.clamp(screenY, labelHeight / 2 + FRAME_PROTECT, effectiveGuiHeight - (labelHeight / 2 + FRAME_PROTECT));

            int bgColor = (alpha * 64 / 255) << 24;
            int borderColor = (alpha << 24) | ItemInfoHelper.getRarityColor(rarity);

            float bgTop = yScreen - 14;
            float bgBottom = yScreen + 10 + infoLines.size() * LINE_HEIGHT;

            if (JEICompat.isLoaded() && RecipePanelInteractionHandler.isLookingAt(markEntity)) {
                poseStack.pushPose();
                poseStack.scale(0.8F, 0.8F, 0.8F);
                Component hintText = Component.translatable("invsee.hint.view_recipe");
                float hintScaledX = xScreen * 1.25F;
                float hintScaledY = (bgTop - 10) * 1.25F;
                int hintColor = (alpha << 24) | 0x88FF88;
                GuiUtil.drawCenteredString(guiGraphics, font, hintText, hintScaledX, hintScaledY, hintColor);
                poseStack.popPose();
            }

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

            if (markEntity.isFromJEI() && JEICompat.isLoaded()) {
                float ownerWidth = font.width(ownerName);
                float iconSpacing = 2;
                float totalWidth = JEI_ICON_SIZE + iconSpacing + ownerWidth;
                float startX = scaledX - totalWidth / 2;

                poseStack.pushPose();
                poseStack.scale(1.25F, 1.25F, 1.25F);
                float iconX = startX / 1.25F;
                float iconY = scaledY / 1.25F - 1;
                float iconAlpha = alpha / 255.0F;
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, iconAlpha);
                guiGraphics.blit(JEI_ICON, (int) iconX, (int) iconY, 0, 0, JEI_ICON_SIZE, JEI_ICON_SIZE, JEI_ICON_SIZE, JEI_ICON_SIZE);
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();

                float textX = startX + JEI_ICON_SIZE + iconSpacing;
                guiGraphics.drawString(font, ownerName, (int) textX, (int) scaledY, ownerColor, true);
            } else {
                GuiUtil.drawCenteredString(guiGraphics, font, ownerName, scaledX, scaledY, ownerColor);
            }

            poseStack.popPose();

            if (InvseeConfig.isShowTooltip() && !tooltipLines.isEmpty()) {
                renderTooltipBox(guiGraphics, font, tooltipLines, xScreen, yScreen, refWidth, bgTop, bgBottom, alpha, effectiveGuiWidth);
            }

            if (RecipePanelState.isExpandedFor(markEntity)) {
                float animProgress = RecipePanelState.getSmoothedProgress(partialTick);
                RecipePanelRenderer.renderRecipePanelNextToLabel(
                        guiGraphics, mc,
                        xScreen, yScreen, refWidth,
                        bgTop, bgBottom,
                        alpha, animProgress
                );
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