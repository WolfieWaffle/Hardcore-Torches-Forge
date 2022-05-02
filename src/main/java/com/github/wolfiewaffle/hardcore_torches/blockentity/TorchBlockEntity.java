package com.github.wolfiewaffle.hardcore_torches.blockentity;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TorchBlockEntity extends FuelBlockEntity {

    public TorchBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.TORCH_BLOCK_ENTITY.get(), pos, state);
        Block block =  state.getBlock();

        if (block instanceof AbstractHardcoreTorchBlock) fuel = ((AbstractHardcoreTorchBlock) state.getBlock()).maxFuel.getAsInt();
    }

    public void tick() {
        Level world = getLevel();

        if (!world.isClientSide) {
            BlockPos pos = getBlockPos();
            BlockState state = getBlockState();

            if (!(state.getBlock() instanceof AbstractHardcoreTorchBlock)) return;

            if (((AbstractHardcoreTorchBlock) state.getBlock()).burnState == ETorchState.LIT) {
                tickLit(world, pos, state);
            } else if (((AbstractHardcoreTorchBlock) state.getBlock()).burnState == ETorchState.SMOLDERING) {
                tickSmoldering(world, pos, state);
            }
        }
    }

    private void tickLit(Level world, BlockPos pos, BlockState state) {

        // Extinguish
        if (Config.torchesRain.get() && world.isRainingAt(pos)) {
            if (random.nextInt(200) == 0) {
                if (Config.torchesSmolder.get()) {
                    ((AbstractHardcoreTorchBlock) world.getBlockState(pos).getBlock()).smother(world, pos, state);
                } else {
                    ((AbstractHardcoreTorchBlock) world.getBlockState(pos).getBlock()).extinguish(world, pos, state);
                }
            }
        }

        // Burn out
        if (fuel >= 0) {
            changeFuel(-1);
        }

        setChanged();
    }

    private void tickSmoldering(Level world, BlockPos pos, BlockState state) {

        // Burn out
        if (random.nextInt(3) == 0) {
            if (fuel > 0) {
                fuel--;

                if (fuel <= 0) {
                    ((AbstractHardcoreTorchBlock) world.getBlockState(pos).getBlock()).burnOut(world, pos, state, false);
                }
            }
        }

        setChanged();
    }
}
