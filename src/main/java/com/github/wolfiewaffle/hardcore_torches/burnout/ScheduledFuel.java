package com.github.wolfiewaffle.hardcore_torches.burnout;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Persists a deadline instead of dirtying the chunk on every fuel decrement. */
public final class ScheduledFuel {
    private static final String DEADLINE_TAG = "BurnoutDeadline";
    private final ScheduledBurner burner;
    private final FuelClock clock = new FuelClock();
    private boolean loaded;

    public ScheduledFuel(ScheduledBurner burner) {
        this.burner = burner;
    }

    private BlockEntity entity() {
        return burner.burnoutBlockEntity();
    }

    private long now() {
        return entity().getLevel() == null ? 0 : Math.max(0, entity().getLevel().getGameTime());
    }

    public int get() {
        return clock.remaining(now());
    }

    public void set(int fuel) {
        clock.setFuel(Math.max(0, fuel), now());
        entity().setChanged();
        refresh();
    }

    public void onLoad() {
        loaded = true;
        refresh();
    }

    public void onRemoved() {
        loaded = false;
        BurnoutScheduler.cancel(burner);
        // Keep the deadline: unloaded-chunk game time intentionally counts.
    }

    /** Also called after same-block LIT/waterlogging changes and after a due event. */
    public void refresh() {
        if (!loaded || !(entity().getLevel() instanceof ServerLevel) || entity().isRemoved()) return;
        long time = now();
        long previousDeadline = clock.deadline();
        clock.setBurning(burner.burnsContinuously(entity().getBlockState()), time);
        if (clock.deadline() != previousDeadline) entity().setChanged();

        if (clock.deadline() == FuelClock.NO_DEADLINE) {
            BurnoutScheduler.cancel(burner);
        } else {
            BurnoutScheduler.schedule(burner, burner.nextBurnoutCheck(clock.deadline(), time));
        }
    }

    public void load(CompoundTag tag) {
        long deadline = tag.contains(DEADLINE_TAG, Tag.TAG_LONG)
                && burner.burnsContinuously(entity().getBlockState())
                ? tag.getLong(DEADLINE_TAG) : FuelClock.NO_DEADLINE;
        clock.restore(tag.getInt("Fuel"), deadline);
        refresh();
    }

    public void save(CompoundTag tag) {
        // Old versions can still read Fuel; the added deadline prevents stale autosaves
        // from refunding the time elapsed between that save and the next unload/restart.
        tag.putInt("Fuel", get());
        if (clock.deadline() == FuelClock.NO_DEADLINE) tag.remove(DEADLINE_TAG);
        else tag.putLong(DEADLINE_TAG, clock.deadline());
    }
}
