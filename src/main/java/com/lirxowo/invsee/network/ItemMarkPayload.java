package com.lirxowo.invsee.network;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.entity.ItemMarkEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

/**
 * 用于从客户端发送物品标记请求到服务器的网络包
 * isFromPlayerInventory: 是否来自玩家背包（如果是，则不高亮任何容器）
 * 容器位置由服务端从玩家的containerMenu中获取，解决客户端拿不到BlockEntity的问题
 */
public record ItemMarkPayload(boolean isFromPlayerInventory, ItemStack itemStack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ItemMarkPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Invsee.MODID, "item_mark"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemMarkPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ItemMarkPayload decode(RegistryFriendlyByteBuf buf) {
            boolean isFromPlayerInventory = buf.readBoolean();
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            return new ItemMarkPayload(isFromPlayerInventory, stack);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ItemMarkPayload payload) {
            buf.writeBoolean(payload.isFromPlayerInventory);
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
                // 删除该玩家之前的标记
                level.getAllEntities().forEach(entity -> {
                    if (entity instanceof ItemMarkEntity markEntity &&
                        markEntity.getOwnerName().equals(player.getName().getString())) {
                        entity.discard();
                    }
                });

                Vec3 markLocation;
                BlockPos containerPos = null;

                // 如果不是来自玩家背包，从服务端的containerMenu获取容器位置
                if (!payload.isFromPlayerInventory) {
                    containerPos = getContainerPosFromMenu(player);
                }

                // 设置标记位置
                if (containerPos != null) {
                    // 在容器方块上方生成标记
                    markLocation = Vec3.atCenterOf(containerPos).add(0, 1.5, 0);
                } else {
                    // 没有容器位置（背包等），在玩家位置生成
                    markLocation = player.position().add(0, 1.5, 0);
                }

                // 创建新标记实体
                ItemMarkEntity markEntity = new ItemMarkEntity(
                        level, player, markLocation, payload.itemStack, containerPos);
                level.addFreshEntity(markEntity);
            }
        });
    }

    /**
     * 从服务端玩家的containerMenu中获取容器方块位置
     * 服务端的Menu里有完整的BlockEntity信息，不像客户端可能只有SimpleContainer
     */
    @Nullable
    private static BlockPos getContainerPosFromMenu(Player player) {
        AbstractContainerMenu menu = player.containerMenu;

        // 如果是玩家背包界面（inventoryMenu），返回null
        if (menu == player.inventoryMenu) {
            return null;
        }

        // 遍历所有槽位，查找属于BlockEntity的容器
        for (Slot slot : menu.slots) {
            // 跳过玩家背包的槽位
            if (slot.container == player.getInventory()) {
                continue;
            }
            // 检查slot.container是否是BlockEntity
            if (slot.container instanceof BlockEntity blockEntity) {
                return blockEntity.getBlockPos();
            }
        }

        return null;
    }
}
