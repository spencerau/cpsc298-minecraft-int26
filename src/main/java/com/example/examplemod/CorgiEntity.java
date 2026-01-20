package com.example.examplemod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;

public class CorgiEntity extends Wolf {
    public CorgiEntity(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }
}


