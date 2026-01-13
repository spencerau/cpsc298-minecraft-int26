package com.example.examplemod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "examplemod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // register new material
    public static final ToolMaterial GOD_TIER = new ToolMaterial(
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // Least restrictive (or define your own)
        9999,   // durability
        100.0F, // speed
        25.0F,  // attack bonus
        30,     // enchantability
        ItemTags.PLANKS // Repairable with wood planks (for fun)
    );

    // BLOCKS

    // NOTE: Copy one of these blocks to make a new one
    public static final DeferredBlock<Block> CHEESE_BLOCK = BLOCKS.registerSimpleBlock("cheese_block",
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_YELLOW)
            .strength(0.5f, 6.0f) // Hardness, Resistance
            .lightLevel(state -> 4) // Emits light level 10
            .sound(net.minecraft.world.level.block.SoundType.STONE) // Stone sound when stepped on or broken
    );

    public static final DeferredItem<BlockItem> CHEESE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("cheese_block", CHEESE_BLOCK);

    public static final DeferredBlock<Block> RUBIKS_BLOCK = BLOCKS.registerSimpleBlock("rubiks_block",
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(0.5f, 6.0f) // Hardness, Resistance
            .lightLevel(state -> 4) // Emits light level 10
            .sound(net.minecraft.world.level.block.SoundType.STONE) // Stone sound when stepped on or broken
    );
    public static final DeferredItem<BlockItem> RUBIKS_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("rubiks_block", RUBIKS_BLOCK);

    public static final DeferredBlock<Block> CORGI_DISPENSER_BLOCK = BLOCKS.registerSimpleBlock("corgi_dispenser_block",
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_YELLOW)
            .strength(0.5f, 6.0f) // Hardness, Resistance
            .lightLevel(state -> 4) // Emits light level 10
            .sound(net.minecraft.world.level.block.SoundType.STONE) // Stone sound when stepped on or broken
    );

    public static final DeferredItem<BlockItem> CORGI_DISPENSER_BLOCK_ITEM =
        ITEMS.registerSimpleBlockItem("corgi_dispenser_block", CORGI_DISPENSER_BLOCK);

    public static final DeferredBlock<Block> BOOM_BLOCK = BLOCKS.register(
        "boom_block",
        registryName -> new BoomBlock(BlockBehaviour.Properties.of()
            .strength(1f, 30f) // hardness and explosive resistance
            .speedFactor(2f)
            .lightLevel(state -> 10)
            .setId(ResourceKey.create(Registries.BLOCK, registryName))
        ));

    public static final DeferredItem<BlockItem> BOOM_BLOCK_ITEM =
        ITEMS.registerSimpleBlockItem("boom_block", BOOM_BLOCK);

    public static final DeferredItem<Item> OP_PICKAXE =
        ITEMS.register("op_pickaxe", registryName ->
            new Item(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, registryName))
                .durability(9999)
                .pickaxe(
                    GOD_TIER,
                    99.0F,   // Attack Damage
                    1.6F     // Attack Speed
                )
            )
        );

    public static final DeferredItem<Item> CHEESE_ITEM =
        ITEMS.register("cheese", registryName ->
        new CheeseItem(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, registryName))
            .food(new FoodProperties.Builder()
                .nutrition(4)
                .saturationModifier(0.5f)
                .alwaysEdible()
                .build()
            )
        )
    );

    // CREATIVE TABS

    // NOTE: Add your block item below like "output.accept(CHEESE_BLOCK_ITEM.get())"
    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.examplemod")) // The translation key for the tab title
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> CORGI_DISPENSER_BLOCK_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(CHEESE_BLOCK_ITEM.get()); // Your custom block item
                        output.accept(RUBIKS_BLOCK_ITEM.get());
                        output.accept(CORGI_DISPENSER_BLOCK_ITEM.get());
                        output.accept(BOOM_BLOCK_ITEM.get());
                        output.accept(OP_PICKAXE.get());
                        output.accept(CHEESE_ITEM.get());
                    }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(CHEESE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
