package com.github.wolfiewaffle.hardcore_torches.init;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.item.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MainMod.MOD_ID);

    public static final RegistryObject<Item> OIL_CAN = ITEMS.register("oil_can", () -> new OilCanItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ANIMAL_FAT = ITEMS.register("animal_fat", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FIRE_STARTER = ITEMS.register("fire_starter", () -> new FireStarterItem(new Item.Properties()));

    public static final RegistryObject<Item> LIT_TORCH = ITEMS.register("lit_torch", () -> new TorchItem(BlockInit.LIT_TORCH.get(), BlockInit.LIT_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final RegistryObject<Item> UNLIT_TORCH = ITEMS.register("unlit_torch", () -> new TorchItem(BlockInit.UNLIT_TORCH.get(), BlockInit.UNLIT_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final RegistryObject<Item> SMOLDERING_TORCH = ITEMS.register("smoldering_torch", () -> new TorchItem(BlockInit.SMOLDERING_TORCH.get(), BlockInit.SMOLDERING_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final RegistryObject<Item> BURNT_TORCH = ITEMS.register("burnt_torch", () -> new TorchItem(BlockInit.BURNT_TORCH.get(), BlockInit.BURNT_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));

    public static final RegistryObject<Item> LIT_LANTERN = ITEMS.register("lit_lantern", () -> new BasicLanternItem(BlockInit.LIT_LANTERN.get(), new Item.Properties()));
    public static final RegistryObject<Item> UNLIT_LANTERN = ITEMS.register("unlit_lantern", () -> new BasicLanternItem(BlockInit.UNLIT_LANTERN.get(), new Item.Properties()));

    public static final RegistryObject<Item> LIT_SOUL_LANTERN = ITEMS.register("lit_soul_lantern", () -> new SoulLanternItem(BlockInit.LIT_SOUL_LANTERN.get(), new Item.Properties()));
    public static final RegistryObject<Item> UNLIT_SOUL_LANTERN = ITEMS.register("unlit_soul_lantern", () -> new SoulLanternItem(BlockInit.UNLIT_SOUL_LANTERN.get(), new Item.Properties()));
}
