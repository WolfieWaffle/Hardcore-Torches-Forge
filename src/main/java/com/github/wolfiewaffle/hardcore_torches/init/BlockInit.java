package com.github.wolfiewaffle.hardcore_torches.init;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.block.*;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockInit {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HardcoreTorches.MOD_ID);

    public static final DeferredBlock<LanternBlock> LIT_LANTERN = BLOCKS.register("lit_lantern", () -> new LanternBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> AbstractLanternBlock.LANTERN_LIGHT_LEVEL).sound(SoundType.LANTERN).noOcclusion(), true, () -> Config.defaultLanternFuel.get()));
    public static final DeferredBlock<LanternBlock> UNLIT_LANTERN = BLOCKS.register("unlit_lantern", () -> new LanternBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().sound(SoundType.LANTERN).noOcclusion(), false, () -> Config.defaultLanternFuel.get()));

    public static final DeferredBlock<LanternBlock> LIT_SOUL_LANTERN = BLOCKS.register("lit_soul_lantern", () -> new LanternBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> AbstractLanternBlock.LANTERN_LIGHT_LEVEL).sound(SoundType.LANTERN).noOcclusion(), true, () -> Config.defaultSoulLanternFuel.get()));
    public static final DeferredBlock<LanternBlock> UNLIT_SOUL_LANTERN = BLOCKS.register("unlit_soul_lantern", () -> new LanternBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().sound(SoundType.LANTERN).noOcclusion(), false, () -> Config.defaultSoulLanternFuel.get()));

    public static final DeferredBlock<HardcoreFloorTorchBlock> LIT_TORCH = BLOCKS.register("lit_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 14).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.FLAME, ParticleTypes.SMOKE, ETorchState.LIT, HardcoreTorches.basicTorches, () -> Config.defaultTorchFuel.get()));
    public static final DeferredBlock<HardcoreWallTorchBlock> LIT_WALL_TORCH = BLOCKS.register("lit_wall_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 14).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.FLAME, ParticleTypes.SMOKE, ETorchState.LIT, HardcoreTorches.basicTorches, () -> Config.defaultTorchFuel.get()));
    public static final DeferredBlock<HardcoreFloorTorchBlock> LIT_SOUL_TORCH = BLOCKS.register("lit_soul_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 10).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SMOKE, ETorchState.LIT, HardcoreTorches.soulTorches, () -> Config.defaultSoulTorchFuel.get()));
    public static final DeferredBlock<HardcoreWallTorchBlock> LIT_WALL_SOUL_TORCH = BLOCKS.register("lit_wall_soul_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 10).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SMOKE, ETorchState.LIT, HardcoreTorches.soulTorches, () -> Config.defaultSoulTorchFuel.get()));

    public static final DeferredBlock<HardcoreFloorTorchBlock> UNLIT_TORCH = BLOCKS.register("unlit_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, null, ETorchState.UNLIT, HardcoreTorches.basicTorches, () -> Config.defaultTorchFuel.get()));
    public static final DeferredBlock<HardcoreWallTorchBlock> UNLIT_WALL_TORCH = BLOCKS.register("unlit_wall_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, null, ETorchState.UNLIT, HardcoreTorches.basicTorches, () -> Config.defaultTorchFuel.get()));
    public static final DeferredBlock<HardcoreFloorTorchBlock> UNLIT_SOUL_TORCH = BLOCKS.register("unlit_soul_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, null, ETorchState.UNLIT, HardcoreTorches.soulTorches, () -> Config.defaultSoulTorchFuel.get()));
    public static final DeferredBlock<HardcoreWallTorchBlock> UNLIT_WALL_SOUL_TORCH = BLOCKS.register("unlit_wall_soul_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, null, ETorchState.UNLIT, HardcoreTorches.soulTorches, () -> Config.defaultSoulTorchFuel.get()));

    public static final DeferredBlock<HardcoreFloorTorchBlock> SMOLDERING_TORCH = BLOCKS.register("smoldering_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 3).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.SMOKE, null, ETorchState.SMOLDERING, HardcoreTorches.basicTorches, () -> Config.defaultTorchFuel.get()));
    public static final DeferredBlock<HardcoreWallTorchBlock> SMOLDERING_WALL_TORCH = BLOCKS.register("smoldering_wall_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().lightLevel((state) -> 3).sound(SoundType.WOOD).noOcclusion(), ParticleTypes.SMOKE, null, ETorchState.SMOLDERING, HardcoreTorches.basicTorches, () -> Config.defaultTorchFuel.get()));

    public static final DeferredBlock<HardcoreFloorTorchBlock> BURNT_TORCH = BLOCKS.register("burnt_torch", () -> new HardcoreFloorTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, null, ETorchState.BURNT, HardcoreTorches.basicTorches, () -> Config.defaultTorchFuel.get()));
    public static final DeferredBlock<HardcoreWallTorchBlock> BURNT_WALL_TORCH = BLOCKS.register("burnt_wall_torch", () -> new HardcoreWallTorchBlock(Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.WOOD).noOcclusion(), null, null, ETorchState.BURNT, HardcoreTorches.basicTorches, () -> Config.defaultTorchFuel.get()));

    public static final DeferredBlock<HardcoreCampfire> HARDCORE_CAMPFIRE = BLOCKS.register("hardcore_campfire", () -> new HardcoreCampfire(true, 1, BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).lightLevel(HardcoreCampfire.litBlockEmission(15)).noOcclusion().ignitedByLava()));
}