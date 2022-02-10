package com.github.wolfiewaffle.hardcore_torches;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.config.ConfigRecipeCondition;
import com.github.wolfiewaffle.hardcore_torches.event.PlayerEventHandler;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.loot.FatModifier;
import com.github.wolfiewaffle.hardcore_torches.loot.SetFuelLootFunction;
import com.github.wolfiewaffle.hardcore_torches.loot.TorchLootFunction;
import com.github.wolfiewaffle.hardcore_torches.recipe.OilCanRecipe;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@net.minecraftforge.fml.common.Mod(MainMod.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MainMod.MOD_ID)
public class MainMod
{
    /**
     * The modid of this mod, this has to match the modid in the mods.toml and has to be in the format defined in {@link net.minecraftforge.fml.loading.moddiscovery.ModInfo}
     */
    public static final String MOD_ID = "hardcore_torches";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    // Groups
    public static TorchGroup basicTorches = new TorchGroup("basic");

    // Tags
    public static final ITag.INamedTag<Item> ALL_TORCH_ITEMS = ItemTags.bind("hardcore_torches:torches");
    public static final ITag.INamedTag<Block> FREE_TORCH_LIGHT_BLOCKS = BlockTags.bind("hardcore_torches:free_torch_light_blocks");
    public static final ITag.INamedTag<Item> FREE_TORCH_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:free_torch_light_items");
    public static final ITag.INamedTag<Item> DAMAGE_TORCH_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:damage_torch_light_items");
    public static final ITag.INamedTag<Item> CONSUME_TORCH_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:consume_torch_light_items");
    public static final ITag.INamedTag<Item> FREE_TORCH_EXTINGUISH_ITEMS = ItemTags.bind("hardcore_torches:free_torch_extinguish_items");
    public static final ITag.INamedTag<Item> DAMAGE_TORCH_EXTINGUISH_ITEMS = ItemTags.bind("hardcore_torches:damage_torch_extinguish_items");
    public static final ITag.INamedTag<Item> CONSUME_TORCH_EXTINGUISH_ITEMS = ItemTags.bind("hardcore_torches:consume_torch_extinguish_items");
    public static final ITag.INamedTag<Item> FREE_TORCH_SMOTHER_ITEMS = ItemTags.bind("hardcore_torches:free_torch_smother_items");
    public static final ITag.INamedTag<Item> DAMAGE_TORCH_SMOTHER_ITEMS = ItemTags.bind("hardcore_torches:damage_torch_smother_items");
    public static final ITag.INamedTag<Item> CONSUME_TORCH_SMOTHER_ITEMS = ItemTags.bind("hardcore_torches:consume_torch_smother_items");
    public static final ITag.INamedTag<Item> FREE_LANTERN_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:free_lantern_light_items");
    public static final ITag.INamedTag<Item> DAMAGE_LANTERN_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:damage_lantern_light_items");
    public static final ITag.INamedTag<Item> CONSUME_LANTERN_LIGHT_ITEMS = ItemTags.bind("hardcore_torches:consume_lantern_light_items");

    // Loot Functions
    public static final LootFunctionType HARDCORE_TORCH_LOOT_FUNCTION = new LootFunctionType(new TorchLootFunction.Serializer());
    public static final LootFunctionType SET_FUEL_LOOT_FUNCTION = new LootFunctionType(new SetFuelLootFunction.Serializer());

    // Recipe Types
    public static final IRecipeType<OilCanRecipe> OIL_CAN_RECIPE = IRecipeType.register("hardcore_torches:oil_can");

    // Register Loot Tables
    private static final DeferredRegister<GlobalLootModifierSerializer<?>> GLM = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    private static final RegistryObject<FatModifier.Serializer> FAT_LOOT_PIG = GLM.register("fat_modifier", FatModifier.Serializer::new);

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

        // For loot tables
        GLM.register(modEventBus);
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
    public static void registerRecipeSerialziers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        CraftingHelper.register(new ConfigRecipeCondition.Serializer(() -> Config.craftUnlit.get(), new ResourceLocation("hardcore_torches", "config_craft_unlit")));
        CraftingHelper.register(new ConfigRecipeCondition.Serializer(() -> (Config.oilRecipeType.get() == 0 || Config.oilRecipeType.get() == 2), new ResourceLocation("hardcore_torches", "config_can_fat")));
        CraftingHelper.register(new ConfigRecipeCondition.Serializer(() -> (Config.oilRecipeType.get() == 1 || Config.oilRecipeType.get() == 2), new ResourceLocation("hardcore_torches", "config_can_coal")));
        event.getRegistry().register(new OilCanRecipe.Serializer().setRegistryName("hardcore_torches:oil_can"));
    }
}
