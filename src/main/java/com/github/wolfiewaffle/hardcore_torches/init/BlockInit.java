package com.github.wolfiewaffle.hardcore_torches.init;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.HardcoreFloorTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.block.HardcoreWallTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.block.LanternBlock;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockInit {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MainMod.MOD_ID);

    public static final RegistryObject<Block> LIT_LANTERN = BLOCKS.register("lit_lantern", () -> new LanternBlock(Block.Properties.of(Material.DECORATION).instabreak().lightLevel((state) -> 15).sound(SoundType.LANTERN).noOcclusion(), true));
    public static final RegistryObject<Block> UNLIT_LANTERN = BLOCKS.register("unlit_lantern", () -> new LanternBlock(Block.Properties.of(Material.DECORATION).instabreak().sound(SoundType.LANTERN).noOcclusion(), false));

    public static final RegistryObject<Block> LIT_TORCH = BLOCKS.register("lit_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel((state) -> 14).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.FLAME, ETorchState.LIT, MainMod.basicTorches));
    public static final RegistryObject<Block> LIT_WALL_TORCH = BLOCKS.register("lit_wall_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel((state) -> 14).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.FLAME, ETorchState.LIT, MainMod.basicTorches));

    public static final RegistryObject<Block> UNLIT_TORCH = BLOCKS.register("unlit_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, ETorchState.UNLIT, MainMod.basicTorches));
    public static final RegistryObject<Block> UNLIT_WALL_TORCH = BLOCKS.register("unlit_wall_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, ETorchState.UNLIT, MainMod.basicTorches));

    public static final RegistryObject<Block> SMOLDERING_TORCH = BLOCKS.register("smoldering_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel((state) -> 3).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.SMOKE, ETorchState.SMOLDERING, MainMod.basicTorches));
    public static final RegistryObject<Block> SMOLDERING_WALL_TORCH = BLOCKS.register("smoldering_wall_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel((state) -> 3).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.SMOKE, ETorchState.SMOLDERING, MainMod.basicTorches));

    public static final RegistryObject<Block> BURNT_TORCH = BLOCKS.register("burnt_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, ETorchState.BURNT, MainMod.basicTorches));
    public static final RegistryObject<Block> BURNT_WALL_TORCH = BLOCKS.register("burnt_wall_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, ETorchState.BURNT, MainMod.basicTorches));
}