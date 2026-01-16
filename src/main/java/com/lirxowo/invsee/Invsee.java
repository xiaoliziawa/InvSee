package com.lirxowo.invsee;

import com.lirxowo.invsee.api.builtin.BuiltinItemInfoProviders;
import com.lirxowo.invsee.api.builtin.ModCompatProviders;
import com.lirxowo.invsee.client.renderer.ItemMarkRenderer;
import com.lirxowo.invsee.config.InvseeConfig;
import com.lirxowo.invsee.registry.EntityRegister;
import com.lirxowo.invsee.registry.NetworkRegister;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Invsee.MODID)
public class Invsee {
    public static final String MODID = "invsee";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Invsee() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EntityRegister.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, InvseeConfig.SPEC);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("InvSee mod initialized!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkRegister.register();

        event.enqueueWork(() -> {
            BuiltinItemInfoProviders.registerAll();
            LOGGER.info("InvSee builtin providers registered!");
            ModCompatProviders.registerAll();
            LOGGER.info("InvSee mod compat providers registered!");
        });
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntityRegister.ITEM_MARK.get(), ItemMarkRenderer::new);
        }
    }
}
