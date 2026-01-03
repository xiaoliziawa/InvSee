package com.lirxowo.invsee.network;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.compat.ftbteams.FTBTeamsCompat;
import com.lirxowo.invsee.config.InvseeConfig;
import com.lirxowo.invsee.entity.ItemMarkEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ItemMarkPayload(MarkSource source, ItemStack itemStack) implements CustomPacketPayload {

    /**
     * Source of the marked item
     */
    public enum MarkSource {
        PLAYER_INVENTORY,  // From player's own inventory
        CONTAINER,         // From a container (chest, etc.)
        VIRTUAL            // From JEI or other virtual sources (not a real container)
    }

    public static final CustomPacketPayload.Type<ItemMarkPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Invsee.MODID, "item_mark"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemMarkPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ItemMarkPayload decode(RegistryFriendlyByteBuf buf) {
            byte sourceOrdinal = buf.readByte();
            MarkSource source = MarkSource.values()[sourceOrdinal];
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            return new ItemMarkPayload(source, stack);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ItemMarkPayload payload) {
            buf.writeByte(payload.source.ordinal());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.itemStack);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleDataInServer(final ItemMarkPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.level() instanceof ServerLevel level) {
                List<ItemMarkEntity> toRemove = new ArrayList<>();
                for (var entity : level.getAllEntities()) {
                    if (entity instanceof ItemMarkEntity markEntity &&
                        markEntity.getOwnerName().equals(player.getName().getString())) {
                        toRemove.add(markEntity);
                    }
                }
                // Now safely remove them
                for (ItemMarkEntity entity : toRemove) {
                    entity.discard();
                }

                Vec3 markLocation;
                BlockPos containerPos = null;

                // Only try to get container position if source is CONTAINER
                if (payload.source == MarkSource.CONTAINER) {
                    containerPos = getContainerPosFromMenu(player);
                }

                if (containerPos != null) {
                    markLocation = Vec3.atCenterOf(containerPos).add(0, 1.5, 0);
                } else {
                    markLocation = player.position().add(0, 1.5, 0);
                }

                ItemMarkEntity markEntity = new ItemMarkEntity(
                        level, player, markLocation, payload.itemStack, containerPos);
                level.addFreshEntity(markEntity);

                // Broadcast slot highlight to players based on team visibility
                double syncRange = InvseeConfig.getMarkDisplayRange();
                SlotHighlightSyncPayload syncPayload = new SlotHighlightSyncPayload(
                        payload.itemStack, level.getGameTime(), player.getUUID());

                UUID senderUUID = player.getUUID();

                // Get all players in range and filter by team visibility
                for (ServerPlayer targetPlayer : level.getServer().getPlayerList().getPlayers()) {
                    if (targetPlayer.level() != level) {
                        continue;
                    }

                    double distanceSq = targetPlayer.distanceToSqr(player.getX(), player.getY(), player.getZ());
                    if (distanceSq > syncRange * syncRange) {
                        continue;
                    }

                    // Check team visibility
                    if (FTBTeamsCompat.canSeeMarks(senderUUID, targetPlayer.getUUID())) {
                        PacketDistributor.sendToPlayer(targetPlayer, syncPayload);
                    }
                }
            }
        });
    }

    @Nullable
    private static BlockPos getContainerPosFromMenu(Player player) {
        AbstractContainerMenu menu = player.containerMenu;

        if (menu == player.inventoryMenu) {
            return null;
        }

        for (Slot slot : menu.slots) {
            if (slot.container == player.getInventory()) {
                continue;
            }
            if (slot.container instanceof BlockEntity blockEntity) {
                return blockEntity.getBlockPos();
            }
        }

        BlockPos pos = getBlockPosFromMenuClass(menu);
        if (pos != null) {
            return pos;
        }

        return null;
    }

    @Nullable
    private static BlockPos getBlockPosFromMenuClass(AbstractContainerMenu menu) {
        Class<?> clazz = menu.getClass();

        while (clazz != null && clazz != AbstractContainerMenu.class) {
            BlockPos pos = tryGetBlockPosFromMethod(menu, clazz, "getBlockEntity");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromMethod(menu, clazz, "getTileEntity");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromMethod(menu, clazz, "getBlockPos");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromMethod(menu, clazz, "getPos");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromField(menu, clazz, "blockEntity");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromField(menu, clazz, "tileEntity");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromField(menu, clazz, "tile");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromField(menu, clazz, "be");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromField(menu, clazz, "te");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromField(menu, clazz, "part");
            if (pos != null) return pos;

            pos = tryFindBlockEntityField(menu, clazz);
            if (pos != null) return pos;

            clazz = clazz.getSuperclass();
        }

        return null;
    }

    @Nullable
    private static BlockPos tryGetBlockPosFromMethod(AbstractContainerMenu menu, Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(menu);

            if (result instanceof BlockEntity blockEntity) {
                return blockEntity.getBlockPos();
            } else if (result instanceof BlockPos blockPos) {
                return blockPos;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    private static BlockPos tryGetBlockPosFromField(AbstractContainerMenu menu, Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(menu);

            if (value == null) {
                return null;
            }

            if (value instanceof BlockEntity blockEntity) {
                return blockEntity.getBlockPos();
            } else if (value instanceof BlockPos blockPos) {
                return blockPos;
            }

            BlockPos pos = tryGetBlockEntityFromObject(value);
            if (pos != null) return pos;

        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    private static BlockPos tryGetBlockEntityFromObject(Object obj) {
        if (obj == null) return null;

        try {
            Method getBlockEntity = obj.getClass().getMethod("getBlockEntity");
            Object result = getBlockEntity.invoke(obj);
            if (result instanceof BlockEntity blockEntity) {
                return blockEntity.getBlockPos();
            }
        } catch (Exception ignored) {
        }

        try {
            Method getBlockPos = obj.getClass().getMethod("getBlockPos");
            Object result = getBlockPos.invoke(obj);
            if (result instanceof BlockPos blockPos) {
                return blockPos;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    @Nullable
    private static BlockPos tryFindBlockEntityField(AbstractContainerMenu menu, Class<?> clazz) {
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(menu);

                if (value == null) continue;

                if (value instanceof BlockEntity blockEntity) {
                    return blockEntity.getBlockPos();
                }

                BlockPos pos = tryGetBlockEntityFromObject(value);
                if (pos != null) return pos;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
