package com.github.wolfiewaffle.hardcore_torches.init;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.blockentity.HardcoreCampfireBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.LanternBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.TorchBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityInit {

    private static Block[] torchTe() {
        return new Block[] {
                BlockInit.LIT_TORCH.get(),
                BlockInit.LIT_WALL_TORCH.get(),
                BlockInit.LIT_SOUL_TORCH.get(),
                BlockInit.LIT_WALL_SOUL_TORCH.get(),
                BlockInit.SMOLDERING_TORCH.get(),
                BlockInit.SMOLDERING_WALL_TORCH.get(),
        };
    }

    private static Block[] lanternTe() {
        return  new Block[] {
                BlockInit.LIT_LANTERN.get(),
                BlockInit.UNLIT_LANTERN.get(),
                BlockInit.LIT_SOUL_LANTERN.get(),
                BlockInit.UNLIT_SOUL_LANTERN.get()
        };
    }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HardcoreTorches.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TorchBlockEntity>> TORCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("torch_block_entity", () -> BlockEntityType.Builder.of(TorchBlockEntity::new, torchTe()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LanternBlockEntity>> LANTERN_BLOCK_ENTITY = BLOCK_ENTITIES.register("lantern_block_entity", () -> BlockEntityType.Builder.of(LanternBlockEntity::new, lanternTe()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HardcoreCampfireBlockEntity>> CAMPFIRE_BLOCK_ENTITY = BLOCK_ENTITIES.register("campfire_block_entity", () -> BlockEntityType.Builder.of(HardcoreCampfireBlockEntity::new, BlockInit.HARDCORE_CAMPFIRE.get()).build(null));
}
