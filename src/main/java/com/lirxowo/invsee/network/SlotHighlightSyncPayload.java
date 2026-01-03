package com.lirxowo.invsee.network;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.client.tracking.TrackingList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SlotHighlightSyncPayload(ItemStack itemStack, long gameTime, UUID senderUUID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SlotHighlightSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Invsee.MODID, "slot_highlight_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotHighlightSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SlotHighlightSyncPayload decode(RegistryFriendlyByteBuf buf) {
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            long gameTime = buf.readLong();
            UUID senderUUID = buf.readUUID();
            return new SlotHighlightSyncPayload(stack, gameTime, senderUUID);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SlotHighlightSyncPayload payload) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.itemStack);
            buf.writeLong(payload.gameTime);
            buf.writeUUID(payload.senderUUID);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleDataOnClient(final SlotHighlightSyncPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                // Team filtering is already done server-side in ItemMarkPayload.handleDataInServer
                // Only players who should see the mark will receive this packet
                TrackingList.startTracking(payload.itemStack, payload.gameTime);
            }
        });
    }
}
