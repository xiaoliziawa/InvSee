package com.lirxowo.invsee.entity;

import com.lirxowo.invsee.compat.ftbteams.FTBTeamsCompat;
import com.lirxowo.invsee.config.InvseeConfig;
import com.lirxowo.invsee.registry.EntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ItemMarkEntity extends Entity {
    private static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<ItemStack> MARKED_ITEM = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Optional<BlockPos>> CONTAINER_POS = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> TEAM_ID = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> OWNER_IN_PARTY = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> MARK_SOURCE = SynchedEntityData.defineId(
            ItemMarkEntity.class, EntityDataSerializers.BYTE);

    public enum MarkSource {
        PLAYER_INVENTORY,
        CONTAINER,
        VIRTUAL
    }

    private int timer = 0;

    public ItemMarkEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public ItemMarkEntity(Level level, Player owner, Vec3 location, ItemStack itemStack, BlockPos containerPos) {
        this(level, owner, location, itemStack, containerPos, MarkSource.PLAYER_INVENTORY);
    }

    public ItemMarkEntity(Level level, Player owner, Vec3 location, ItemStack itemStack, BlockPos containerPos, MarkSource source) {
        this(EntityRegister.ITEM_MARK.get(), level);
        this.setPos(location);
        this.entityData.set(OWNER_NAME, owner.getName().getString());
        this.entityData.set(MARKED_ITEM, itemStack.copy());
        this.entityData.set(CONTAINER_POS, Optional.ofNullable(containerPos));
        this.entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
        this.entityData.set(MARK_SOURCE, (byte) source.ordinal());

        UUID teamId = FTBTeamsCompat.getPlayerTeamId(owner.getUUID());
        this.entityData.set(TEAM_ID, Optional.ofNullable(teamId));
        this.entityData.set(OWNER_IN_PARTY, FTBTeamsCompat.isInParty(owner.getUUID()));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_NAME, "");
        this.entityData.define(MARKED_ITEM, ItemStack.EMPTY);
        this.entityData.define(CONTAINER_POS, Optional.empty());
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(TEAM_ID, Optional.empty());
        this.entityData.define(OWNER_IN_PARTY, false);
        this.entityData.define(MARK_SOURCE, (byte) 0);
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
            this.entityData.set(MARKED_ITEM, ItemStack.of(compound.getCompound("MarkedItem")));
        }
        if (compound.contains("ContainerX")) {
            BlockPos pos = new BlockPos(
                    compound.getInt("ContainerX"),
                    compound.getInt("ContainerY"),
                    compound.getInt("ContainerZ")
            );
            this.entityData.set(CONTAINER_POS, Optional.of(pos));
        }
        if (compound.contains("OwnerUUID")) {
            this.entityData.set(OWNER_UUID, Optional.of(compound.getUUID("OwnerUUID")));
        }
        if (compound.contains("TeamID")) {
            this.entityData.set(TEAM_ID, Optional.of(compound.getUUID("TeamID")));
        }
        if (compound.contains("OwnerInParty")) {
            this.entityData.set(OWNER_IN_PARTY, compound.getBoolean("OwnerInParty"));
        }
        if (compound.contains("MarkSource")) {
            this.entityData.set(MARK_SOURCE, compound.getByte("MarkSource"));
        }
        this.timer = compound.getInt("Timer");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("OwnerName", this.entityData.get(OWNER_NAME));
        compound.put("MarkedItem", this.entityData.get(MARKED_ITEM).save(new CompoundTag()));
        this.entityData.get(CONTAINER_POS).ifPresent(pos -> {
            compound.putInt("ContainerX", pos.getX());
            compound.putInt("ContainerY", pos.getY());
            compound.putInt("ContainerZ", pos.getZ());
        });
        this.entityData.get(OWNER_UUID).ifPresent(uuid -> compound.putUUID("OwnerUUID", uuid));
        this.entityData.get(TEAM_ID).ifPresent(uuid -> compound.putUUID("TeamID", uuid));
        compound.putBoolean("OwnerInParty", this.entityData.get(OWNER_IN_PARTY));
        compound.putByte("MarkSource", this.entityData.get(MARK_SOURCE));
        compound.putInt("Timer", this.timer);
    }

    public String getOwnerName() {
        return this.entityData.get(OWNER_NAME);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    @Nullable
    public UUID getTeamId() {
        return this.entityData.get(TEAM_ID).orElse(null);
    }

    public boolean isOwnerInParty() {
        return this.entityData.get(OWNER_IN_PARTY);
    }

    public ItemStack getMarkedItem() {
        return this.entityData.get(MARKED_ITEM);
    }

    @Nullable
    public BlockPos getContainerPos() {
        return this.entityData.get(CONTAINER_POS).orElse(null);
    }

    public MarkSource getMarkSource() {
        byte ordinal = this.entityData.get(MARK_SOURCE);
        MarkSource[] values = MarkSource.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return MarkSource.PLAYER_INVENTORY;
    }

    public boolean isFromJEI() {
        return getMarkSource() == MarkSource.VIRTUAL;
    }

    public float getLifeProgress() {
        return (float) timer / InvseeConfig.getMarkDurationTicks();
    }

    public boolean shouldBeVisibleTo(Player viewer) {
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID == null) {
            return true;
        }

        if (ownerUUID.equals(viewer.getUUID())) {
            return true;
        }

        return FTBTeamsCompat.canSeeMarksClient(ownerUUID, getTeamId(), isOwnerInParty(), viewer);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double range = InvseeConfig.getMarkDisplayRange();
        return distance < range * range;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
