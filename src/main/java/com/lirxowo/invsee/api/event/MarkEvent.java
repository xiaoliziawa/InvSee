package com.lirxowo.invsee.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class MarkEvent {

    public enum Type {
        ITEM_MARKED,
        TRACKING_STARTED,
        TRACKING_ENDED,
        ENTITY_CREATED,
        ENTITY_REMOVED
    }

    private final Type type;
    private final ItemStack itemStack;
    private final Player player;
    private final BlockPos containerPos;
    private final boolean isFromPlayerInventory;
    private final long gameTime;
    private boolean canceled = false;

    public MarkEvent(Type type, ItemStack itemStack, Player player,
                     @Nullable BlockPos containerPos, boolean isFromPlayerInventory, long gameTime) {
        this.type = type;
        this.itemStack = itemStack.copy();
        this.player = player;
        this.containerPos = containerPos;
        this.isFromPlayerInventory = isFromPlayerInventory;
        this.gameTime = gameTime;
    }

    public Type getType() {
        return type;
    }

    public ItemStack getItemStack() {
        return itemStack.copy();
    }

    public Player getPlayer() {
        return player;
    }

    @Nullable
    public BlockPos getContainerPos() {
        return containerPos;
    }

    public boolean isFromPlayerInventory() {
        return isFromPlayerInventory;
    }

    public long getGameTime() {
        return gameTime;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        if (type == Type.ITEM_MARKED) {
            this.canceled = canceled;
        }
    }

    public static MarkEvent itemMarked(ItemStack stack, Player player,
                                        @Nullable BlockPos containerPos, boolean fromInventory) {
        return new MarkEvent(Type.ITEM_MARKED, stack, player, containerPos, fromInventory,
                player.level().getGameTime());
    }

    public static MarkEvent trackingStarted(ItemStack stack, Player player, long gameTime) {
        return new MarkEvent(Type.TRACKING_STARTED, stack, player, null, false, gameTime);
    }

    public static MarkEvent trackingEnded(ItemStack stack, Player player, long gameTime) {
        return new MarkEvent(Type.TRACKING_ENDED, stack, player, null, false, gameTime);
    }

    public static MarkEvent entityCreated(ItemStack stack, Player player,
                                           @Nullable BlockPos containerPos, boolean fromInventory) {
        return new MarkEvent(Type.ENTITY_CREATED, stack, player, containerPos, fromInventory,
                player.level().getGameTime());
    }

    public static MarkEvent entityRemoved(ItemStack stack, Player player, long gameTime) {
        return new MarkEvent(Type.ENTITY_REMOVED, stack, player, null, false, gameTime);
    }
}
