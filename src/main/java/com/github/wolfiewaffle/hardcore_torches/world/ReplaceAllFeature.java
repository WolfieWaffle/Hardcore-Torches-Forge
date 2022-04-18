package com.github.wolfiewaffle.hardcore_torches.world;

import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ReplaceAllFeature extends Feature<NoneFeatureConfiguration> {
    private static Map<BlockState, BlockState> REPLACEMENTS = new HashMap<>();
    private static BlockState STATE_EAST = Blocks.WALL_TORCH.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST);
    private static BlockState STATE_NORTH = Blocks.WALL_TORCH.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
    private static BlockState STATE_SOUTH = Blocks.WALL_TORCH.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH);
    private static BlockState STATE_WEST = Blocks.WALL_TORCH.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST);

    static {
        REPLACEMENTS.put(STATE_EAST, BlockInit.LIT_WALL_TORCH.get().withPropertiesOf(STATE_EAST));
        REPLACEMENTS.put(STATE_NORTH, BlockInit.LIT_WALL_TORCH.get().withPropertiesOf(STATE_NORTH));
        REPLACEMENTS.put(STATE_SOUTH, BlockInit.LIT_WALL_TORCH.get().withPropertiesOf(STATE_SOUTH));
        REPLACEMENTS.put(STATE_WEST, BlockInit.LIT_WALL_TORCH.get().withPropertiesOf(STATE_WEST));
        REPLACEMENTS.put(Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIAMOND_BLOCK.defaultBlockState());
    }

    public ReplaceAllFeature(Codec codec) {
        super(codec);
    }

    @Override
    public boolean place(NoneFeatureConfiguration p_204741_, WorldGenLevel p_204742_, ChunkGenerator p_204743_, Random p_204744_, BlockPos p_204745_) {
        System.out.println("OTHER PLACE");

        return super.place(p_204741_, p_204742_, p_204743_, p_204744_, p_204745_);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        ChunkAccess chunk = context.level().getChunk(origin);
        System.out.println("ORIGIN" + origin);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < chunk.getHeight(); y++) {
                    BlockPos pos = new BlockPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    System.out.println(pos);

                    BlockState newState = REPLACEMENTS.get(chunk.getBlockState(pos));
                    if (newState != null) chunk.setBlockState(pos, newState, false);
                    if (chunk.getBlockState(pos).getBlock() != Blocks.AIR) chunk.setBlockState(pos, Blocks.IRON_BLOCK.defaultBlockState(), false);
                }
            }
        }

        return true;
    }
}
