package com.lirxowo.invsee.network;

import com.lirxowo.invsee.client.tracking.TrackingList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SlotHighlightSyncPacket {

    private final ItemStack itemStack;
    private final long gameTime;
    private final UUID senderUUID;

    public SlotHighlightSyncPacket(ItemStack itemStack, long gameTime, UUID senderUUID) {
        this.itemStack = itemStack;
        this.gameTime = gameTime;
        this.senderUUID = senderUUID;
    }

    public static void encode(SlotHighlightSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.itemStack);
        buf.writeLong(msg.gameTime);
        buf.writeUUID(msg.senderUUID);
    }

    public static SlotHighlightSyncPacket decode(FriendlyByteBuf buf) {
        ItemStack stack = buf.readItem();
        long gameTime = buf.readLong();
        UUID senderUUID = buf.readUUID();
        return new SlotHighlightSyncPacket(stack, gameTime, senderUUID);
    }

    public static void handle(SlotHighlightSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                TrackingList.startTracking(msg.itemStack, msg.gameTime);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
