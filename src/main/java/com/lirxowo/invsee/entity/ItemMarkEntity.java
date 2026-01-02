package com.lirxowo.invsee.entity;

import com.lirxowo.invsee.config.InvseeConfig;
import com.lirxowo.invsee.registry.EntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ItemMarkEntity extends Entity {
    private static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<ItemStack> MARKED_ITEM = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Optional<BlockPos>> CONTAINER_POS = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private int timer = 0;

    public ItemMarkEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public ItemMarkEntity(Level level, Player owner, Vec3 location, ItemStack itemStack, BlockPos containerPos) {
        this(EntityRegister.ITEM_MARK.get(), level);
        this.setPos(location);
        this.entityData.set(OWNER_NAME, owner.getName().getString());
        this.entityData.set(MARKED_ITEM, itemStack.copy());
        this.entityData.set(CONTAINER_POS, Optional.ofNullable(containerPos));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_NAME, "");
        builder.define(MARKED_ITEM, ItemStack.EMPTY);
        builder.define(CONTAINER_POS, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();
        timer++;
        if (!this.level().isClientSide) {
            if (timer >= InvseeConfig.getMarkDurationTicks()) {
                this.discard();
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("OwnerName")) {
            this.entityData.set(OWNER_NAME, compound.getString("OwnerName"));
        }
        if (compound.contains("MarkedItem")) {
            this.entityData.set(MARKED_ITEM, ItemStack.parseOptional(
                    this.registryAccess(), compound.getCompound("MarkedItem")));
        }
        if (compound.contains("ContainerX")) {
            BlockPos pos = new BlockPos(
                    compound.getInt("ContainerX"),
                    compound.getInt("ContainerY"),
                    compound.getInt("ContainerZ")
            );
            this.entityData.set(CONTAINER_POS, Optional.of(pos));
        }
        this.timer = compound.getInt("Timer");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("OwnerName", this.entityData.get(OWNER_NAME));
        compound.put("MarkedItem", this.entityData.get(MARKED_ITEM).save(this.registryAccess()));
        this.entityData.get(CONTAINER_POS).ifPresent(pos -> {
            compound.putInt("ContainerX", pos.getX());
            compound.putInt("ContainerY", pos.getY());
            compound.putInt("ContainerZ", pos.getZ());
        });
        compound.putInt("Timer", this.timer);
    }

    public String getOwnerName() {
        return this.entityData.get(OWNER_NAME);
    }

    public ItemStack getMarkedItem() {
        return this.entityData.get(MARKED_ITEM);
    }

    public BlockPos getContainerPos() {
        return this.entityData.get(CONTAINER_POS).orElse(null);
    }

    public float getLifeProgress() {
        return (float) timer / InvseeConfig.getMarkDurationTicks();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double range = InvseeConfig.getMarkDisplayRange();
        return distance < range * range;
    }
}
