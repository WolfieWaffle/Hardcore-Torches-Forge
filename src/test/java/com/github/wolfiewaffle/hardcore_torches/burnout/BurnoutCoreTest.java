package com.github.wolfiewaffle.hardcore_torches.burnout;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Dependency-free regression tests. Execute with tools/test-burnout-core.sh. */
public final class BurnoutCoreTest {
    private static int tests;

    public static void main(String[] args) {
        run("legacy fuel starts only when lit", () -> {
            FuelClock c = new FuelClock();
            c.restore(100, FuelClock.NO_DEADLINE);
            eq(100, c.remaining(1_000));
            c.setBurning(true, 1_000);
            eq(99, c.remaining(1_001));
            eq(0, c.remaining(1_100));
        });
        run("reads are pure and fuel never goes negative", () -> {
            FuelClock c = lit(100, 1_000);
            eq(75, c.remaining(1_025));
            eq(75, c.remaining(1_025));
            eq(1_100L, c.deadline());
            eq(0, c.remaining(Long.MAX_VALUE));
        });
        run("extinguish freezes remaining fuel", () -> {
            FuelClock c = lit(100, 0);
            c.setBurning(false, 25);
            eq(75, c.remaining(50_000));
            eq(FuelClock.NO_DEADLINE, c.deadline());
        });
        run("relight resumes, not refills", () -> {
            FuelClock c = lit(100, 0);
            c.setBurning(false, 25);
            c.setBurning(true, 1_000);
            eq(74, c.remaining(1_001));
            eq(0, c.remaining(1_075));
        });
        run("refuel extends deadline using remaining fuel", () -> {
            FuelClock c = lit(100, 1_000);
            c.setFuel(c.remaining(1_050) + 25, 1_050);
            eq(1_125L, c.deadline());
            eq(0, c.remaining(1_125));
        });
        run("fuel reduction advances deadline", () -> {
            FuelClock c = lit(100, 1_000);
            c.setFuel(3, 1_010);
            eq(1_013L, c.deadline());
            eq(0, c.remaining(1_013));
        });
        run("zero fuel is due immediately", () -> {
            FuelClock c = lit(1, 10);
            c.setFuel(0, 10);
            eq(0, c.remaining(10));
            eq(10L, c.deadline());
        });
        run("deadline survives save and chunk unload", () -> {
            FuelClock c = lit(100, 1_000);
            FuelClock restored = new FuelClock();
            restored.restore(c.remaining(1_025), c.deadline());
            restored.setBurning(true, 1_090);
            eq(10, restored.remaining(1_090));
            eq(0, restored.remaining(5_000));
        });
        run("server offline time does not count", () -> {
            FuelClock c = lit(100, 1_000);
            FuelClock restored = new FuelClock();
            restored.restore(c.remaining(1_025), c.deadline());
            eq(75, restored.remaining(1_025));
        });
        run("inactive NBT ignores an obsolete deadline", () -> {
            FuelClock c = new FuelClock();
            c.restore(75, FuelClock.NO_DEADLINE);
            eq(75, c.remaining(999_999));
        });
        run("time rollback cannot increase fuel above saved amount", () -> {
            FuelClock c = new FuelClock();
            c.restore(50, 1_100);
            eq(50, c.remaining(900));
        });
        run("deadline arithmetic saturates", () -> {
            FuelClock c = lit(100, Long.MAX_VALUE - 10);
            eq(Long.MAX_VALUE, c.deadline());
            eq(0, c.remaining(Long.MAX_VALUE));
        });
        run("negative fuel rejected", () -> {
            expectIllegal(() -> new FuelClock().setFuel(-1, 0));
        });
        run("empty queue", () -> {
            IndexedDeadlineQueue<String> q = new IndexedDeadlineQueue<>();
            eq(0, q.size());
            eq(null, q.pollDue(Long.MAX_VALUE));
        });
        run("deadline order and exact boundary", () -> {
            IndexedDeadlineQueue<String> q = new IndexedDeadlineQueue<>();
            q.schedule("later", 20);
            q.schedule("first", 5);
            q.schedule("middle", 10);
            eq(null, q.pollDue(4));
            eq("first", q.pollDue(5));
            eq("middle", q.pollDue(10));
            eq("later", q.pollDue(20));
            eq(0, q.size());
        });
        run("earlier reschedule replaces the old event", () -> {
            IndexedDeadlineQueue<String> q = new IndexedDeadlineQueue<>();
            q.schedule("a", 10_000);
            q.schedule("b", 30);
            q.schedule("a", 2);
            eq(2, q.size());
            eq("a", q.pollDue(2));
            eq("b", q.pollDue(30));
            eq(null, q.pollDue(10_000));
        });
        run("later reschedule repairs heap order", () -> {
            IndexedDeadlineQueue<String> q = new IndexedDeadlineQueue<>();
            q.schedule("a", 1);
            q.schedule("b", 2);
            q.schedule("a", 50);
            eq("b", q.pollDue(2));
            eq(null, q.pollDue(49));
            eq("a", q.pollDue(50));
        });
        run("cancellation removes memory immediately", () -> {
            IndexedDeadlineQueue<String> q = new IndexedDeadlineQueue<>();
            q.schedule("a", 10);
            q.schedule("b", 20);
            q.schedule("c", 30);
            q.cancel("b");
            q.cancel("unknown");
            eq(2, q.size());
            eq("a", q.pollDue(100));
            eq("c", q.pollDue(100));
            eq(null, q.pollDue(100));
        });
        run("100000 refuels do not accumulate tombstones", () -> {
            IndexedDeadlineQueue<String> q = new IndexedDeadlineQueue<>();
            for (int i = 0; i < 100_000; i++) q.schedule("lamp", i);
            eq(1, q.size());
            eq(null, q.pollDue(99_998));
            eq("lamp", q.pollDue(99_999));
            eq(0, q.size());
        });
        run("bounded drain and reentrant scheduling", () -> {
            IndexedDeadlineQueue<String> q = new IndexedDeadlineQueue<>();
            q.schedule("lamp", 1);
            int n = q.drainDue(1, 4, key -> q.schedule(key, 1));
            eq(4, n);
            eq(1, q.size());
            eq(0, q.drainDue(1, 0, key -> { throw new AssertionError(); }));
        });
        run("separate worlds have independent queues", () -> {
            IndexedDeadlineQueue<String> a = new IndexedDeadlineQueue<>();
            IndexedDeadlineQueue<String> b = new IndexedDeadlineQueue<>();
            a.schedule("same-position", 10);
            b.schedule("same-position", 20);
            a.cancel("same-position");
            eq("same-position", b.pollDue(20));
        });
        run("randomized indexed queue matches a reference map", () -> {
            IndexedDeadlineQueue<Integer> q = new IndexedDeadlineQueue<>();
            Map<Integer, Long> reference = new HashMap<>();
            Random random = new Random(482_913);
            for (int step = 0; step < 100_000; step++) {
                int key = random.nextInt(200);
                int op = random.nextInt(3);
                long time = random.nextInt(5_000);
                if (op == 0) {
                    q.schedule(key, time);
                    reference.put(key, time);
                } else if (op == 1) {
                    q.cancel(key);
                    reference.remove(key);
                } else {
                    long earliest = reference.values().stream().mapToLong(Long::longValue).min().orElse(Long.MAX_VALUE);
                    Integer actual = q.pollDue(time);
                    if (earliest <= time) {
                        if (actual == null) throw new AssertionError("missing due event");
                        eq(earliest, reference.remove(actual).longValue());
                    } else eq(null, actual);
                }
                eq(reference.size(), q.size());
            }
        });
        run("geometric delay bounds", () -> {
            eq(1, FuelMath.geometricDelay(0.0, 1.0 / 200));
            eq(1, FuelMath.geometricDelay(0.75, 1.0));
            if (FuelMath.geometricDelay(Math.nextDown(1.0), 1.0 / 200) < 1) throw new AssertionError();
        });
        run("geometric rain sampling has the expected mean", () -> {
            Random r = new Random(219);
            long total = 0;
            int samples = 200_000;
            for (int i = 0; i < samples; i++) total += FuelMath.geometricDelay(r.nextDouble(), 1.0 / 200);
            double mean = (double) total / samples;
            if (Math.abs(mean - 200) > 2) throw new AssertionError("mean=" + mean);
        });
        run("fuel additions clamp without integer overflow", () -> {
            eq(Integer.MAX_VALUE, FuelMath.addClamped(Integer.MAX_VALUE - 10, 100, Integer.MAX_VALUE));
            eq(0, FuelMath.addClamped(5, -10, 100));
            eq(100, FuelMath.addClamped(90, 20, 100));
        });
        run("scaled fuel ignores zero and invalid factors", () -> {
            eq(0, FuelMath.scaledFuel(1600, 0));
            eq(0, FuelMath.scaledFuel(1600, Double.NaN));
            eq(12_800, FuelMath.scaledFuel(1600, 8));
            eq(Integer.MAX_VALUE, FuelMath.scaledFuel(Integer.MAX_VALUE, 8));
        });
        System.out.println("PASS: " + tests + " test groups");
    }

    private static FuelClock lit(int fuel, long now) {
        FuelClock c = new FuelClock();
        c.setFuel(fuel, now);
        c.setBurning(true, now);
        return c;
    }
    private static void run(String name, Runnable body) {
        try { body.run(); tests++; System.out.println("PASS " + name); }
        catch (Throwable t) { throw new AssertionError("FAIL " + name, t); }
    }
    private static void eq(long expected, long actual) {
        if (expected != actual) throw new AssertionError("expected=" + expected + ", actual=" + actual);
    }
    private static void eq(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) throw new AssertionError("expected=" + expected + ", actual=" + actual);
    }
    private static void expectIllegal(Runnable body) {
        try { body.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("expected IllegalArgumentException");
    }
}
