package com.lirxowo.invsee.api.builtin;

import com.lirxowo.invsee.api.InvseeAPI;
import com.lirxowo.invsee.api.provider.IItemInfoProvider;
import com.lirxowo.invsee.compat.ae2.AE2Compat;
import com.lirxowo.invsee.compat.mekanism.MekanismCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModCompatProviders {

    public static void registerAll() {
        if (MekanismCompat.isLoaded()) {
            InvseeAPI.registerItemInfoProvider(new MekanismChemicalProvider());
        }

        if (AE2Compat.isLoaded()) {
            InvseeAPI.registerItemInfoProvider(new AE2StorageCellProvider());
        }
    }

    public static class MekanismChemicalProvider implements IItemInfoProvider {
        @Override
        public String getProviderId() {
            return "invsee:mekanism_chemical";
        }

        @Override
        public boolean canProvideInfo(ItemStack stack) {
            if (!MekanismCompat.isLoaded()) return false;
            try {
                return !MekanismCompat.getChemicalInfo(stack).isEmpty();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public List<Component> getInfoLines(ItemStack stack) {
            if (!MekanismCompat.isLoaded()) return new ArrayList<>();
            try {
                return MekanismCompat.getChemicalInfo(stack);
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        @Override
        public int getPriority() {
            return 500;
        }

        @Override
        public String getDisplayName() {
            return "Mekanism Chemical Info";
        }
    }

    public static class AE2StorageCellProvider implements IItemInfoProvider {
        @Override
        public String getProviderId() {
            return "invsee:ae2_storage_cell";
        }

        @Override
        public boolean canProvideInfo(ItemStack stack) {
            if (!AE2Compat.isLoaded()) return false;
            try {
                return !AE2Compat.getStorageCellInfo(stack).isEmpty();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public List<Component> getInfoLines(ItemStack stack) {
            if (!AE2Compat.isLoaded()) return new ArrayList<>();
            try {
                return AE2Compat.getStorageCellInfo(stack);
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        @Override
        public int getPriority() {
            return 500;
        }

        @Override
        public String getDisplayName() {
            return "AE2 Storage Cell Info";
        }
    }
}
