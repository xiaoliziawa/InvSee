package com.lirxowo.invsee.network;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.entity.ItemMarkEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 用于从客户端发送物品标记请求到服务器的网络包
 * hasContainer为true时containerPos有效，否则在玩家位置生成
 */
public record ItemMarkPayload(boolean hasContainer, BlockPos containerPos, ItemStack itemStack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ItemMarkPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Invsee.MODID, "item_mark"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemMarkPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ItemMarkPayload decode(RegistryFriendlyByteBuf buf) {
            boolean hasContainer = buf.readBoolean();
            BlockPos pos = hasContainer ? buf.readBlockPos() : BlockPos.ZERO;
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            return new ItemMarkPayload(hasContainer, pos, stack);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ItemMarkPayload payload) {
            buf.writeBoolean(payload.hasContainer);
            if (payload.hasContainer) {
                buf.writeBlockPos(payload.containerPos);
            }
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.itemStack);
        }
    };

    // 便捷构造器 - 有容器位置
    public static ItemMarkPayload withContainer(BlockPos containerPos, ItemStack itemStack) {
        return new ItemMarkPayload(true, containerPos, itemStack);
    }

    // 便捷构造器 - 无容器位置
    public static ItemMarkPayload withoutContainer(ItemStack itemStack) {
        return new ItemMarkPayload(false, BlockPos.ZERO, itemStack);
    }

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

                // 检查是否有有效的容器位置
                if (payload.hasContainer) {
                    BlockPos pos = payload.containerPos;
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    // 只有真正的容器方块才在上方生成标记
                    if (blockEntity instanceof net.minecraft.world.Container) {
                        markLocation = Vec3.atCenterOf(pos).add(0, 1.5, 0);
                        containerPos = pos;
                    } else {
                        // 不是容器方块，在玩家位置生成
                        markLocation = player.position().add(0, 1.5, 0);
                    }
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
}
