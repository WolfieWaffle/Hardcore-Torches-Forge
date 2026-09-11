package com.github.wolfiewaffle.hardcore_torches.burnout;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Server-thread-only lifecycle contract for a fuelled block. */
public interface ScheduledBurner {
    BlockEntity burnoutBlockEntity();

    boolean burnsContinuously(BlockState state);

    default long nextBurnoutCheck(long fuelDeadline, long now) {
        return fuelDeadline;
    }

    void onBurnoutCheck();
}
