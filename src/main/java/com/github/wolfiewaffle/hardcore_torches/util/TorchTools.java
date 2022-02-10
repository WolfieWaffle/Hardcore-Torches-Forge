package com.github.wolfiewaffle.hardcore_torches.util;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TorchTools {

    public static boolean canLight(Item item, Block block) {

        if (item instanceof TorchItem) {
            ETorchState state = ((TorchItem) item).burnState;

            if (state == ETorchState.UNLIT || state == ETorchState.SMOLDERING) {
                if (MainMod.FREE_TORCH_LIGHT_BLOCKS.contains(block)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void displayParticle(BasicParticleType particle, BlockState state, World world, BlockPos pos, float spread) {
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getY() + 0.7;
        double f = (double)pos.getZ() + 0.5;

        if (particle != null) {
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                Direction dir2 = dir.getOpposite();

                world.addParticle(particle, d + 0.27D * (double)dir2.getStepX(), e + 0.22D, f + 0.27D * (double)dir2.getStepZ(), 0.0D, 0.0D, 0.0D);
            } else {
                world.addParticle(particle, d, e, f, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public static void displayParticle(BasicParticleType particle, BlockState state, World world, BlockPos pos) {
        displayParticle(particle, state, world, pos, 0f);
    }
}
