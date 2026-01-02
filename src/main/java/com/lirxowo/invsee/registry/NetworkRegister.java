package com.lirxowo.invsee.registry;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.network.ItemMarkPayload;
import com.lirxowo.invsee.network.SlotHighlightSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Invsee.MODID)
public class NetworkRegister {
    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ItemMarkPayload.TYPE,
                ItemMarkPayload.STREAM_CODEC,
                ItemMarkPayload::handleDataInServer
        );
        registrar.playToClient(
                SlotHighlightSyncPayload.TYPE,
                SlotHighlightSyncPayload.STREAM_CODEC,
                SlotHighlightSyncPayload::handleDataOnClient
        );
    }
}
