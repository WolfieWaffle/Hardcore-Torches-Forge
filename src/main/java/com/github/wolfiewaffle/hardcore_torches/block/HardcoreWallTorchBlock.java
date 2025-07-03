package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
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

import java.util.Map;
import java.util.function.IntSupplier;

public class HardcoreWallTorchBlock extends AbstractHardcoreTorchBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(5.5F, 3.0F, 11.0F, 10.5F, 13.0F, 16.0F), Direction.SOUTH, Block.box(5.5F, 3.0F, 0.0F, 10.5F, 13.0F, 5.0F), Direction.WEST, Block.box(11.0F, 3.0F, 5.5F, 16.0F, 13.0F, 10.5F), Direction.EAST, Block.box(0.0F, 3.0F, 5.5F, 5.0F, 13.0F, 10.5F)));

    public static final MapCodec<HardcoreFloorTorchBlock> CODEC = null;

    public HardcoreWallTorchBlock(Properties prop, SimpleParticleType fireParticle, SimpleParticleType smokeParticle, ETorchState burnState, TorchGroup group, IntSupplier maxFuel) {
        super(prop, fireParticle, smokeParticle, burnState, group, maxFuel);
    }

    @Override
    public boolean isWall() {
        return true;
    }

    // region Overridden methods for TorchBlock since I can't extend 2 classes
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        TorchTools.displayParticle(fireParticle, state, world, pos);
        TorchTools.displayParticle(smokeParticle, state, world, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABBS.get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);

        BlockPos blockpos = pos.relative(facing.getOpposite());
        BlockState blockstate = level.getBlockState(blockpos);
        return blockstate.isFaceSturdy(level, blockpos, facing);
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
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        super.createBlockStateDefinition(stateManager);
        stateManager.add(FACING);
    }
    // endregion

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
