package com.example.examplemod;

import com.example.examplemod.CorgiEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ExampleMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CorgiEntity>> CORGI =
            ENTITY_TYPES.register("corgi", () ->
                    EntityType.Builder.<CorgiEntity>of(CorgiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.85F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(
                                    Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID,
                                            "corgi")
                            ))
            );

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(ModEntities::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(CORGI.get(), Wolf.createAttributes().build());
    }
}
