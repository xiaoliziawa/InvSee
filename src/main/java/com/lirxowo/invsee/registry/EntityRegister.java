package com.lirxowo.invsee.registry;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.entity.ItemMarkEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 实体注册表
 */
public class EntityRegister {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Invsee.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ItemMarkEntity>> ITEM_MARK =
            ENTITY_TYPES.register("item_mark", () ->
                    EntityType.Builder.<ItemMarkEntity>of(ItemMarkEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .fireImmune()
                            .build(Invsee.MODID + ":item_mark")
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
