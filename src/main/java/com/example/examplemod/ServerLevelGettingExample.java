package com.example.examplemod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class ServerLevelGettingExample {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    ServerLevel serverLevel = server.getLevel(Level.OVERWORLD); // or Level.NETHER, Level.END
}