package com.github.wolfiewaffle.hardcore_torches.burnout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * One entry per key. O(1) idle polling; O(log n) insertion, rescheduling and removal.
 * Unlike a priority queue with generation tokens, repeated refueling cannot leave
 * arbitrarily many stale entries behind a far-future deadline.
 * Single-threaded: all Minecraft callers must run on the owning server thread.
 */
public final class IndexedDeadlineQueue<K> {
    private static final class Entry<K> {
        final K key;
        long deadline;
        int index;

        Entry(K key, long deadline, int index) {
            this.key = key;
            this.deadline = deadline;
            this.index = index;
        }
    }

    private final ArrayList<Entry<K>> heap = new ArrayList<>();
    private final Map<K, Entry<K>> entries = new HashMap<>();

    public int size() {
        return heap.size();
    }

    public void schedule(K key, long deadline) {
        Objects.requireNonNull(key, "key");
        Entry<K> entry = entries.get(key);
        if (entry == null) {
            entry = new Entry<>(key, deadline, heap.size());
            entries.put(key, entry);
            heap.add(entry);
            siftUp(entry.index);
        } else {
            long previous = entry.deadline;
            entry.deadline = deadline;
            if (deadline < previous) siftUp(entry.index);
            else if (deadline > previous) siftDown(entry.index);
        }
    }

    public void cancel(K key) {
        Entry<K> entry = entries.get(key);
        if (entry != null) removeAt(entry.index);
    }

    public K pollDue(long now) {
        if (heap.isEmpty() || heap.get(0).deadline > now) return null;
        K key = heap.get(0).key;
        removeAt(0);
        return key;
    }

    /** The callback may cancel or reschedule entries, including its own key. */
    public int drainDue(long now, int budget, Consumer<? super K> callback) {
        Objects.requireNonNull(callback, "callback");
        int count = 0;
        while (count < budget) {
            K key = pollDue(now);
            if (key == null) break;
            count++;
            callback.accept(key);
        }
        return count;
    }

    private void removeAt(int index) {
        Entry<K> removed = heap.get(index);
        entries.remove(removed.key);
        Entry<K> last = heap.remove(heap.size() - 1);
        if (index == heap.size()) return;
        heap.set(index, last);
        last.index = index;
        if (index > 0 && last.deadline < heap.get((index - 1) / 2).deadline) siftUp(index);
        else siftDown(index);
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap.get(parent).deadline <= heap.get(index).deadline) return;
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        while (true) {
            int left = index * 2 + 1;
            if (left >= heap.size()) return;
            int right = left + 1;
            int smallest = right < heap.size() && heap.get(right).deadline < heap.get(left).deadline ? right : left;
            if (heap.get(index).deadline <= heap.get(smallest).deadline) return;
            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int a, int b) {
        Entry<K> first = heap.get(a);
        Entry<K> second = heap.get(b);
        heap.set(a, second);
        heap.set(b, first);
        second.index = a;
        first.index = b;
    }
}
