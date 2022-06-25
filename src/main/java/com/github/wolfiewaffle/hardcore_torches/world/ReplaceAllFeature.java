package com.github.wolfiewaffle.hardcore_torches.world;

import com.github.wolfiewaffle.hardcore_torches.blockentity.LanternBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.fml.config.ModConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ReplaceAllFeature extends Feature<NoneFeatureConfiguration> {
    private static Map<BlockState, BlockState> REPLACEMENTS = new HashMap<>();
    private static BlockState TORCH_STATE = Blocks.TORCH.defaultBlockState();
    private static BlockState TORCH_STATE_EAST = Blocks.WALL_TORCH.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST);
    private static BlockState TORCH_STATE_NORTH = Blocks.WALL_TORCH.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
    private static BlockState TORCH_STATE_SOUTH = Blocks.WALL_TORCH.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH);
    private static BlockState TORCH_STATE_WEST = Blocks.WALL_TORCH.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST);
    private static BlockState LANTERN_STATE = Blocks.LANTERN.defaultBlockState();
    private static BlockState LANTERN_STATE_HANGING = Blocks.LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING, true);

    static {
        REPLACEMENTS.put(TORCH_STATE, BlockInit.LIT_TORCH.get().defaultBlockState());
        REPLACEMENTS.put(TORCH_STATE_EAST, BlockInit.LIT_WALL_TORCH.get().withPropertiesOf(TORCH_STATE_EAST));
        REPLACEMENTS.put(TORCH_STATE_NORTH, BlockInit.LIT_WALL_TORCH.get().withPropertiesOf(TORCH_STATE_NORTH));
        REPLACEMENTS.put(TORCH_STATE_SOUTH, BlockInit.LIT_WALL_TORCH.get().withPropertiesOf(TORCH_STATE_SOUTH));
        REPLACEMENTS.put(TORCH_STATE_WEST, BlockInit.LIT_WALL_TORCH.get().withPropertiesOf(TORCH_STATE_WEST));
        REPLACEMENTS.put(LANTERN_STATE, BlockInit.LIT_LANTERN.get().defaultBlockState());
        REPLACEMENTS.put(LANTERN_STATE_HANGING, BlockInit.LIT_LANTERN.get().withPropertiesOf(LANTERN_STATE_HANGING));
    }

    public ReplaceAllFeature(Codec codec) {
        super(codec);
    }

    @Override
    public boolean place(NoneFeatureConfiguration p_204741_, WorldGenLevel p_204742_, ChunkGenerator p_204743_, RandomSource p_204744_, BlockPos p_204745_) {
        return super.place(p_204741_, p_204742_, p_204743_, p_204744_, p_204745_);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        ChunkAccess chunk = level.getChunk(origin);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < chunk.getHeight(); y++) {
                    BlockPos pos = new BlockPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState oldState = chunk.getBlockState(pos);
                    BlockState newState = REPLACEMENTS.get(oldState);

                    if (newState != null) {
                        chunk.setBlockState(pos, newState, false);
                        Block newBlock = newState.getBlock();

                        if (newBlock instanceof BaseEntityBlock) {
                            BlockEntity newEntity = ((BaseEntityBlock) chunk.getBlockState(pos).getBlock()).newBlockEntity(pos, newState);
                            chunk.setBlockEntity(newEntity);

                            if (newEntity instanceof LanternBlockEntity) {
                                ((LanternBlockEntity) newEntity).setFuel(Config.defaultLanternFuel.get());
                            }

                            newEntity.setChanged();
                        }
                    }
                }
            }
        }

        return true;
    }
}
