package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class BlockDropHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = event.getPlayer().level();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        // if (!level.isClientSide && state.is(ExampleMod.CORGI_DISPENSER_BLOCK.get())) {
        //     Block.popResource((ServerLevel) level, pos, new ItemStack(Items.RED_DYE));
        // }
    }
}