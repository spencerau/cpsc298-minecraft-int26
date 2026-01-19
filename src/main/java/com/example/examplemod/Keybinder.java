package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class Keybinder {
    public static KeyMapping OPEN_TEXTBOX_MENU;

    // Called during mod init - from your main mod, like this:
    // Keybinder.register(modEventBus);
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(Keybinder::onRegisterKeyMappings);
    }

    // This needs to be registered on the GAME EVENT BUS (NeoForge.EVENT_BUS) - in your main mod, like this:
    // Keybinder.registerGameEvents();
    public static void registerGameEvents() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(Keybinder::onPlayerTick);
    }

    // This is what gets called from modEventBus.addListener(Keybinder::onRegisterKeyMappings) above
    // Only gets called once to set up key mappings
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_TEXTBOX_MENU = new KeyMapping(
                "key.examplemod.open_ui",
                InputConstants.KEY_G,               // Default key
                "key.categories.misc"              // Category in controls menu
        );
        event.register(OPEN_TEXTBOX_MENU);

        // You can add other keybindings here
    }

    // Gets called every player tick (registered above in registerGameEvents)
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Ensure we're on the client and this is the local player
        if (!(player instanceof LocalPlayer)) return;
        if (Minecraft.getInstance().screen != null) return;

        // Consuming the click means whatever key we assigned to OPEN_TEXTBOX_MENU won't be
        // used to trigger other stuff.  It's one-use only.
        if (OPEN_TEXTBOX_MENU != null && OPEN_TEXTBOX_MENU.consumeClick()) {
            Minecraft.getInstance().setScreen(new SampleUiScreen());
        }
    }
}