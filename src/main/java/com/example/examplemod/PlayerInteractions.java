package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class PlayerInteractions {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();       // The player
        ItemStack item = event.getItemStack();   // The item used
        Level world = event.getLevel();          // The world (level)
        InteractionHand hand = event.getHand();  // Main or off-hand

        if (!world.isClientSide) {
            player.displayClientMessage(
                Component.literal(
                    "[RightClickItem] Player: " + player.getName().getString() +
                        ", Item: " + item.getDisplayName().getString() +
                        ", Hand: " + hand +
                        ", In World: " + (world instanceof ServerLevel ? "Server" : "Client")
                ),
                false
            );
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();                     // The player
        ItemStack item = event.getItemStack();                 // The item used
        Level world = event.getLevel();                        // The world
        InteractionHand hand = event.getHand();                // Hand used
        Entity target = event.getTarget();                     // Entity being interacted with

        if (!world.isClientSide) {
            player.displayClientMessage(
                Component.literal(
                    "[EntityInteract] Player: " + player.getName().getString() +
                        ", Target Entity: " + target.getName().getString() +
                        ", Hand: " + hand +
                        ", Item: " + item.getDisplayName().getString()
                ),
                false
            );
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        InteractionHand hand = event.getHand();
        ItemStack item = player.getItemInHand(hand);
        BlockPos pos = event.getPos();

        // Check if empty hand and right-clicked CORGI_DISPENSER_BLOCK
        if (hand == InteractionHand.MAIN_HAND && item.isEmpty() &&
                world.getBlockState(pos).is(ExampleMod.CORGI_DISPENSER_BLOCK.get())) {

            if (!world.isClientSide) {

                // Play the sound
                world.playSound(null, pos,
                        ExampleMod.CORGI_BARK.value(),
                        SoundSource.NEUTRAL, 1.0F, 1.0F);

                // show the particle effects
                ServerLevel serverLevel = (ServerLevel) world;
                serverLevel.sendParticles(
                    ParticleTypes.HEART,  // Particle type
                    pos.getX() + 0.5,     // X center
                    pos.getY() + 1.0,     // Y just above the block
                    pos.getZ() + 0.5,     // Z center
                    5,                    // Count
                    0.3, 0.3, 0.3,        // Spread in x/y/z
                    0.01                  // Speed (motion)
                );
                
                // Spawn a Corgi just above the block (server-side)
                if (world instanceof ServerLevel server) {
                    var corgi = ModEntities.CORGI.get().create(server, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                    if (corgi != null) {
                        // position
                        var p = Vec3.atBottomCenterOf(pos).add(0.0, 1.0, 0.0);
                        corgi.setPos(p);                      // set x/y/z
        
                        // face the player (yaw) and keep pitch flat
                        corgi.setYRot(player.getYRot());
                        corgi.setXRot(0.0F);
        
                        // sync body/head rotation so it looks the way it's facing
                        if (corgi instanceof net.minecraft.world.entity.LivingEntity living) {
                            living.setYBodyRot(corgi.getYRot());
                            living.setYHeadRot(corgi.getYRot());
                        }
        
                        // don't let us die
                        corgi.setPersistenceRequired();
        
                        // custom name
                        RandomSource random = world.getRandom();
                        String name = random.nextBoolean() ? "Sandie" : "Nova";
                        corgi.setCustomName(Component.literal(name));
        
                        // auto-tame and start off not sitting
                        if (corgi instanceof net.minecraft.world.entity.animal.wolf.Wolf wolf) {
                            wolf.tame(player);
                            wolf.setOrderedToSit(false);
                        }
        
                        server.addFreshEntity(corgi);
                    }
                }
            }
            event.setCanceled(true);
        }

        // Only activate when holding the Transmutation Wand
        if (item.is(ExampleMod.TRANSMUTATION_WAND.get())) {
            if (!world.isClientSide) {
                var state = world.getBlockState(pos);

                if (state.is(Blocks.COPPER_ORE)) {
                    world.setBlock(pos, Blocks.IRON_ORE.defaultBlockState(), 3);
                    player.displayClientMessage(Component.literal("Transmuted Copper → Iron!"), false);
                    ShowTransmuteParticles(world, pos);
                } else if (state.is(Blocks.IRON_ORE)) {
                    world.setBlock(pos, Blocks.GOLD_ORE.defaultBlockState(), 3);
                    player.displayClientMessage(Component.literal("Transmuted Iron → Gold!"), false);
                    ShowTransmuteParticles(world, pos);
                } else if (state.is(Blocks.GOLD_ORE)) {
                    world.setBlock(pos, Blocks.NETHERITE_BLOCK.defaultBlockState(), 3);
                    player.displayClientMessage(Component.literal("Transmuted Gold → Netherite!"), false);
                    ShowTransmuteParticles(world, pos);
                } else if (state.is(Blocks.NETHERITE_BLOCK)) {
                    world.setBlock(pos, ExampleMod.CHEESE_BLOCK.get().defaultBlockState(), 3);
                    player.displayClientMessage(Component.literal("Transmuted Netherite → Cheese!"), false);
                    ShowTransmuteParticles(world, pos);
                } else if (state.is(ExampleMod.CHEESE_BLOCK.get())) {
                    world.setBlock(pos, Blocks.COPPER_ORE.defaultBlockState(), 3);
                    player.displayClientMessage(Component.literal("Transmuted Cheese → Copper!"), false);
                    ShowTransmuteParticles(world, pos);
                } else if (state.is(Blocks.LAPIS_ORE)) {
                    world.setBlock(pos, Blocks.DIAMOND_ORE.defaultBlockState(), 3);
                    player.displayClientMessage(Component.literal("Transmuted Lapis → Diamond!"), false);
                    ShowTransmuteParticles(world, pos);
                } else if (state.is(Blocks.DIAMOND_ORE)) {
                    world.setBlock(pos, Blocks.EMERALD_ORE.defaultBlockState(), 3);
                    player.displayClientMessage(Component.literal("Transmuted Diamond → Emerald!"), false);
                    ShowTransmuteParticles(world, pos);
                } else if (state.is(Blocks.EMERALD_ORE)) {
                    world.setBlock(pos, Blocks.LAPIS_ORE.defaultBlockState(), 3);
                    player.displayClientMessage(Component.literal("Transmuted Emerald → Lapis!"), false);
                    ShowTransmuteParticles(world, pos);
                } else {
                    player.displayClientMessage(Component.literal("This block can't be transmuted."), false);
                }

                // Stop the normal interaction (like mining or opening)
                event.setCanceled(true);
            }
        }
    }

    private static void ShowTransmuteParticles(Level world, BlockPos pos)
    {
        ServerLevel serverLevel = (ServerLevel) world;

        serverLevel.sendParticles(
            (SimpleParticleType) SampleUiScreen.getSelectedParticleType(), // Particle type
            pos.getX() + 0.5,           // X center
            pos.getY() + 1.0,           // Y just above the block
            pos.getZ() + 0.5,           // Z center
            5,                          // Count
            0.3, 0.3, 0.3,       				// Spread in x/y/z
            0.01                        // Speed (motion)
        );
    }

}