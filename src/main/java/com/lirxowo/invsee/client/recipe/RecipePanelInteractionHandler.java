package com.lirxowo.invsee.client.recipe;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.compat.jei.JEICompat;
import com.lirxowo.invsee.config.InvseeConfig;
import com.lirxowo.invsee.entity.ItemMarkEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

import javax.annotation.Nullable;
import java.util.List;

@EventBusSubscriber(modid = Invsee.MODID, value = Dist.CLIENT)
public class RecipePanelInteractionHandler {

    @Nullable
    private static ItemMarkEntity lookedAtEntity = null;

    private static final double INTERACTION_RANGE = 32.0;

    private static final double HITBOX_SIZE = 0.8;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) {
            return;
        }

        updateLookedAtEntity(mc.player, mc.level);

        RecipePanelState.tick();

        if (RecipePanelState.isVisible()) {
            ItemMarkEntity targetEntity = findEntityById(mc.level, mc.player, RecipePanelState.getExpandedEntityId());
            if (targetEntity == null || !isLookingAt(targetEntity)) {
                RecipePanelState.closePanel();
            }
        }
    }

    @Nullable
    private static ItemMarkEntity findEntityById(Level level, Player player, @Nullable java.util.UUID uuid) {
        if (uuid == null) {
            return null;
        }
        float markDisplayRange = InvseeConfig.getMarkDisplayRange();
        List<ItemMarkEntity> marks = level.getEntitiesOfClass(
                ItemMarkEntity.class,
                player.getBoundingBox().inflate(markDisplayRange),
                e -> e.getUUID().equals(uuid)
        );
        return marks.isEmpty() ? null : marks.get(0);
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (event.getButton() != 1 || event.getAction() != 1) {
            return;
        }

        if (lookedAtEntity != null && JEICompat.isLoaded()) {
            if (!lookedAtEntity.shouldBeVisibleTo(mc.player)) {
                return;
            }

            RecipePanelState.togglePanel(lookedAtEntity);

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (lookedAtEntity == null || !RecipePanelState.isVisible()) {
            return;
        }

        if (!RecipePanelState.isExpandedFor(lookedAtEntity)) {
            return;
        }

        if (RecipePanelState.getTotalRecipes() <= 1) {
            return;
        }

        double scrollDelta = event.getScrollDeltaY();
        if (scrollDelta > 0) {
            RecipePanelState.previousRecipe();
            event.setCanceled(true);
        } else if (scrollDelta < 0) {
            RecipePanelState.nextRecipe();
            event.setCanceled(true);
        }
    }

    private static void updateLookedAtEntity(Player player, Level level) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(INTERACTION_RANGE));

        float markDisplayRange = InvseeConfig.getMarkDisplayRange();
        List<ItemMarkEntity> marks = level.getEntitiesOfClass(
                ItemMarkEntity.class,
                player.getBoundingBox().inflate(markDisplayRange),
                e -> e.distanceToSqr(player) <= markDisplayRange * markDisplayRange
                        && e.shouldBeVisibleTo(player)
        );

        ItemMarkEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (ItemMarkEntity mark : marks) {
            Vec3 markPos = mark.position().add(0, 0.5, 0);
            AABB hitbox = new AABB(
                    markPos.x - HITBOX_SIZE / 2, markPos.y - HITBOX_SIZE / 2, markPos.z - HITBOX_SIZE / 2,
                    markPos.x + HITBOX_SIZE / 2, markPos.y + HITBOX_SIZE / 2, markPos.z + HITBOX_SIZE / 2
            );

            var intersection = hitbox.clip(eyePos, endPos);
            if (intersection.isPresent()) {
                double dist = eyePos.distanceToSqr(intersection.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = mark;
                }
            }
        }

        lookedAtEntity = closest;
    }

    @Nullable
    public static ItemMarkEntity getLookedAtEntity() {
        return lookedAtEntity;
    }

    public static boolean isLookingAt(ItemMarkEntity entity) {
        return entity.equals(lookedAtEntity);
    }
}
