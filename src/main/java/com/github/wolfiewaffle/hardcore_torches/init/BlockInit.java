package com.github.wolfiewaffle.hardcore_torches.init;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockInit {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MainMod.MOD_ID);

    public static final RegistryObject<Block> LIT_LANTERN = BLOCKS.register("lit_lantern", () -> new LitLanternBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> AbstractLanternBlock.LANTERN_LIGHT_LEVEL).sound(SoundType.LANTERN).noOcclusion()));
    public static final RegistryObject<Block> UNLIT_LANTERN = BLOCKS.register("unlit_lantern", () -> new UnlitLanternBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().sound(SoundType.LANTERN).noOcclusion()));

    public static final RegistryObject<Block> LIT_SOUL_LANTERN = BLOCKS.register("lit_soul_lantern", () -> new LitSoulLanternBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> SoulLanternBlock.SOUL_LANTERN_LIGHT_LEVEL).sound(SoundType.LANTERN).noOcclusion()));
    public static final RegistryObject<Block> UNLIT_SOUL_LANTERN = BLOCKS.register("unlit_soul_lantern", () -> new UnlitSoulLanternBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().sound(SoundType.LANTERN).noOcclusion()));

    public static final RegistryObject<Block> LIT_TORCH = BLOCKS.register("lit_torch", () -> new LitFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 14).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> LIT_WALL_TORCH = BLOCKS.register("lit_wall_torch", () -> new LitWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 14).sound(SoundType.WOOD).noOcclusion()));

    public static final RegistryObject<Block> UNLIT_TORCH = BLOCKS.register("unlit_torch", () -> new UnlitFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> UNLIT_WALL_TORCH = BLOCKS.register("unlit_wall_torch", () -> new UnlitWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion()));

    public static final RegistryObject<Block> SMOLDERING_TORCH = BLOCKS.register("smoldering_torch", () -> new SmolderingFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 3).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> SMOLDERING_WALL_TORCH = BLOCKS.register("smoldering_wall_torch", () -> new SmolderingWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 3).sound(SoundType.WOOD).noOcclusion()));

    public static final RegistryObject<Block> BURNT_TORCH = BLOCKS.register("burnt_torch", () -> new BurntFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> BURNT_WALL_TORCH = BLOCKS.register("burnt_wall_torch", () -> new BurntWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion()));
}