package com.github.wolfiewaffle.hardcore_torches.blockentity;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.burnout.FuelMath;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TorchBlockEntity extends FuelBlockEntity {
    private long nextRainCheck = -1;

    public TorchBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.TORCH_BLOCK_ENTITY.get(), pos, state);
        if (state.getBlock() instanceof AbstractHardcoreTorchBlock torch) {
            fuelTimer.set(torch.maxFuel.getAsInt());
        }
    }

    @Override
    public int getMaxFuel() {
        return Config.defaultTorchFuel.get();
    }

    @Override
    public boolean burnsContinuously(BlockState state) {
        return state.getBlock() instanceof AbstractHardcoreTorchBlock torch
                && torch.burnState == ETorchState.LIT;
    }

    @Override
    public long nextBurnoutCheck(long fuelDeadline, long now) {
        if (!Config.torchesRain.get()) return fuelDeadline;
        if (nextRainCheck < 0) nextRainCheck = nextRainOpportunity(now);
        return Math.min(fuelDeadline, nextRainCheck);
    }

    private long nextRainOpportunity(long now) {
        return FuelMath.deadlineAfter(now, FuelMath.geometricDelay(random.nextDouble(), 1.0 / 200.0));
    }

    @Override
    public void onBurnoutCheck() {
        Level world = getLevel();
        if (world == null || world.isClientSide || isRemoved() || !burnsContinuously(getBlockState())) return;
        if (getFuel() == 0) {
            super.onBurnoutCheck();
            return;
        }
        long now = world.getGameTime();
        if (Config.torchesRain.get() && nextRainCheck >= 0 && now >= nextRainCheck) {
            nextRainCheck = nextRainOpportunity(now);
            if (world.isRainingAt(getBlockPos())) {
                BlockState state = getBlockState();
                AbstractHardcoreTorchBlock torch = (AbstractHardcoreTorchBlock) state.getBlock();
                if (Config.torchesSmolder.get()) torch.smother(world, getBlockPos(), state);
                else torch.extinguish(world, getBlockPos(), state, true);
                return;
            }
        }
        fuelTimer.refresh();
    }

    /** Only smoldering torches keep their original 1/3-per-tick stochastic burn. */
    public void tick() {
        Level world = getLevel();
        if (world == null || world.isClientSide || isRemoved()) return;
        BlockState state = getBlockState();
        if (state.getBlock() instanceof AbstractHardcoreTorchBlock torch
                && torch.burnState == ETorchState.SMOLDERING
                && getFuel() > 0 && random.nextInt(3) == 0) {
            setFuel(getFuel() - 1);
            if (getFuel() == 0) torch.burnOut(world, getBlockPos(), state, false);
        }
    }
}
