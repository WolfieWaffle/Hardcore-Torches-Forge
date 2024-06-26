package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.IntSupplier;

public abstract class HardcoreWallTorchBlock extends AbstractHardcoreTorchBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public HardcoreWallTorchBlock(Properties prop, SimpleParticleType particle, ETorchState burnState, TorchGroup group, IntSupplier maxFuel) {
        super(prop, particle, burnState, group, maxFuel);
    }

    @Override
    public boolean isWall() {
        return true;
    }

    // region Overridden methods for TorchBlock since I can't extend 2 classes
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        TorchTools.displayParticle(particle, state, world, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return Blocks.WALL_TORCH.getShape(state, world, pos, ctx);
    }

    @Override
    public BlockState updateShape(BlockState p_57503_, Direction p_57504_, BlockState p_57505_, LevelAccessor p_57506_, BlockPos p_57507_, BlockPos p_57508_) {
        return Blocks.WALL_TORCH.updateShape(p_57503_, p_57504_, p_57505_, p_57506_, p_57507_, p_57508_);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return Blocks.WALL_TORCH.canSurvive(state, world, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState torchState = Blocks.WALL_TORCH.getStateForPlacement(ctx);

        if (torchState != null) {
            BlockState state = this.defaultBlockState();
            Direction d = torchState.getValue(FACING);
            return state.setValue(FACING, d);
        }

        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return Blocks.WALL_TORCH.rotate(state, rotation);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return Blocks.WALL_TORCH.mirror(state, mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        super.createBlockStateDefinition(stateManager);
        stateManager.add(FACING);
    }
    // endregion
}
