package com.github.wolfiewaffle.hardcore_torches.init;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.item.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemInit {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HardcoreTorches.MOD_ID);

    public static final DeferredHolder<Item, OilCanItem> OIL_CAN = ITEMS.register("oil_can", () -> new OilCanItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> ANIMAL_FAT = ITEMS.register("animal_fat", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, FireStarterItem> FIRE_STARTER = ITEMS.register("fire_starter", () -> new FireStarterItem(new Item.Properties()));

    public static final DeferredHolder<Item, TorchItem> LIT_TORCH = ITEMS.register("lit_torch", () -> new TorchItem(BlockInit.LIT_TORCH.get(), BlockInit.LIT_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredHolder<Item, TorchItem> UNLIT_TORCH = ITEMS.register("unlit_torch", () -> new TorchItem(BlockInit.UNLIT_TORCH.get(), BlockInit.UNLIT_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredHolder<Item, TorchItem> SMOLDERING_TORCH = ITEMS.register("smoldering_torch", () -> new TorchItem(BlockInit.SMOLDERING_TORCH.get(), BlockInit.SMOLDERING_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredHolder<Item, TorchItem> BURNT_TORCH = ITEMS.register("burnt_torch", () -> new TorchItem(BlockInit.BURNT_TORCH.get(), BlockInit.BURNT_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredHolder<Item, TorchItem> LIT_SOUL_TORCH = ITEMS.register("lit_soul_torch", () -> new TorchItem(BlockInit.LIT_SOUL_TORCH.get(), BlockInit.LIT_WALL_SOUL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredHolder<Item, TorchItem> UNLIT_SOUL_TORCH = ITEMS.register("unlit_soul_torch", () -> new TorchItem(BlockInit.UNLIT_SOUL_TORCH.get(), BlockInit.UNLIT_WALL_SOUL_TORCH.get(), new Item.Properties(), Direction.DOWN));

    public static final DeferredHolder<Item, BasicLanternItem> LIT_LANTERN = ITEMS.register("lit_lantern", () -> new BasicLanternItem(BlockInit.LIT_LANTERN.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BasicLanternItem> UNLIT_LANTERN = ITEMS.register("unlit_lantern", () -> new BasicLanternItem(BlockInit.UNLIT_LANTERN.get(), new Item.Properties()));

    public static final DeferredHolder<Item, SoulLanternItem> LIT_SOUL_LANTERN = ITEMS.register("lit_soul_lantern", () -> new SoulLanternItem(BlockInit.LIT_SOUL_LANTERN.get(), new Item.Properties()));
    public static final DeferredHolder<Item, SoulLanternItem> UNLIT_SOUL_LANTERN = ITEMS.register("unlit_soul_lantern", () -> new SoulLanternItem(BlockInit.UNLIT_SOUL_LANTERN.get(), new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> UNLIT_CAMPFIRE = ITEMS.register("unlit_campfire", () -> new BlockItem(BlockInit.HARDCORE_CAMPFIRE.get(), new Item.Properties()));
}
