package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.IntSupplier;

public class HardcoreFloorTorchBlock extends AbstractHardcoreTorchBlock {

    public static final MapCodec<HardcoreFloorTorchBlock> CODEC = null;
    protected static final int AABB_STANDING_OFFSET = 2;
    protected static final VoxelShape AABB = Block.box(6.0F, 0.0F, 6.0F, 10.0F, 10.0F, 10.0F);

    public HardcoreFloorTorchBlock(Properties prop, SimpleParticleType fireParticle, SimpleParticleType smokeParticle, ETorchState burnState, TorchGroup group, IntSupplier maxFuel) {
        super(prop, fireParticle, smokeParticle, burnState, group, maxFuel);
    }

    @Override
    public boolean isWall() {
        return false;
    }

    // region Overridden methods for TorchBlock since I can't extend 2 classes
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        TorchTools.displayParticle(fireParticle, state, world, pos);
        TorchTools.displayParticle(smokeParticle, state, world, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    protected BlockState updateShape(BlockState state1, Direction direction, BlockState state2, LevelAccessor world, BlockPos pos1, BlockPos pos2) {
        return direction == Direction.DOWN && !this.canSurvive(state1, world, pos1) ? Blocks.AIR.defaultBlockState() : super.updateShape(state1, direction, state2, world, pos1, pos2);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return canSupportCenter(world, pos.below(), Direction.UP);
    }
    // endregion

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
