package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Random;

public class HardcoreFloorTorchBlock extends AbstractHardcoreTorchBlock {

    public HardcoreFloorTorchBlock(Properties prop, BasicParticleType particle, ETorchState burnState, TorchGroup group) {
        super(prop, particle, burnState, group);
    }

    @Override
    public boolean isWall() {
        return false;
    }

    // region Overridden methods for TorchBlock since I can't extend 2 classes
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TorchTools.displayParticle(particle, state, world, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
        return Blocks.TORCH.getShape(state, world, pos, ctx);
    }

    @Override
    public BlockState updateShape(BlockState p_57503_, Direction p_57504_, BlockState p_57505_, IWorld p_57506_, BlockPos p_57507_, BlockPos p_57508_) {
        return Blocks.TORCH.updateShape(p_57503_, p_57504_, p_57505_, p_57506_, p_57507_, p_57508_);
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return Blocks.TORCH.canSurvive(state, world, pos);
    }
    // endregion


}
