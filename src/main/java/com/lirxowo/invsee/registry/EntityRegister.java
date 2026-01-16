package com.lirxowo.invsee.registry;

import com.lirxowo.invsee.Invsee;
import com.lirxowo.invsee.entity.ItemMarkEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegister {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Invsee.MODID);

    public static final RegistryObject<EntityType<ItemMarkEntity>> ITEM_MARK =
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
