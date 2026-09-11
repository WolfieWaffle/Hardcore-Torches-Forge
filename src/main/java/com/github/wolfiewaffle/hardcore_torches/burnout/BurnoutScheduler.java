package com.github.wolfiewaffle.hardcore_torches.burnout;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.IdentityHashMap;
import java.util.Map;

/** One idle queue-head check per ticking server level, not one callback per block. */
@EventBusSubscriber(modid = HardcoreTorches.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class BurnoutScheduler {
    // Bounds callback count, not milliseconds. Large simultaneous expiries may be deferred.
    static final int MAX_EVENTS_PER_TICK = 1024;
    private static final Map<ServerLevel, IndexedDeadlineQueue<ScheduledBurner>> QUEUES =
            new IdentityHashMap<>();

    private BurnoutScheduler() {}

    public static void schedule(ScheduledBurner burner, long deadline) {
        BlockEntity entity = burner.burnoutBlockEntity();
        if (entity.getLevel() instanceof ServerLevel level && !entity.isRemoved()) {
            long nextTick = FuelMath.deadlineAfter(Math.max(0, level.getGameTime()), 1);
            QUEUES.computeIfAbsent(level, ignored -> new IndexedDeadlineQueue<>())
                    .schedule(burner, Math.max(nextTick, deadline));
        }
    }

    public static void cancel(ScheduledBurner burner) {
        if (burner.burnoutBlockEntity().getLevel() instanceof ServerLevel level) {
            IndexedDeadlineQueue<ScheduledBurner> queue = QUEUES.get(level);
            if (queue != null) queue.cancel(burner);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.tickRateManager().runsNormally()) return;
        IndexedDeadlineQueue<ScheduledBurner> queue = QUEUES.get(level);
        if (queue == null) return;

        long now = level.getGameTime();
        for (int count = 0; count < MAX_EVENTS_PER_TICK; count++) {
            ScheduledBurner burner = queue.pollDue(now);
            if (burner == null) break;
            BlockEntity entity = burner.burnoutBlockEntity();
            if (entity.isRemoved() || entity.getLevel() != level) continue;

            BlockPos pos = entity.getBlockPos();
            // getChunkNow never generates/loads a chunk. Avoid even lazy BE creation.
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null || chunk.getBlockEntities().get(pos) != entity) continue;
            burner.onBurnoutCheck();
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) QUEUES.remove(level);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        QUEUES.clear();
    }
}
