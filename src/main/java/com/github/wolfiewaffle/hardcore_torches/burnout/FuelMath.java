package com.github.wolfiewaffle.hardcore_torches.burnout;

/** Arithmetic shared by the event-driven integration and the standalone tests. */
public final class FuelMath {
    private FuelMath() {}

    public static long deadlineAfter(long now, long delay) {
        if (delay < 0 || now < 0) throw new IllegalArgumentException("Negative game time or delay");
        return now > Long.MAX_VALUE - delay ? Long.MAX_VALUE : now + delay;
    }

    public static int addClamped(int current, int increment, int maximum) {
        return (int) Math.max(0L, Math.min(Math.max(0, maximum), (long) current + increment));
    }

    public static int scaledFuel(int burnTime, double factor) {
        if (burnTime <= 0 || !Double.isFinite(factor) || factor <= 0) return 0;
        return (int) Math.min(Integer.MAX_VALUE, burnTime * factor);
    }

    /** Sample the number of ticks until the next independent Bernoulli success. */
    public static int geometricDelay(double uniform, double probability) {
        if (!(uniform >= 0 && uniform < 1) || !(probability > 0 && probability <= 1)) {
            throw new IllegalArgumentException("Expected 0 <= u < 1 and 0 < p <= 1");
        }
        if (probability == 1) return 1;
        double failures = Math.floor(Math.log1p(-uniform) / Math.log1p(-probability));
        return (int) Math.min(Integer.MAX_VALUE, failures + 1);
    }
}
