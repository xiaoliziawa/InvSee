package com.lirxowo.invsee.compat.jei;

import com.lirxowo.invsee.Invsee;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class InvseeJEIPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Invsee.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JEIRuntimeHelper.setRuntime(jeiRuntime);
        Invsee.LOGGER.info("InvSee JEI integration initialized");
    }

    @Override
    public void onRuntimeUnavailable() {
        JEIRuntimeHelper.setRuntime(null);
        Invsee.LOGGER.info("InvSee JEI integration unloaded");
    }
}
