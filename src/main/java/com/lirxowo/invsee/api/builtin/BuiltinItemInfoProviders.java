package com.lirxowo.invsee.api.builtin;

import com.lirxowo.invsee.api.InvseeAPI;
import com.lirxowo.invsee.api.provider.IItemInfoProvider;
import com.lirxowo.invsee.client.util.ItemInfoHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;

public class BuiltinItemInfoProviders {

    public static void registerAll() {
        InvseeAPI.registerItemInfoProvider(new RarityProvider());
        InvseeAPI.registerItemInfoProvider(new DurabilityProvider());
        InvseeAPI.registerItemInfoProvider(new EnchantmentProvider());
        InvseeAPI.registerItemInfoProvider(new EnergyProvider());
        InvseeAPI.registerItemInfoProvider(new FluidProvider());
        InvseeAPI.registerItemInfoProvider(new PotionProvider());
        InvseeAPI.registerItemInfoProvider(new CountProvider());
    }

    public static class RarityProvider implements IItemInfoProvider {
        @Override
        public String getProviderId() {
            return "invsee:rarity";
        }

        @Override
        public boolean canProvideInfo(ItemStack stack) {
            return stack.getRarity() != Rarity.COMMON;
        }

        @Override
        public List<Component> getInfoLines(ItemStack stack) {
            List<Component> lines = new ArrayList<>();
            Rarity rarity = stack.getRarity();
            String name = switch (rarity) {
                case UNCOMMON -> "Uncommon";
                case RARE -> "Rare";
                case EPIC -> "Epic";
                default -> rarity.name();
            };
            lines.add(Component.literal("✦ " + name).withStyle(rarity.getStyleModifier()));
            return lines;
        }

        @Override
        public int getPriority() {
            return 1000;
        }
    }

    public static class DurabilityProvider implements IItemInfoProvider {
        @Override
        public String getProviderId() {
            return "invsee:durability";
        }

        @Override
        public boolean canProvideInfo(ItemStack stack) {
            return stack.isDamageableItem();
        }

        @Override
        public List<Component> getInfoLines(ItemStack stack) {
            List<Component> lines = new ArrayList<>();
            int current = stack.getMaxDamage() - stack.getDamageValue();
            int max = stack.getMaxDamage();
            float percent = (float) current / max;
            ChatFormatting color = getDurabilityColor(percent);
            lines.add(Component.translatable("item.durability", current, max).withStyle(color));
            return lines;
        }

        private ChatFormatting getDurabilityColor(float percent) {
            if (percent > 0.75f) return ChatFormatting.GREEN;
            if (percent > 0.5f) return ChatFormatting.YELLOW;
            if (percent > 0.25f) return ChatFormatting.GOLD;
            return ChatFormatting.RED;
        }

        @Override
        public int getPriority() {
            return 950;
        }
    }

    public static class EnchantmentProvider implements IItemInfoProvider {
        @Override
        public String getProviderId() {
            return "invsee:enchantment";
        }

        @Override
        public boolean canProvideInfo(ItemStack stack) {
            return !stack.getEnchantments().isEmpty();
        }

        @Override
        public List<Component> getInfoLines(ItemStack stack) {
            List<Component> lines = new ArrayList<>();
            ItemEnchantments enchantments = stack.getEnchantments();

            List<Component> enchantList = new ArrayList<>();
            for (Holder<Enchantment> holder : enchantments.keySet()) {
                int level = enchantments.getLevel(holder);
                if (level > 0) {
                    enchantList.add(Enchantment.getFullname(holder, level));
                }
            }

            if (enchantList.size() <= 3) {
                lines.addAll(enchantList);
            } else {
                for (int i = 0; i < 3; i++) {
                    lines.add(enchantList.get(i));
                }
                lines.add(Component.literal("+" + (enchantList.size() - 3) + " more...").withStyle(ChatFormatting.GRAY));
            }

            return lines;
        }

        @Override
        public int getPriority() {
            return 900;
        }
    }

    public static class EnergyProvider implements IItemInfoProvider {
        @Override
        public String getProviderId() {
            return "invsee:energy";
        }

        @Override
        public boolean canProvideInfo(ItemStack stack) {
            return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
        }

