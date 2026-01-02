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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
     *
     * 使用多种策略来获取容器位置：
     * 1. 从Slot的Container检查BlockEntity
     * 2. 从Menu类本身获取BlockEntity字段/方法（支持AE2、Mekanism等模组）
     * 3. 通过反射查找常见的BlockEntity字段名
     */
    @Nullable
    private static BlockPos getContainerPosFromMenu(Player player) {
        AbstractContainerMenu menu = player.containerMenu;

        // 如果是玩家背包界面（inventoryMenu），返回null
        if (menu == player.inventoryMenu) {
            return null;
        }

        // 策略1: 遍历所有槽位，查找属于BlockEntity的容器
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

        // 策略2: 尝试从Menu类直接获取BlockEntity
        BlockPos pos = getBlockPosFromMenuClass(menu);
        if (pos != null) {
            return pos;
        }

        return null;
    }

    /**
     * 通过反射从Menu类获取BlockEntity/BlockPos
     * 支持多种模组的Container实现
     */
    @Nullable
    private static BlockPos getBlockPosFromMenuClass(AbstractContainerMenu menu) {
        Class<?> clazz = menu.getClass();

        // 遍历类层次结构
        while (clazz != null && clazz != AbstractContainerMenu.class) {
            // 尝试常见的方法名
            BlockPos pos = tryGetBlockPosFromMethod(menu, clazz, "getBlockEntity");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromMethod(menu, clazz, "getTileEntity");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromMethod(menu, clazz, "getBlockPos");
            if (pos != null) return pos;

            pos = tryGetBlockPosFromMethod(menu, clazz, "getPos");
            if (pos != null) return pos;

            // 尝试常见的字段名
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

            // AE2 Part 支持 - part 字段包含 getBlockEntity() 方法
            pos = tryGetBlockPosFromField(menu, clazz, "part");
            if (pos != null) return pos;

            // 遍历所有字段查找BlockEntity类型或有getBlockEntity方法的对象
            pos = tryFindBlockEntityField(menu, clazz);
            if (pos != null) return pos;

            clazz = clazz.getSuperclass();
        }

        return null;
    }

    /**
     * 尝试通过方法名获取BlockPos
     */
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
            // 方法不存在或调用失败，继续尝试其他方式
        }
        return null;
    }

    /**
     * 尝试通过字段名获取BlockPos
     * 支持：BlockEntity、BlockPos、以及任何有 getBlockEntity() 方法的对象（如 AE2 的 IPart）
     */
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

            // 尝试调用对象的 getBlockEntity() 方法（支持 AE2 IPart 等）
            BlockPos pos = tryGetBlockEntityFromObject(value);
            if (pos != null) return pos;

        } catch (Exception ignored) {
            // 字段不存在或访问失败，继续尝试其他方式
        }
        return null;
    }

    /**
     * 尝试从任意对象获取 BlockEntity 的位置
     * 通过调用 getBlockEntity() 方法
     */
    @Nullable
    private static BlockPos tryGetBlockEntityFromObject(Object obj) {
        if (obj == null) return null;

        try {
            // 尝试调用 getBlockEntity() 方法
            Method getBlockEntity = obj.getClass().getMethod("getBlockEntity");
            Object result = getBlockEntity.invoke(obj);
            if (result instanceof BlockEntity blockEntity) {
                return blockEntity.getBlockPos();
            }
        } catch (Exception ignored) {
            // 方法不存在或调用失败
        }

        try {
            // 尝试调用 getBlockPos() 方法
            Method getBlockPos = obj.getClass().getMethod("getBlockPos");
            Object result = getBlockPos.invoke(obj);
            if (result instanceof BlockPos blockPos) {
                return blockPos;
            }
        } catch (Exception ignored) {
            // 方法不存在或调用失败
        }

        return null;
    }

    /**
     * 遍历类的所有字段，查找BlockEntity类型的字段，
     * 或者有 getBlockEntity() 方法的字段（如 AE2 IPart）
     */
    @Nullable
    private static BlockPos tryFindBlockEntityField(AbstractContainerMenu menu, Class<?> clazz) {
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(menu);

                if (value == null) continue;

                // 检查字段类型是否是BlockEntity或其子类
                if (value instanceof BlockEntity blockEntity) {
                    return blockEntity.getBlockPos();
                }

                // 检查字段是否有 getBlockEntity() 方法（支持 AE2 IPart 等）
                BlockPos pos = tryGetBlockEntityFromObject(value);
                if (pos != null) return pos;
            }
        } catch (Exception ignored) {
            // 访问失败，返回null
        }
        return null;
    }
}
