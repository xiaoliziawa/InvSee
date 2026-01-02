package com.lirxowo.invsee;

import com.lirxowo.invsee.client.renderer.ItemMarkRenderer;
import com.lirxowo.invsee.registry.EntityRegister;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;

@Mod(Invsee.MODID)
public class Invsee {
    public static final String MODID = "invsee";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Invsee(IEventBus modEventBus) {
        // 注册实体类型
        EntityRegister.register(modEventBus);

        LOGGER.info("InvSee mod initialized!");
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntityRegister.ITEM_MARK.get(), ItemMarkRenderer::new);
        }
    }
}
