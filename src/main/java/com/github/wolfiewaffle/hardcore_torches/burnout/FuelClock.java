package com.github.wolfiewaffle.hardcore_torches.burnout;

/**
 * Fuel measured in game ticks. Reads do not mutate state or require periodic saves.
 * Persist both remaining(now) and deadline(): the latter makes an otherwise clean
 * chunk's saved data remain valid as game time advances.
 */
public final class FuelClock {
    public static final long NO_DEADLINE = -1L;
    private int fuel;
    private long deadline = NO_DEADLINE;

    public int remaining(long now) {
        if (deadline == NO_DEADLINE) return fuel;
        if (now >= deadline) return 0;
        // Avoid subtraction overflow, and never manufacture fuel after a clock rollback.
        if (now < 0 || deadline - now > fuel) return fuel;
        return (int) (deadline - now);
    }

    public long deadline() {
        return deadline;
    }

    public void setFuel(int value, long now) {
        if (value < 0) throw new IllegalArgumentException("Fuel must be nonnegative");
        fuel = value;
        if (deadline != NO_DEADLINE) deadline = FuelMath.deadlineAfter(now, value);
    }

    public void setBurning(boolean burning, long now) {
        if (burning) {
            if (deadline == NO_DEADLINE) deadline = FuelMath.deadlineAfter(now, fuel);
        } else if (deadline != NO_DEADLINE) {
            fuel = remaining(now);
            deadline = NO_DEADLINE;
        }
    }

    public void restore(int savedFuel, long savedDeadline) {
        fuel = Math.max(0, savedFuel);
        deadline = savedDeadline < 0 ? NO_DEADLINE : savedDeadline;
    }
}
