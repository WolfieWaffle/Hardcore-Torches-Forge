package com.github.wolfiewaffle.hardcore_torches;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.config.ConfigRecipeCondition;
import com.github.wolfiewaffle.hardcore_torches.event.PlayerEventHandler;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.loot.SetFuelLootFunction;
import com.github.wolfiewaffle.hardcore_torches.loot.TorchLootFunction;
import com.github.wolfiewaffle.hardcore_torches.recipe.OilCanRecipe;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@net.minecraftforge.fml.common.Mod(MainMod.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MainMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    // Groups
    public static TorchGroup basicTorches = new TorchGroup("basic");

    // Tags
    public static final Tag.Named<Item> ALL_TORCH_ITEMS = ItemTags.bind("hardcore_torches:torches");
    public static final Tag.Named<Block> FREE_TORCH_LIGHT_BLOCKS = BlockTags.bind("hardcore_torches:free_torch_light_blocks");
    public static final Tag.Named<Item> FREE_TORCH_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:free_torch_light_items");
    public static final Tag.Named<Item> DAMAGE_TORCH_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:damage_torch_light_items");
    public static final Tag.Named<Item> CONSUME_TORCH_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:consume_torch_light_items");
    public static final Tag.Named<Item> FREE_TORCH_EXTINGUISH_ITEMS = ItemTags.bind("hardcore_torches:free_torch_extinguish_items");
    public static final Tag.Named<Item> DAMAGE_TORCH_EXTINGUISH_ITEMS = ItemTags.bind("hardcore_torches:damage_torch_extinguish_items");
    public static final Tag.Named<Item> CONSUME_TORCH_EXTINGUISH_ITEMS = ItemTags.bind("hardcore_torches:consume_torch_extinguish_items");
    public static final Tag.Named<Item> FREE_TORCH_SMOTHER_ITEMS = ItemTags.bind("hardcore_torches:free_torch_smother_items");
    public static final Tag.Named<Item> DAMAGE_TORCH_SMOTHER_ITEMS = ItemTags.bind("hardcore_torches:damage_torch_smother_items");
    public static final Tag.Named<Item> CONSUME_TORCH_SMOTHER_ITEMS = ItemTags.bind("hardcore_torches:consume_torch_smother_items");
    public static final Tag.Named<Item> FREE_LANTERN_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:free_lantern_light_items");
    public static final Tag.Named<Item> DAMAGE_LANTERN_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:damage_lantern_light_items");
    public static final Tag.Named<Item> CONSUME_LANTERN_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:consume_lantern_light_items");

    // Loot Functions
    public static final LootItemFunctionType HARDCORE_TORCH_LOOT_FUNCTION = new LootItemFunctionType(new TorchLootFunction.Serializer());
    public static final LootItemFunctionType SET_FUEL_LOOT_FUNCTION = new LootItemFunctionType(new SetFuelLootFunction.Serializer());

    // Recipe Types
    public static final RecipeType<OilCanRecipe> OIL_CAN_RECIPE = RecipeType.register("hardcore_torches:oil_can");

    /**
     * The modid of this mod, this has to match the modid in the mods.toml and has to be in the format defined in {@link net.minecraftforge.fml.loading.moddiscovery.ModInfo}
     */
    public static final String MOD_ID = "hardcore_torches";

    /**
     * Order of initialization:
     * 1. Registration
     * 2. Config reading (for client + common)
     * 3. FMLCommonSetupEvent
     * After world load:
     * 4. Config read on server
     */
    public MainMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);

        Config.init();

        // Init
        ItemInit.ITEMS.register(modEventBus);
        BlockInit.BLOCKS.register(modEventBus);
        BlockEntityInit.BLOCK_ENTITIES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Groups
        basicTorches.add(BlockInit.LIT_TORCH.get());
        basicTorches.add(BlockInit.LIT_WALL_TORCH.get());
        basicTorches.add(BlockInit.UNLIT_TORCH.get());
        basicTorches.add(BlockInit.UNLIT_WALL_TORCH.get());
        basicTorches.add(BlockInit.SMOLDERING_TORCH.get());
        basicTorches.add(BlockInit.SMOLDERING_WALL_TORCH.get());
        basicTorches.add(BlockInit.BURNT_TORCH.get());
        basicTorches.add(BlockInit.BURNT_WALL_TORCH.get());

        // Register Loot Functions
        Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation("hardcore_torches", "torch"), HARDCORE_TORCH_LOOT_FUNCTION);
        Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation("hardcore_torches", "set_damage"), SET_FUEL_LOOT_FUNCTION);
    }

    @SubscribeEvent //ModBus, can't use addListener due to nested genetics.
    public static void registerRecipeSerialziers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        CraftingHelper.register(new ConfigRecipeCondition.Serializer(() -> {return Config.craftUnlit.get();}, new ResourceLocation("hardcore_torches", "config_craft_unlit")));
        event.getRegistry().register(new OilCanRecipe.Serializer().setRegistryName("hardcore_torches:oil_can"));
    }
}
