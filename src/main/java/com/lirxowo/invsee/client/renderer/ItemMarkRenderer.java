package com.lirxowo.invsee.client.renderer;

import com.lirxowo.invsee.entity.ItemMarkEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;

import java.util.OptionalDouble;

public class ItemMarkRenderer extends EntityRenderer<ItemMarkEntity> {
    private final ItemRenderer itemRenderer;

    private static final RenderType LINES_SEE_THROUGH = RenderType.create(
            "invsee_lines_see_through",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2.0)))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .createCompositeState(false)
    );

    public ItemMarkRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.0F;
    }

    @Override
    protected int getBlockLightLevel(ItemMarkEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(ItemMarkEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(ItemMarkEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Check team visibility before rendering
        Minecraft mc = Minecraft.getInstance();
        Player localPlayer = mc.player;
        if (localPlayer != null && !entity.shouldBeVisibleTo(localPlayer)) {
            return; // Don't render if not visible to this player
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        ItemStack itemStack = entity.getMarkedItem();
        if (itemStack.isEmpty()) return;

        float lifeProgress = entity.getLifeProgress();
        float alpha = lifeProgress > 0.8f ? (1f - lifeProgress) / 0.2f : 1f;

        BlockPos containerPos = entity.getContainerPos();
        if (containerPos != null) {
            renderContainerOutline(poseStack, bufferSource, entity, containerPos, alpha);
        }

        float bobOffset = Mth.sin(((float) entity.tickCount + partialTick) / 10.0F) * 0.1F + 0.1F;

        poseStack.pushPose();
        poseStack.translate(0.0F, bobOffset + 0.25F, 0.0F);

        float spin = ((float) entity.tickCount + partialTick) / 20.0F;
        poseStack.mulPose(Axis.YP.rotation(spin));

        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, entity.level(), null, entity.getId());
        this.itemRenderer.render(
                itemStack,
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                bufferSource,
                15728880,
                OverlayTexture.NO_OVERLAY,
                bakedModel
        );

        poseStack.popPose();
    }

    private void renderContainerOutline(PoseStack poseStack, MultiBufferSource bufferSource,
                                        ItemMarkEntity entity, BlockPos containerPos, float alpha) {
        Vec3 entityPos = entity.position();
        double offsetX = containerPos.getX() - entityPos.x;
        double offsetY = containerPos.getY() - entityPos.y;
        double offsetZ = containerPos.getZ() - entityPos.z;

        poseStack.pushPose();
        poseStack.translate(offsetX, offsetY, offsetZ);

        AABB box = new AABB(0, 0, 0, 1, 1, 1);
        float r = 1.0f, g = 0.84f, b = 0.0f;

        VertexConsumer lineBuffer = bufferSource.getBuffer(LINES_SEE_THROUGH);
        LevelRenderer.renderLineBox(poseStack, lineBuffer, box, r, g, b, alpha);

        poseStack.popPose();
    }
}