        @Override
        public List<Component> getInfoLines(ItemStack stack) {
            List<Component> lines = new ArrayList<>();
            IEnergyStorage energyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energyStorage != null) {
                int stored = energyStorage.getEnergyStored();
                int max = energyStorage.getMaxEnergyStored();
                float percent = max > 0 ? (float) stored / max : 0;
                ChatFormatting color = getEnergyColor(percent);
                lines.add(Component.literal("⚡ " + ItemInfoHelper.formatNumber(stored) + " / " +
                        ItemInfoHelper.formatNumber(max) + " FE").withStyle(color));
            }
            return lines;
        }

        private ChatFormatting getEnergyColor(float percent) {
            if (percent > 0.75f) return ChatFormatting.GREEN;
            if (percent > 0.5f) return ChatFormatting.YELLOW;
            if (percent > 0.25f) return ChatFormatting.GOLD;
            return ChatFormatting.RED;
        }

        @Override
        public int getPriority() {
            return 800;
        }
    }

    public static class FluidProvider implements IItemInfoProvider {
        @Override
        public String getProviderId() {
            return "invsee:fluid";
        }

        @Override
        public boolean canProvideInfo(ItemStack stack) {
            return stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
        }

        @Override
        public List<Component> getInfoLines(ItemStack stack) {
            List<Component> lines = new ArrayList<>();
            IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (fluidHandler != null) {
                int tanks = fluidHandler.getTanks();
                for (int i = 0; i < tanks; i++) {
                    FluidStack fluidStack = fluidHandler.getFluidInTank(i);
                    int capacity = fluidHandler.getTankCapacity(i);
                    if (capacity > 0) {
                        if (fluidStack.isEmpty()) {
                            lines.add(Component.literal("💧 Empty / " + ItemInfoHelper.formatNumber(capacity) + " mB")
                                    .withStyle(ChatFormatting.GRAY));
                        } else {
                            int amount = fluidStack.getAmount();
                            float percent = (float) amount / capacity;
                            ChatFormatting color = getFluidColor(percent);
                            Component fluidName = fluidStack.getHoverName();
                            lines.add(Component.literal("💧 ").withStyle(color)
                                    .append(fluidName)
                                    .append(Component.literal(" " + ItemInfoHelper.formatNumber(amount) +
                                            " / " + ItemInfoHelper.formatNumber(capacity) + " mB").withStyle(color)));
                        }
                    }
                }
            }
            return lines;
        }

        private ChatFormatting getFluidColor(float percent) {
            if (percent > 0.75f) return ChatFormatting.AQUA;
            if (percent > 0.5f) return ChatFormatting.BLUE;
            if (percent > 0.25f) return ChatFormatting.DARK_AQUA;
            return ChatFormatting.GRAY;
        }

        @Override
        public int getPriority() {
            return 700;
        }
    }

    public static class PotionProvider implements IItemInfoProvider {
        @Override
        public String getProviderId() {
            return "invsee:potion";
        }

        @Override
        public boolean canProvideInfo(ItemStack stack) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            return contents != null && contents.hasEffects();
        }

        @Override
        public List<Component> getInfoLines(ItemStack stack) {
            List<Component> lines = new ArrayList<>();
            PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
            if (potionContents != null) {
                float tickRate = 20.0F;
                for (MobEffectInstance effect : potionContents.getAllEffects()) {
                    Component effectName = Component.translatable(effect.getDescriptionId());
                    if (effect.getAmplifier() > 0) {
                        effectName = Component.translatable("potion.withAmplifier", effectName,
                                Component.translatable("potion.potency." + effect.getAmplifier()));
                    }
                    if (!effect.endsWithin(20)) {
                        Component duration = MobEffectUtil.formatDuration(effect, 1.0F, tickRate);
                        effectName = Component.translatable("potion.withDuration", effectName, duration);
                    }
                    ChatFormatting color = effect.getEffect().value().getCategory().getTooltipFormatting();
                    lines.add(Component.literal("⚗ ").withStyle(color).append(effectName.copy().withStyle(color)));
                }
            }
            return lines;
        }

        @Override
        public int getPriority() {
            return 600;
        }
    }

    public static class CountProvider implements IItemInfoProvider {
        @Override
        public String getProviderId() {
            return "invsee:count";
        }

        @Override
        public boolean canProvideInfo(ItemStack stack) {
            return stack.getCount() > 1;
        }

        @Override
        public List<Component> getInfoLines(ItemStack stack) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal("x" + stack.getCount()).withStyle(ChatFormatting.YELLOW));
            return lines;
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }
}
